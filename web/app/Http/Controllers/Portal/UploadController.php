<?php

namespace App\Http\Controllers\Portal;

use App\Http\Controllers\Controller;
use App\Jobs\ProcessBankStatement;
use App\Models\StatementUpload;
use Illuminate\Http\Request;

class UploadController extends Controller
{
    public function show()
    {
        return view('portal.upload');
    }

    public function store(Request $request)
    {
        $request->validate([
            'statement'        => 'required|file|mimes:pdf|max:20480',
            'default_category' => 'nullable|integer|min:1|max:20',
        ]);

        $uid  = session('portal_user.uid');
        $file = $request->file('statement');

        $path = $file->store("statements/{$uid}");

        $upload = StatementUpload::create([
            'uid'              => $uid,
            'filename'         => $file->getClientOriginalName(),
            'path'             => $path,
            'status'           => 'pending',
            'default_category' => $request->default_category,
        ]);

        ProcessBankStatement::dispatch($upload->id, $uid);

        return redirect()->route('portal.dashboard')
            ->with('success', "Statement <strong>{$file->getClientOriginalName()}</strong> is queued for processing. Transactions will appear shortly.");
    }
}
