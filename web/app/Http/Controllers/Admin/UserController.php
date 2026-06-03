<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\FirestoreService;
use Illuminate\Http\Request;
use Kreait\Firebase\Contract\Auth;
use Kreait\Firebase\Exception\Auth\UserNotFound;
use Throwable;

class UserController extends Controller
{
    // Matches BudgetBuddyDatabase.Callback.onCreate seeding order (IDs 1–10)
    private const DEFAULT_CATEGORIES = [
        1  => ['id' => 1,  'icon' => '🛒', 'name' => 'Food & Groceries'],
        2  => ['id' => 2,  'icon' => '🚗', 'name' => 'Transport'],
        3  => ['id' => 3,  'icon' => '🎬', 'name' => 'Entertainment'],
        4  => ['id' => 4,  'icon' => '💊', 'name' => 'Healthcare'],
        5  => ['id' => 5,  'icon' => '💡', 'name' => 'Utilities'],
        6  => ['id' => 6,  'icon' => '🏠', 'name' => 'Housing'],
        7  => ['id' => 7,  'icon' => '📚', 'name' => 'Education'],
        8  => ['id' => 8,  'icon' => '👗', 'name' => 'Clothing'],
        9  => ['id' => 9,  'icon' => '💰', 'name' => 'Savings'],
        10 => ['id' => 10, 'icon' => '📦', 'name' => 'Other'],
    ];

    public static function badgeLabel(string $type): array
    {
        return match($type) {
            'FIRST_TRANSACTION', 'FIRST_STEP' => ['🎯', 'First Step'],
            'SEVEN_DAY_STREAK'               => ['🔥', '7-Day Streak'],
            'BUDGET_MASTER'                  => ['💰', 'Budget Master'],
            'DEBT_FREE'                      => ['🏆', 'Debt Free'],
            'GOAL_ACHIEVED'                  => ['⭐', 'Goal Achieved'],
            'THRIFTY_CHAMP'                  => ['🤑', 'Thrifty Champ'],
            'PERFECT_MONTH'                  => ['🗓️', 'Perfect Month'],
            'SAVINGS_STAR'                   => ['💫', 'Savings Star'],
            default                          => ['🏅', ucwords(strtolower(str_replace('_', ' ', $type)))],
        };
    }


    public function index(Request $request)
    {
        $search = $request->query('search', '');

        try {
            $auth = app(Auth::class);
            $users = collect();

            foreach ($auth->listUsers(1000) as $user) {
                $users->push([
                    'uid'          => $user->uid,
                    'email'        => $user->email,
                    'display_name' => $user->displayName,
                    'disabled'     => $user->disabled,
                    'created_at'   => $user->metadata->createdAt?->format('Y-m-d H:i'),
                    'last_sign_in' => $user->metadata->lastLoginAt?->format('Y-m-d H:i'),
                ]);
            }

            if ($search) {
                $users = $users->filter(fn ($u) =>
                    str_contains(strtolower($u['email'] ?? ''), strtolower($search)) ||
                    str_contains(strtolower($u['display_name'] ?? ''), strtolower($search))
                )->values();
            }

            $users = $users->sortByDesc('created_at')->values();
            $firebaseConfigured = true;
        } catch (Throwable) {
            $users = collect();
            $firebaseConfigured = false;
        }

        $perPage = 20;
        $page = (int) $request->query('page', 1);
        $total = $users->count();
        $paged = $users->forPage($page, $perPage)->values();

        return view('admin.users.index', [
            'users'              => $paged,
            'total'              => $total,
            'page'               => $page,
            'perPage'            => $perPage,
            'lastPage'           => max(1, (int) ceil($total / $perPage)),
            'search'             => $search,
            'firebaseConfigured' => $firebaseConfigured,
        ]);
    }

    public function show(string $uid)
    {
        try {
            $user = app(Auth::class)->getUser($uid);
        } catch (UserNotFound) {
            abort(404, 'User not found');
        } catch (Throwable $e) {
            return back()->with('error', 'Firebase error: ' . $e->getMessage());
        }

        try {
            $fs           = new FirestoreService();
            $profile      = $fs->getUserDocument($uid);
            $transactions = collect($fs->getCollection($uid, 'transactions'))->sortByDesc('date')->values();
            $budgets      = collect($fs->getCollection($uid, 'budgets'))->sortByDesc('createdAt')->values();
            $goals        = collect($fs->getCollection($uid, 'goals'))->sortByDesc('createdAt')->values();
            $debts        = collect($fs->getCollection($uid, 'debts'))->sortByDesc('createdAt')->values();
            $badges       = collect($fs->getCollection($uid, 'badges'))->sortByDesc('earnedAt')->values();

            // Merge seeded defaults (IDs 1–10) with user-synced custom categories
            $userCats = collect($fs->getCollection($uid, 'categories'))->keyBy('id')->all();
            $categories = collect(self::DEFAULT_CATEGORIES + $userCats);
        } catch (Throwable) {
            $profile = $transactions = $budgets = $goals = $debts = $badges = collect();
            $categories = collect();
        }

        // Totals for summary cards
        $totalIncome  = $transactions->where('type', 'INCOME')->sum('amount');
        $totalExpense = $transactions->where('type', 'EXPENSE')->sum('amount');
        $netBalance   = $totalIncome - $totalExpense;

        return view('admin.users.show', compact(
            'user', 'profile', 'transactions', 'budgets',
            'goals', 'debts', 'badges', 'categories',
            'totalIncome', 'totalExpense', 'netBalance'
        ));
    }

    public function disable(string $uid)
    {
        try {
            app(Auth::class)->disableUser($uid);
            return back()->with('success', 'User disabled.');
        } catch (Throwable $e) {
            return back()->with('error', $e->getMessage());
        }
    }

    public function enable(string $uid)
    {
        try {
            app(Auth::class)->enableUser($uid);
            return back()->with('success', 'User enabled.');
        } catch (Throwable $e) {
            return back()->with('error', $e->getMessage());
        }
    }

    public function resetPassword(string $uid)
    {
        try {
            $user = app(Auth::class)->getUser($uid);
            if ($user->email) {
                app(Auth::class)->sendPasswordResetLink($user->email);
                return back()->with('success', 'Password reset email sent.');
            }
            return back()->with('error', 'User has no email address.');
        } catch (Throwable $e) {
            return back()->with('error', $e->getMessage());
        }
    }

    public function destroy(string $uid)
    {
        try {
            app(Auth::class)->deleteUser($uid);
            return redirect()->route('admin.users.index')->with('success', 'User deleted.');
        } catch (Throwable $e) {
            return back()->with('error', $e->getMessage());
        }
    }

    public function export()
    {
        $rows = [['UID', 'Email', 'Display Name', 'Created At', 'Last Sign-In', 'Status']];

        try {
            foreach (app(Auth::class)->listUsers(1000) as $user) {
                $rows[] = [
                    $user->uid,
                    $user->email ?? '',
                    $user->displayName ?? '',
                    $user->metadata->createdAt?->format('Y-m-d H:i:s') ?? '',
                    $user->metadata->lastLoginAt?->format('Y-m-d H:i:s') ?? '',
                    $user->disabled ? 'disabled' : 'active',
                ];
            }
        } catch (Throwable) {}

        $csv = implode("\n", array_map(
            fn ($row) => implode(',', array_map(
                fn ($v) => '"' . str_replace('"', '""', $v) . '"', $row
            )),
            $rows
        ));

        return response($csv, 200, [
            'Content-Type'        => 'text/csv',
            'Content-Disposition' => 'attachment; filename="budgetbuddy-users-' . now()->format('Y-m-d') . '.csv"',
        ]);
    }
}
