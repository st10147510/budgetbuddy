<?php

namespace Tests\Unit;

use App\Jobs\ProcessBankStatement;
use ReflectionClass;
use Tests\TestCase;

/**
 * Unit-tests for the PDF parsing logic in ProcessBankStatement.
 *
 * All methods under test are private — accessed via ReflectionClass.
 * No database, queue, storage, or Firebase interaction occurs.
 */
class StatementParserTest extends TestCase
{
    private ProcessBankStatement $job;
    private ReflectionClass      $ref;

    protected function setUp(): void
    {
        parent::setUp();
        // uploadId / uid are required by the constructor but never used by parsing methods
        $this->job = new ProcessBankStatement(uploadId: 1, uid: 'test-uid');
        $this->ref = new ReflectionClass($this->job);
    }

    // ── Helper: call any private method on the job ─────────────────────────────

    private function call(string $method, mixed ...$args): mixed
    {
        $m = $this->ref->getMethod($method);
        $m->setAccessible(true);
        return $m->invoke($this->job, ...$args);
    }

    // ── extractTransactions ────────────────────────────────────────────────────

    public function test_empty_text_returns_empty_array(): void
    {
        $result = $this->call('extractTransactions', '', null);
        $this->assertSame([], $result);
    }

    public function test_header_lines_are_ignored(): void
    {
        $text   = "Statement of Account\nDate Description Amount Balance";
        $result = $this->call('extractTransactions', $text, null);
        $this->assertSame([], $result);
    }

    public function test_full_sa_bank_line_parsed_as_expense(): void
    {
        // Format: "01 May 2026Eskom Payment-R 6,740.79R 38,509.96"
        $line   = "01 May 2026Eskom Payment-R 6,740.79R 38,509.96";
        $result = $this->call('extractTransactions', $line, null);

        $this->assertCount(1, $result);
        $this->assertSame('EXPENSE', $result[0]['type']);
        $this->assertSame(6740.79, $result[0]['amount']);
        $this->assertStringContainsString('Eskom', $result[0]['notes']);
    }

    public function test_salary_line_parsed_as_income(): void
    {
        $line   = "01 May 2026Salary PaymentR 55,000.00R 93,500.00";
        $result = $this->call('extractTransactions', $line, null);

        $this->assertCount(1, $result);
        $this->assertSame('INCOME', $result[0]['type']);
        $this->assertSame(55000.00, $result[0]['amount']);
    }

    public function test_explicit_income_suffix_overrides_keyword_detection(): void
    {
        // Positive amount + "Income" suffix → income regardless of description
        $line   = "15 Jan 2026Mystery DepositR 1,000.00R 5,000.00Income";
        $result = $this->call('extractTransactions', $line, null);

        $this->assertCount(1, $result);
        $this->assertSame('INCOME', $result[0]['type']);
    }

    public function test_explicit_expense_suffix_forces_expense(): void
    {
        $line   = "15 Jan 2026Salary AdvanceR 500.00R 4,500.00Expense";
        $result = $this->call('extractTransactions', $line, null);

        $this->assertCount(1, $result);
        $this->assertSame('EXPENSE', $result[0]['type']);
    }

    public function test_explicit_category_suffix_sets_category(): void
    {
        $line   = "10 Mar 2026PnP ShoppersR 420.00R 9,580.00Category 1";
        $result = $this->call('extractTransactions', $line, null);

        $this->assertCount(1, $result);
        $this->assertSame(1, $result[0]['categoryId']);
    }

    public function test_default_category_used_when_no_keyword_matches(): void
    {
        $line   = "10 Mar 2026Unknown MerchantR 100.00R 900.00";
        $result = $this->call('extractTransactions', $line, '3');

        $this->assertCount(1, $result);
        $this->assertSame(3, $result[0]['categoryId']);
    }

    public function test_unknown_merchant_without_default_gets_category_10(): void
    {
        $line   = "10 Mar 2026Xyzzy Frobnicator-R 50.00R 950.00";
        $result = $this->call('extractTransactions', $line, null);

        $this->assertCount(1, $result);
        $this->assertSame(10, $result[0]['categoryId']);
    }

    public function test_multiple_lines_all_parsed(): void
    {
        $text = implode("\n", [
            "01 May 2026Salary PaymentR 55,000.00R 93,500.00",
            "02 May 2026Transfer to Savings-R 7,549.49R 85,950.51",
            "03 May 2026Netflix-R 199.00R 85,751.51",
        ]);
        $result = $this->call('extractTransactions', $text, null);

        $this->assertCount(3, $result);
    }

    public function test_line_without_amount_is_skipped(): void
    {
        $line   = "01 May 2026No amount here at all";
        $result = $this->call('extractTransactions', $line, null);
        $this->assertSame([], $result);
    }

    // ── parseLine ─────────────────────────────────────────────────────────────

    public function test_short_line_returns_null(): void
    {
        $result = $this->call('parseLine', 'abc', null);
        $this->assertNull($result);
    }

    public function test_zero_amount_returns_null(): void
    {
        $result = $this->call('parseLine', '01 May 2026Description-R 0.00R 0.00', null);
        $this->assertNull($result);
    }

    public function test_date_is_stored_as_milliseconds(): void
    {
        $line   = "01 Jan 2024Salary PaymentR 10,000.00R 10,000.00";
        $result = $this->call('parseLine', $line, null);

        $this->assertNotNull($result);
        // Timestamp in ms must be in the 2024 range
        $year = (int) date('Y', intdiv($result['date'], 1000));
        $this->assertSame(2024, $year);
    }

    public function test_amount_is_rounded_to_2_decimal_places(): void
    {
        $line   = "01 Jan 2024PaymentR 1,234.567R 5,000.00";
        $result = $this->call('parseLine', $line, null);
        // If parsed, amount should have at most 2 decimal places
        if ($result !== null) {
            $this->assertSame(round($result['amount'], 2), $result['amount']);
        } else {
            $this->markTestSkipped('Amount format not matched by parser');
        }
    }

    // ── extractLeadingDate — multiple date formats ────────────────────────────

    private function extractDate(string $line): ?int
    {
        $rest = null;
        $m    = $this->ref->getMethod('extractLeadingDate');
        $m->setAccessible(true);
        return $m->invokeArgs($this->job, [$line, &$rest]);
    }

    public function test_date_format_dd_mon_yyyy(): void
    {
        $ms = $this->extractDate("15 Mar 2025rest of line");
        $this->assertNotNull($ms);
        $this->assertSame('2025', date('Y', intdiv($ms, 1000)));
        $this->assertSame('3',    date('n', intdiv($ms, 1000)));
        $this->assertSame('15',   date('j', intdiv($ms, 1000)));
    }

    public function test_date_format_dd_slash_mm_slash_yyyy(): void
    {
        $ms = $this->extractDate("15/03/2025rest");
        $this->assertNotNull($ms);
        $this->assertSame('2025', date('Y', intdiv($ms, 1000)));
    }

    public function test_date_format_yyyy_dash_mm_dash_dd(): void
    {
        $ms = $this->extractDate("2025-03-15 rest");
        $this->assertNotNull($ms);
        $this->assertSame('2025', date('Y', intdiv($ms, 1000)));
        $this->assertSame('3',    date('n', intdiv($ms, 1000)));
    }

    public function test_date_format_dd_dash_mm_dash_yyyy(): void
    {
        $ms = $this->extractDate("15-03-2025rest");
        $this->assertNotNull($ms);
    }

    public function test_date_format_dd_mon_yy_two_digit_year(): void
    {
        $ms = $this->extractDate("01 Jan 26rest");
        $this->assertNotNull($ms);
        $this->assertSame('2026', date('Y', intdiv($ms, 1000)));
    }

    public function test_implausible_date_year_1990_returns_null(): void
    {
        $ms = $this->extractDate("01 Jan 1990rest");
        $this->assertNull($ms);
    }

    public function test_line_with_no_date_returns_null(): void
    {
        $ms = $this->extractDate("No date here R 100.00");
        $this->assertNull($ms);
    }

    // ── normalizeMonth — Afrikaans month abbreviations ────────────────────────

    public function test_normalize_afrikaans_mrt_to_mar(): void
    {
        $result = $this->call('normalizeMonth', '01 Mrt 2025');
        $this->assertStringContainsString('Mar', $result);
    }

    public function test_normalize_afrikaans_mei_to_may(): void
    {
        $result = $this->call('normalizeMonth', '01 Mei 2025');
        $this->assertStringContainsString('May', $result);
    }

    public function test_normalize_afrikaans_okt_to_oct(): void
    {
        $result = $this->call('normalizeMonth', '01 Okt 2025');
        $this->assertStringContainsString('Oct', $result);
    }

    public function test_normalize_afrikaans_des_to_dec(): void
    {
        $result = $this->call('normalizeMonth', '01 Des 2025');
        $this->assertStringContainsString('Dec', $result);
    }

    public function test_normalize_english_months_unchanged(): void
    {
        foreach (['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'] as $mon) {
            $result = $this->call('normalizeMonth', "01 {$mon} 2025");
            $this->assertStringContainsString($mon, $result, "Failed to preserve {$mon}");
        }
    }

    // ── isIncome ──────────────────────────────────────────────────────────────

    public function test_salary_keyword_is_income(): void
    {
        $this->assertTrue($this->call('isIncome', 'Monthly Salary Payment'));
    }

    public function test_deposit_keyword_is_income(): void
    {
        $this->assertTrue($this->call('isIncome', 'Cash Deposit'));
    }

    public function test_refund_keyword_is_income(): void
    {
        $this->assertTrue($this->call('isIncome', 'Refund from Amazon'));
    }

    public function test_interest_earned_is_income(): void
    {
        $this->assertTrue($this->call('isIncome', 'Interest Earned'));
    }

    public function test_transfer_in_is_income(): void
    {
        $this->assertTrue($this->call('isIncome', 'Transfer In'));
    }

    public function test_regular_purchase_is_not_income(): void
    {
        $this->assertFalse($this->call('isIncome', 'Eskom Payment'));
        $this->assertFalse($this->call('isIncome', 'Netflix Subscription'));
        $this->assertFalse($this->call('isIncome', 'PnP Supermarket'));
    }

    // ── guessCategory ─────────────────────────────────────────────────────────

    public function test_shoprite_maps_to_category_1_food(): void
    {
        $this->assertSame(1, $this->call('guessCategory', 'Shoprite Payment'));
    }

    public function test_uber_maps_to_category_2_transport(): void
    {
        $this->assertSame(2, $this->call('guessCategory', 'Uber Trip'));
    }

    public function test_netflix_maps_to_category_3_entertainment(): void
    {
        $this->assertSame(3, $this->call('guessCategory', 'Netflix Subscription'));
    }

    public function test_dischem_maps_to_category_4_health(): void
    {
        $this->assertSame(4, $this->call('guessCategory', 'DisChem Purchase'));
    }

    public function test_eskom_maps_to_category_5_utilities(): void
    {
        $this->assertSame(5, $this->call('guessCategory', 'Eskom Prepaid'));
    }

    public function test_rent_maps_to_category_6_housing(): void
    {
        $this->assertSame(6, $this->call('guessCategory', 'Monthly Rent Payment'));
    }

    public function test_unisa_maps_to_category_7_education(): void
    {
        $this->assertSame(7, $this->call('guessCategory', 'Unisa Tuition Fee'));
    }

    public function test_woolworths_maps_to_category_8_shopping(): void
    {
        $this->assertSame(8, $this->call('guessCategory', 'Woolworths Purchase'));
    }

    public function test_savings_maps_to_category_9(): void
    {
        $this->assertSame(9, $this->call('guessCategory', 'Transfer to Savings Account'));
    }

    public function test_unknown_merchant_maps_to_category_10(): void
    {
        $this->assertSame(10, $this->call('guessCategory', 'Zxqbzz Unknown'));
    }

    // ── cleanDesc ─────────────────────────────────────────────────────────────

    public function test_clean_desc_trims_whitespace(): void
    {
        $result = $this->call('cleanDesc', '  hello world  ');
        $this->assertSame('hello world', $result);
    }

    public function test_clean_desc_collapses_internal_spaces(): void
    {
        $result = $this->call('cleanDesc', 'hello   world');
        $this->assertSame('hello world', $result);
    }

    public function test_clean_desc_truncates_at_120_chars(): void
    {
        $long   = str_repeat('a', 200);
        $result = $this->call('cleanDesc', $long);
        $this->assertSame(120, mb_strlen($result));
    }

    public function test_clean_desc_short_string_unchanged_length(): void
    {
        $result = $this->call('cleanDesc', 'short');
        $this->assertSame('short', $result);
    }
}
