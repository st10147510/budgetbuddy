<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\FirestoreService;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Pagination\LengthAwarePaginator;
use Kreait\Firebase\Contract\Auth;
use Throwable;

class TransactionController extends Controller
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

    public function index(Request $request)
    {
        $auth    = app(Auth::class);
        $uids    = [];
        $userMap = [];

        foreach ($auth->listUsers(1000) as $user) {
            $uids[]              = $user->uid;
            $userMap[$user->uid] = $user->email ?? $user->uid;
        }

        $fs  = new FirestoreService();
        $all = collect($fs->getAllUsersCollection($uids, 'transactions'));

        // ── Filters ───────────────────────────────────────────────────────
        if ($uid = $request->get('uid')) {
            $all = $all->where('_uid', $uid);
        }
        if ($type = $request->get('type')) {
            $all = $all->where('type', strtoupper($type));
        }
        if ($catId = $request->get('category')) {
            $all = $all->where('categoryId', (int) $catId);
        }
        if ($search = $request->get('search')) {
            $all = $all->filter(fn ($tx) => str_contains(
                strtolower($tx['notes'] ?? ''),
                strtolower($search)
            ));
        }

        $all = $all->sortByDesc('date')->values();

        // ── Paginate in PHP ───────────────────────────────────────────────
        $perPage = 30;
        $page    = max(1, (int) $request->get('page', 1));
        $total   = $all->count();
        $items   = $all->forPage($page, $perPage);

        $paginator = new LengthAwarePaginator($items, $total, $perPage, $page, [
            'path'  => $request->url(),
            'query' => $request->except('page'),
        ]);

        $categories = self::DEFAULT_CATEGORIES;

        return view('admin.transactions.index', compact(
            'paginator', 'userMap', 'uids', 'categories',
        ));
    }
}
