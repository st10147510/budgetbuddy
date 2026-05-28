<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\FirestoreService;
use Carbon\Carbon;
use Kreait\Firebase\Contract\Auth;
use Throwable;

class InsightsController extends Controller
{
    private const DEFAULT_CATEGORIES = [
        1  => ['icon' => '🛒', 'name' => 'Food & Groceries'],
        2  => ['icon' => '🚗', 'name' => 'Transport'],
        3  => ['icon' => '🎬', 'name' => 'Entertainment'],
        4  => ['icon' => '💊', 'name' => 'Healthcare'],
        5  => ['icon' => '💡', 'name' => 'Utilities'],
        6  => ['icon' => '🏠', 'name' => 'Housing'],
        7  => ['icon' => '📚', 'name' => 'Education'],
        8  => ['icon' => '👗', 'name' => 'Clothing'],
        9  => ['icon' => '💰', 'name' => 'Savings'],
        10 => ['icon' => '📦', 'name' => 'Other'],
    ];

    public function index()
    {
        $auth     = app(Auth::class);
        $uids     = [];
        $userMap  = [];

        foreach ($auth->listUsers(1000) as $user) {
            $uids[]              = $user->uid;
            $userMap[$user->uid] = $user->email ?? $user->uid;
        }

        $fs     = new FirestoreService();
        $allTxs = $fs->getAllUsersCollection($uids, 'transactions');

        // ── Category spend (expenses only) ────────────────────────────────
        $catSpend = [];
        foreach ($allTxs as $tx) {
            if (($tx['type'] ?? '') !== 'EXPENSE') continue;
            $catId = $tx['categoryId'] ?? 10;
            $catSpend[$catId] = ($catSpend[$catId] ?? 0.0) + ($tx['amount'] ?? 0);
        }
        arsort($catSpend);
        $catSpend = array_slice($catSpend, 0, 8, true); // top 8

        // ── User health distribution ──────────────────────────────────────
        $userNet = [];
        foreach ($allTxs as $tx) {
            $uid = $tx['_uid'];
            $amt = $tx['amount'] ?? 0;
            if (($tx['type'] ?? '') === 'INCOME') {
                $userNet[$uid] = ($userNet[$uid] ?? 0.0) + $amt;
            } else {
                $userNet[$uid] = ($userNet[$uid] ?? 0.0) - $amt;
            }
        }
        $surplus    = count(array_filter($userNet, fn ($v) => $v >  0));
        $deficit    = count(array_filter($userNet, fn ($v) => $v <  0));
        $breakeven  = count(array_filter($userNet, fn ($v) => $v == 0));
        $noActivity = max(0, count($uids) - count($userNet));

        // ── Monthly transaction volume (last 6 months) ────────────────────
        $monthlyVolume = [];
        for ($i = 5; $i >= 0; $i--) {
            $monthlyVolume[now()->subMonths($i)->format('M Y')] = ['count' => 0, 'income' => 0.0, 'expense' => 0.0];
        }
        foreach ($allTxs as $tx) {
            if (!isset($tx['date'])) continue;
            try {
                $label = Carbon::createFromTimestampMs($tx['date'])->format('M Y');
            } catch (Throwable) {
                continue;
            }
            if (!isset($monthlyVolume[$label])) continue;
            $monthlyVolume[$label]['count']++;
            if (($tx['type'] ?? '') === 'INCOME') {
                $monthlyVolume[$label]['income']  += $tx['amount'] ?? 0;
            } else {
                $monthlyVolume[$label]['expense'] += $tx['amount'] ?? 0;
            }
        }

        // ── Income vs expense per user (for top-spenders table) ───────────
        $userStats = [];
        foreach ($userNet as $uid => $net) {
            $inc = 0; $exp = 0; $cnt = 0;
            foreach ($allTxs as $tx) {
                if ($tx['_uid'] !== $uid) continue;
                $cnt++;
                if (($tx['type'] ?? '') === 'INCOME') $inc += $tx['amount'] ?? 0;
                else                                  $exp += $tx['amount'] ?? 0;
            }
            $userStats[$uid] = [
                'email'   => $userMap[$uid] ?? $uid,
                'net'     => $net,
                'income'  => $inc,
                'expense' => $exp,
                'txCount' => $cnt,
            ];
        }
        uasort($userStats, fn ($a, $b) => $b['txCount'] <=> $a['txCount']);
        $topUsers = array_slice($userStats, 0, 10);

        return view('admin.insights.index', compact(
            'catSpend', 'surplus', 'deficit', 'breakeven', 'noActivity',
            'monthlyVolume', 'topUsers'
        ));
    }

    public static function categoryName(int $id): string
    {
        return self::DEFAULT_CATEGORIES[$id]['icon'] . ' ' . (self::DEFAULT_CATEGORIES[$id]['name'] ?? 'Other');
    }
}
