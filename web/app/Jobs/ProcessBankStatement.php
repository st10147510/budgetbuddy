<?php

namespace App\Jobs;

use App\Models\StatementUpload;
use App\Services\FirebaseStorageService;
use App\Services\FirestoreService;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Storage;
use Smalot\PdfParser\Parser;
use Throwable;

class ProcessBankStatement implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public int $tries   = 3;
    public int $timeout = 120;

    public function __construct(
        public readonly int    $uploadId,
        public readonly string $uid,
    ) {}

    public function handle(): void
    {
        $upload = StatementUpload::findOrFail($this->uploadId);
        $upload->update(['status' => 'processing']);

        try {
            $localPath = Storage::path($upload->path);

            // ── 1. Upload PDF to Firebase Storage ────────────────────────────
            try {
                $storageUrl = (new FirebaseStorageService())
                    ->uploadStatement($this->uid, $localPath, $upload->filename);
                $upload->update(['storage_url' => $storageUrl]);
            } catch (Throwable $e) {
                Log::warning("ProcessBankStatement #{$this->uploadId}: Storage upload failed: " . $e->getMessage());
            }

            // ── 2. Parse transactions from PDF ───────────────────────────────
            $transactions = $this->parse($localPath, $upload->default_category);

            if (empty($transactions)) {
                $upload->update(['status' => 'done', 'rows_imported' => 0]);
                return;
            }

            // ── 3. Write to Firestore with field names matching Android ───────
            $fs       = new FirestoreService();
            $imported = 0;
            $now      = (int) (microtime(true) * 1000);

            foreach ($transactions as $tx) {
                // Deterministic ID: hash of (uid, date, amount, notes) so that
                // re-uploading the same statement is idempotent (PATCH overwrites
                // the same document rather than creating a duplicate).
                $hashInput   = "{$this->uid}|{$tx['date']}|{$tx['amount']}|{$tx['notes']}";
                $firestoreId = abs(crc32($hashInput));

                // Exact field names from Android FirestoreRepository.TransactionEntity.toMap()
                $document = [
                    'id'               => $firestoreId,
                    'userId'           => $this->uid,
                    'amount'           => $tx['amount'],
                    'categoryId'       => $tx['categoryId'],
                    'date'             => $tx['date'],
                    'notes'            => $tx['notes'],
                    'receiptImagePath' => '',
                    'type'             => $tx['type'],
                    'createdAt'        => $now,
                ];

                try {
                    $fs->setDocument($this->uid, 'transactions', (string) $firestoreId, $document);
                    $imported++;
                } catch (Throwable $e) {
                    Log::warning("ProcessBankStatement: failed to write tx #{$firestoreId}: " . $e->getMessage());
                }
            }

            $upload->update(['status' => 'done', 'rows_imported' => $imported]);

        } catch (Throwable $e) {
            Log::error("ProcessBankStatement #{$this->uploadId} failed: " . $e->getMessage());
            $upload->update(['status' => 'failed', 'error' => $e->getMessage()]);
            $this->fail($e);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PDF PARSER
    // Handles SA bank PDFs where column whitespace is stripped by the renderer,
    // producing lines like:
    //   "01 May 2026Eskom Payment-R 6,740.79R 38,509.96"
    //   "01 May 2026Salary PaymentR 55,000.00R 93,500.00Income"
    //   "02 May 2026Transfer to Savings-R 7,549.49R 85,950.51Category 9"
    // ═════════════════════════════════════════════════════════════════════════

    private function parse(string $path, ?string $defaultCategory): array
    {
        $parser = new Parser();
        $pdf    = $parser->parseFile($path);

        $pages = $pdf->getPages();
        $text  = count($pages) > 1
            ? implode("\n", array_map(fn ($p) => $p->getText(), $pages))
            : $pdf->getText();

        return $this->extractTransactions($text, $defaultCategory);
    }

    private function extractTransactions(string $text, ?string $defaultCategory): array
    {
        $transactions = [];
        foreach (preg_split('/\r?\n/', $text) as $line) {
            $tx = $this->parseLine(trim($line), $defaultCategory);
            if ($tx !== null) {
                $transactions[] = $tx;
            }
        }
        return $transactions;
    }

    /**
     * Parse one line from a SA bank statement PDF.
     *
     * After text extraction column spacing is lost, so a row looks like:
     *   {date}{description}{-R amount}{R balance}[{Category N|Income|Expense}]
     *
     * Returns null if the line is not a transaction row.
     */
    private function parseLine(string $line, ?string $defaultCategory): ?array
    {
        if (strlen($line) < 10) return null;

        // ── Step 1: Date at start of line (no space required after) ───────────
        $dateMs = $this->extractLeadingDate($line, $afterDate);
        if ($dateMs === null) return null;

        $rest = trim($afterDate ?? '');
        if (strlen($rest) < 3) return null;

        // ── Step 2: Peel off optional suffix: "Category N", "Income", "Expense"
        $explicitCategoryId = null;
        $explicitIsIncome   = null;

        if (preg_match('/\s*Category\s+(\d+)\s*$/i', $rest, $m)) {
            $explicitCategoryId = (int) $m[1];
            $rest = substr($rest, 0, -strlen($m[0]));
        } elseif (preg_match('/\s+Income\s*$/i', $rest)) {
            $explicitIsIncome = true;
            $rest = rtrim(preg_replace('/\s+Income\s*$/i', '', $rest));
        } elseif (preg_match('/\s+Expense\s*$/i', $rest)) {
            $explicitIsIncome = false;
            $rest = rtrim(preg_replace('/\s+Expense\s*$/i', '', $rest));
        }

        // ── Step 3: Extract all R-amounts from the rest of the line ───────────
        // Matches: -R 6,740.79  |  R 55,000.00  |  -R38,509.96  |  R2,704.70
        $amounts = [];
        // Matches: -R 6,740.79 | R 55,000.00 | R -7,402.68 | -R7,402.68
        // Both leading-minus (-R) and trailing-minus (R -) forms are handled.
        $desc    = preg_replace_callback(
            '/(-?)\s*R\s*(-?)\s*([\d,]+\.\d{2})/i',
            function ($m) use (&$amounts) {
                $negative = $m[1] === '-' || $m[2] === '-';
                $value    = (float) str_replace(',', '', $m[3]);
                if ($value > 0) {
                    $amounts[] = ['value' => $value, 'negative' => $negative];
                }
                return '';
            },
            $rest
        );

        if (empty($amounts)) return null;

        $desc = $this->cleanDesc($desc);
        if (strlen($desc) < 2) return null;

        // ── Step 4: First amount = transaction; last amount = running balance ──
        // When there are 2+ amounts, the last one is always the running balance.
        $tx      = $amounts[0];
        $amount  = $tx['value'];

        if ($explicitIsIncome !== null) {
            $isIncome = $explicitIsIncome;
        } elseif ($tx['negative']) {
            // Negative sign = debit = expense
            $isIncome = false;
        } else {
            // Positive and no explicit marker → check description keywords
            $isIncome = $this->isIncome($desc);
        }

        if ($amount <= 0) return null;

        $categoryId = $explicitCategoryId
            ?? ($defaultCategory ? (int) $defaultCategory : $this->guessCategory($desc));

        return [
            'amount'     => round($amount, 2),
            'type'       => $isIncome ? 'INCOME' : 'EXPENSE',
            'categoryId' => $categoryId,
            'date'       => $dateMs,
            'notes'      => $desc,
        ];
    }

    // ── Date extraction ───────────────────────────────────────────────────────

    /**
     * Extract a date from the beginning of $line.
     * Does NOT require whitespace after the date — PDF renderers strip it.
     * Sets $rest to the text after the matched date.
     */
    private function extractLeadingDate(string $line, ?string &$rest): ?int
    {
        // Ordered most-to-least specific; no \s+ required after date
        $patterns = [
            // YYYY/MM/DD or YYYY-MM-DD (must be followed by non-digit or end)
            ['/^(\d{4}[\/\-]\d{2}[\/\-]\d{2})(.*)$/us', ['Y/m/d', 'Y-m-d']],
            // DD/MM/YYYY or DD-MM-YYYY
            ['/^(\d{2}[\/\-]\d{2}[\/\-]\d{4})(.*)$/us', ['d/m/Y', 'd-m-Y']],
            // DD Mon YYYY  (e.g. "01 May 2026")
            ['/^(\d{1,2}\s+[A-Za-z]{3}\s+\d{4})(.*)$/us', ['j M Y', 'd M Y']],
            // DD Mon YY  (e.g. "01 May 26")
            ['/^(\d{1,2}\s+[A-Za-z]{3}\s+\d{2})(.*)$/us', ['d M y', 'j M y']],
            // DD Mon  (ABSA omits year — assume current year)
            ['/^(\d{1,2}\s+[A-Za-z]{3})(\s.*)$/us', ['d M']],
        ];

        foreach ($patterns as [$regex, $formats]) {
            if (!preg_match($regex, $line, $m)) continue;

            $rawDate = trim($m[1]);
            $rest    = $m[2];

            foreach ($formats as $fmt) {
                if ($fmt === 'd M') {
                    $rawDate .= ' ' . date('Y');
                    $fmt      = 'd M Y';
                }
                $normalized = $this->normalizeMonth($rawDate);
                $dt         = \DateTime::createFromFormat($fmt, $normalized);
                if ($dt !== false && $this->isPlausibleDate($dt)) {
                    return $dt->getTimestamp() * 1000;
                }
            }
        }

        return null;
    }

    private function normalizeMonth(string $raw): string
    {
        $af = [
            'Jan' => 'Jan', 'Feb' => 'Feb', 'Mrt' => 'Mar', 'Mar' => 'Mar',
            'Apr' => 'Apr', 'Mei' => 'May', 'May' => 'May', 'Jun' => 'Jun',
            'Jul' => 'Jul', 'Aug' => 'Aug', 'Sep' => 'Sep', 'Okt' => 'Oct',
            'Oct' => 'Oct', 'Nov' => 'Nov', 'Des' => 'Dec', 'Dec' => 'Dec',
        ];
        return preg_replace_callback('/\b([A-Za-z]{3})\b/', function ($m) use ($af) {
            return $af[ucfirst(strtolower($m[1]))] ?? $m[0];
        }, $raw);
    }

    private function isPlausibleDate(\DateTime $dt): bool
    {
        $y = (int) $dt->format('Y');
        return $y >= 2010 && $y <= (int) date('Y') + 1;
    }

    // ── Category & direction helpers ──────────────────────────────────────────

    private function isIncome(string $desc): bool
    {
        $d = strtolower($desc);
        foreach ([
            'salary', 'salaris', 'loon', 'wages', 'payroll',
            'transfer in', 'tfr in', 'trfr in',
            'payment received', 'pay received',
            'deposit',
            'income',
            'interest earned', 'int earned', 'interest cr',
            'refund', 'cashback', 'cash back',
            'reimburs', 'reversal',
        ] as $kw) {
            if (str_contains($d, $kw)) return true;
        }
        return false;
    }

    private function guessCategory(string $desc): int
    {
        $d = strtolower($desc);

        $map = [
            1 => [
                'shoprite', 'checkers', 'pick n pay', 'pnp', 'spar', 'woolworths food',
                'woolies food', 'food lover', 'food lovers', 'fruit & veg', 'makro food',
                'kfc', 'mcdonald', 'steers', 'nandos', 'wimpy', 'debonairs', 'fishaways',
                'chicken licken', 'pizza hut', 'burger king', 'subway', 'tashas',
                'mugg & bean', 'ocean basket', 'spur', 'cattle baron', 'hungry lion',
                'food', 'grocery', 'groceries', 'restaurant', 'cafe', 'bakery',
                'butchery', 'deli', 'fresh produce', 'meal', 'takeaway', 'takeout',
                'coffee shop', 'coffee',
            ],
            2 => [
                'uber trip', 'uber', 'bolt taxi', 'bolt',
                'bp ', 'shell ', 'engen', 'caltex', 'sasol', 'total petrol', 'astron',
                'fuel station', 'fuel', 'petrol', 'diesel',
                'e-toll', 'etoll', 'sanral', 'toll',
                'parking payment', 'parking', 'parkade', 'wilson parking',
                'metrorail', 'prasa', 'myciti', 'bus ticket',
                'auto & general', 'car service', 'car wash',
                'transport', 'motor',
            ],
            3 => [
                'netflix', 'dstv', 'showmax', 'spotify', 'apple music', 'youtube premium',
                'amazon prime', 'disney+', 'disney plus', 'hbo',
                'ster kinekor', 'nu metro', 'cinema',
                'steam gaming', 'steam', 'playstation', 'xbox', 'nintendo',
                'ticketmaster', 'computicket', 'concert', 'theatre',
                'kindle',
            ],
            4 => [
                'clicks pharmacy', 'dischem', 'dis-chem', 'medirite',
                'doctor consultation', 'doctor', 'physician', 'specialist',
                'hospital', 'clinic', 'mediclinic', 'netcare', 'life healthcare',
                'dentist', 'dental', 'optometrist', 'vision',
                'pharmacy', 'script', 'medication', 'medicine',
                'discovery health', 'bonitas', 'momentum health',
                'physiotherapy', 'physio', 'medical centre', 'medical',
            ],
            5 => [
                'eskom', 'electricity', 'prepaid electricity', 'johannesburg water',
                'city of cape town', 'city of joburg', 'ethekwini', 'municipality',
                'water', 'rates and taxes', 'rates',
                'telkom', 'rain internet', 'rain ', 'vodacom', 'mtn ', 'cell c', 'cellc',
                'internet', 'broadband', 'fibre', 'adsl', 'lte',
                'gas', 'prepaid',
            ],
            6 => [
                'rent', 'rental payment', 'home loan', 'mortgage bond', 'mortgage',
                'body corporate', 'hoa fees', 'hoa ', 'levy payment', 'levy',
                'sectional title',
                'home insurance', 'building insurance',
                'maintenance', 'repairs', 'plumber', 'electrician',
                'furniture', 'appliance',
            ],
            7 => [
                'unisa tuition', 'unisa', 'school fees', 'school fee', 'tuition',
                'university', 'varsity', 'college',
                'online course', 'course', 'udemy', 'coursera', 'skillshare',
                'stationery', 'textbook', 'book store',
                'education', 'training',
            ],
            8 => [
                'woolworths fashion', 'woolworths', 'woolies',
                'mr price', 'mrp', 'edgars', 'jet stores',
                'foschini', 'tfg', 'totalsports', 'exact', 'markhams',
                'h&m', 'zara', 'ackermans', 'pep stores',
                'truworths', 'identity', 'sportscene',
                'clothing', 'apparel', 'fashion', 'shoes', 'sneakers',
            ],
            9 => [
                'transfer to savings', 'savings', 'fixed deposit',
                'sanlam investment', 'sanlam', 'old mutual', 'discovery invest',
                'allan gray', 'coronation', 'satrix', 'easy equities',
                'retirement', 'pension', 'provident', 'unit trust',
                'investment', 'invest',
            ],
        ];

        foreach ($map as $catId => $keywords) {
            foreach ($keywords as $kw) {
                if (str_contains($d, $kw)) return $catId;
            }
        }

        return 10;
    }

    private function cleanDesc(string $desc): string
    {
        $desc = preg_replace('/\s+/', ' ', trim($desc));
        return mb_substr($desc, 0, 120);
    }
}
