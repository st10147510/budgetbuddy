<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Jobs\ProcessBankStatement;
use App\Models\StatementUpload;
use Illuminate\Http\Request;
use Kreait\Firebase\Contract\Auth;
use Throwable;

class UploadMonitorController extends Controller
{
    public function index(Request $request)
    {
        $status = $request->get('status');

        $query = StatementUpload::orderByDesc('created_at');
        if ($status) {
            $query->where('status', $status);
        }
        $jobs = $query->paginate(25)->withQueryString();

        // Resolve user emails from Firebase Auth (batch by unique UIDs on this page)
        $auth     = app(Auth::class);
        $emailMap = [];
        foreach ($jobs->pluck('uid')->unique() as $uid) {
            try {
                $u = $auth->getUser($uid);
                $emailMap[$uid] = $u->email ?? $uid;
            } catch (Throwable) {
                $emailMap[$uid] = $uid;
            }
        }

        $statusCounts = StatementUpload::selectRaw('status, count(*) as cnt')
            ->groupBy('status')
            ->pluck('cnt', 'status');

        return view('admin.uploads.index', compact('jobs', 'emailMap', 'status', 'statusCounts'));
    }

    public function retry(int $id)
    {
        $upload = StatementUpload::findOrFail($id);
        $upload->update(['status' => 'pending', 'error' => null, 'rows_imported' => 0]);
        ProcessBankStatement::dispatch($upload->id, $upload->uid);

        return back()->with('success', "Job #{$id} requeued. Run php artisan queue:work if not already running.");
    }
}
