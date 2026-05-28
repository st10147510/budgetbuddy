<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use Throwable;

class DashboardController extends Controller
{
    public function index()
    {
        try {
            $stats = $this->getStats();
            $firebaseConfigured = true;
        } catch (Throwable) {
            $stats = ['total_users' => 0, 'active_users' => 0, 'disabled_users' => 0, 'new_this_week' => 0];
            $firebaseConfigured = false;
        }

        return view('admin.dashboard', compact('stats', 'firebaseConfigured'));
    }

    private function getStats(): array
    {
        $auth = app(\Kreait\Firebase\Contract\Auth::class);
        $users = $auth->listUsers(1000);

        $total = 0;
        $disabled = 0;
        $newThisWeek = 0;
        $weekAgo = now()->subWeek()->getTimestamp() * 1000;

        foreach ($users as $user) {
            $total++;
            if ($user->disabled) $disabled++;
            $createdAt = $user->metadata->createdAt;
            if ($createdAt && $createdAt->format('U') * 1000 >= $weekAgo) $newThisWeek++;
        }

        return [
            'total_users'    => $total,
            'active_users'   => $total - $disabled,
            'disabled_users' => $disabled,
            'new_this_week'  => $newThisWeek,
        ];
    }
}
