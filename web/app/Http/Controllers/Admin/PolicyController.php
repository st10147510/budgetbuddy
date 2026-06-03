<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\PolicyService;
use Illuminate\Http\Request;

class PolicyController extends Controller
{
    public function index()
    {
        $service = new PolicyService();
        $versions = $service->getCurrentVersions();
        return view('admin.policies.index', [
            'versions'       => $versions,
            'termsContent'   => $service->getContent('terms'),
            'privacyContent' => $service->getContent('privacy'),
        ]);
    }

    public function update(Request $request)
    {
        $request->validate([
            'type'    => 'required|in:terms,privacy',
            'version' => 'required|regex:/^\d+\.\d+$/',
            'content' => 'nullable|string|max:200000',
        ]);

        $service = new PolicyService();
        $service->updateVersion($request->type, $request->version, $request->input('content', ''));

        $label = $request->type === 'terms' ? 'Terms & Conditions' : 'Privacy Policy';
        return back()->with('success', "{$label} saved as v{$request->version} in Firestore. All users will be prompted to re-accept.");
    }
}
