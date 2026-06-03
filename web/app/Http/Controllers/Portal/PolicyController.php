<?php

namespace App\Http\Controllers\Portal;

use App\Http\Controllers\Controller;
use App\Services\PolicyService;
use Illuminate\Http\Request;

class PolicyController extends Controller
{
    public function show()
    {
        $service  = new PolicyService();
        $versions = $service->getCurrentVersions();
        return view('portal.accept-policies', compact('versions'));
    }

    public function accept(Request $request)
    {
        $uid = session('portal_user.uid');
        if (! $uid) {
            return redirect()->route('portal.login');
        }

        $service = new PolicyService();
        $service->recordAllAcceptances($uid, 'web');

        return redirect()->route('portal.dashboard')->with('success', 'Thank you for accepting our policies.');
    }
}
