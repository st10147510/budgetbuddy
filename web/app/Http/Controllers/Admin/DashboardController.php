<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\StatementUpload;
use App\Services\FirestoreService;
use Carbon\Carbon;
use Throwable;

class DashboardController extends Controller
{
    public function index()
    {
        try {
            $stats = $this->getStats();
            $firebaseConfigured = true;
        } catch (Throwable) {
            $stats = $this->emptyStats();
            $firebaseConfigured = false;
        }

        return view('admin.dashboard', compact('stats', 'firebaseConfigured'));
    }

    private function getStats(): array
    {
        $auth = app(\Kreait\Firebase\Contract\Auth::class);

        $weekAgoTs = now()->subWeek()->getTimestamp()  * 1000;
        $dayAgoTs  = now()->subDay()->getTimestamp()   * 1000;

        $total          = 0;
        $disabled       = 0;
        $newThisWeek    = 0;
        $activeToday    = 0;
        $activeThisWeek = 0;
        $userList       = [];

        foreach ($auth->listUsers(1000) as $user) {
            $total++;
            if ($user->disabled) $disabled++;

            $createdAtTs  = $user->metadata->createdAt
                ? (int) ($user->metadata->createdAt->format('U') * 1000) : null;
            $lastSignInTs = $user->metadata->lastLoginAt
                ? (int) ($user->metadata->lastLoginAt->format('U') * 1000) : null;

            if ($createdAtTs  !== null && $createdAtTs  >= $weekAgoTs) $newThisWeek++;
            if ($lastSignInTs !== null && $lastSignInTs >= $dayAgoTs)  $activeToday++;
            if ($lastSignInTs !== null && $lastSignInTs >= $weekAgoTs) $activeThisWeek++;

            $userList[] = [
                'uid'         => $user->uid,
                'email'       => $user->email ?? '—',
                'displayName' => $user->displayName,
                'createdAt'   => $createdAtTs,
                'lastSignIn'  => $lastSignInTs,
                'disabled'    => $user->disabled,
            ];
        }

        // Newest first
        usort($userList, fn ($a, $b) => ($b['createdAt'] ?? 0) <=> ($a['createdAt'] ?? 0));
        $recentSignups = array_slice($userList, 0, 5);

        // 14-day signup sparkline
        $signupsByDay = [];
        for ($i = 13; $i >= 0; $i--) {
            $signupsByDay[now()->subDays($i)->format('d M')] = 0;
        }
        foreach ($userList as $u) {
            if ($u['createdAt']) {
                $label = Carbon::createFromTimestampMs($u['createdAt'])->format('d M');
                if (array_key_exists($label, $signupsByDay)) {
                    $signupsByDay[$label]++;
                }
            }
        }

        // ── Firestore engagement (capped at 100 users to stay within timeout) ──
        $fs = new FirestoreService();
        $totalTransactions    = 0;
        $totalIncome          = 0.0;
        $totalExpense         = 0.0;
        $usersWithTx          = 0;
        $usersWithGoals       = 0;
        $usersWithBudgets     = 0;
        $usersWithDebts       = 0;

        foreach (array_slice($userList, 0, 100) as $u) {
            try {
                $txs = $fs->getCollection($u['uid'], 'transactions');
                if (!empty($txs)) {
                    $usersWithTx++;
                    $totalTransactions += count($txs);
                    foreach ($txs as $tx) {
                        if (($tx['type'] ?? '') === 'INCOME') {
                            $totalIncome  += $tx['amount'] ?? 0;
                        } else {
                            $totalExpense += $tx['amount'] ?? 0;
                        }
                    }
                }
            } catch (Throwable) {}

            try {
                if (!empty($fs->getCollection($u['uid'], 'goals')))   $usersWithGoals++;
            } catch (Throwable) {}

            try {
                if (!empty($fs->getCollection($u['uid'], 'budgets'))) $usersWithBudgets++;
            } catch (Throwable) {}

            try {
                if (!empty($fs->getCollection($u['uid'], 'debts')))   $usersWithDebts++;
            } catch (Throwable) {}
        }

        $avgTxPerUser = $usersWithTx > 0
            ? round($totalTransactions / $usersWithTx, 1) : 0;

        // ── Bank statement uploads (SQLite) ──────────────────────────────────
        $uploadStats = StatementUpload::selectRaw(
            'count(*) as total_uploads,
             count(distinct uid) as users_uploading,
             coalesce(sum(rows_imported), 0) as total_rows'
        )->first();

        return [
            'total_users'        => $total,
            'active_users'       => $total - $disabled,
            'disabled_users'     => $disabled,
            'new_this_week'      => $newThisWeek,
            'active_today'       => $activeToday,
            'active_this_week'   => $activeThisWeek,
            'total_transactions' => $totalTransactions,
            'total_income'       => $totalIncome,
            'total_expense'      => $totalExpense,
            'net_balance'        => $totalIncome - $totalExpense,
            'users_with_tx'      => $usersWithTx,
            'avg_tx_per_user'    => $avgTxPerUser,
            'users_with_goals'   => $usersWithGoals,
            'users_with_budgets' => $usersWithBudgets,
            'users_with_debts'   => $usersWithDebts,
            'upload_stats'       => $uploadStats,
            'recent_signups'     => $recentSignups,
            'signups_by_day'     => $signupsByDay,
        ];
    }

    private function emptyStats(): array
    {
        $signupsByDay = [];
        for ($i = 13; $i >= 0; $i--) {
            $signupsByDay[now()->subDays($i)->format('d M')] = 0;
        }

        $uploadStats = StatementUpload::selectRaw(
            'count(*) as total_uploads,
             count(distinct uid) as users_uploading,
             coalesce(sum(rows_imported), 0) as total_rows'
        )->first();

        return [
            'total_users' => 0, 'active_users' => 0, 'disabled_users' => 0,
            'new_this_week' => 0, 'active_today' => 0, 'active_this_week' => 0,
            'total_transactions' => 0, 'total_income' => 0, 'total_expense' => 0, 'net_balance' => 0,
            'users_with_tx' => 0, 'avg_tx_per_user' => 0,
            'users_with_goals' => 0, 'users_with_budgets' => 0, 'users_with_debts' => 0,
            'upload_stats' => $uploadStats,
            'recent_signups' => [],
            'signups_by_day' => $signupsByDay,
        ];
    }
}
