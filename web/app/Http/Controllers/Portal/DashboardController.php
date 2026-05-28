<?php

namespace App\Http\Controllers\Portal;

use App\Http\Controllers\Controller;
use App\Models\StatementUpload;
use App\Services\FirestoreService;
use Throwable;

class DashboardController extends Controller
{
    // Default categories matching Android Room seed (IDs 1-10)
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

    public function index()
    {
        $uid = session('portal_user.uid');

        try {
            $fs           = new FirestoreService();
            $transactions = collect($fs->getCollection($uid, 'transactions'))->sortByDesc('date')->values();
            $goals        = collect($fs->getCollection($uid, 'goals'))->sortByDesc('createdAt')->values();
            $userCats     = collect($fs->getCollection($uid, 'categories'))->keyBy('id')->all();
            $categories   = collect(self::DEFAULT_CATEGORIES + $userCats);
        } catch (Throwable) {
            $transactions = collect();
            $goals        = collect();
            $categories   = collect(self::DEFAULT_CATEGORIES);
        }

        // All-time totals
        $totalIncome  = $transactions->where('type', 'INCOME')->sum('amount');
        $totalExpense = $transactions->where('type', 'EXPENSE')->sum('amount');
        $netBalance   = $totalIncome - $totalExpense;

        // Current-month totals (millisecond timestamps match Android DateUtils)
        $monthStart = (int) (now()->startOfMonth()->timestamp * 1000);
        $monthEnd   = (int) (now()->endOfMonth()->timestamp * 1000);
        $monthTx        = $transactions->whereBetween('date', [$monthStart, $monthEnd]);
        $monthlyIncome  = $monthTx->where('type', 'INCOME')->sum('amount');
        $monthlyExpense = $monthTx->where('type', 'EXPENSE')->sum('amount');
        $monthlyNet     = $monthlyIncome - $monthlyExpense;
        $monthLabel     = now()->format('F Y');

        $jobs = StatementUpload::where('uid', $uid)
            ->orderByDesc('created_at')
            ->limit(5)
            ->get();

        return view('portal.dashboard', compact(
            'transactions', 'goals', 'categories',
            'totalIncome', 'totalExpense', 'netBalance',
            'monthlyIncome', 'monthlyExpense', 'monthlyNet', 'monthLabel',
            'jobs',
        ));
    }
}
