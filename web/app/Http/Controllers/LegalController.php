<?php

namespace App\Http\Controllers;

use App\Services\PolicyService;

class LegalController extends Controller
{
    public function terms()
    {
        $service  = new PolicyService();
        $versions = $service->getCurrentVersions();
        return view('legal.terms', [
            'firestoreContent' => $service->getContent('terms'),
            'version'          => $versions['terms_version'],
            'updatedAt'        => $versions['terms_updated_at'],
        ]);
    }

    public function privacy()
    {
        $service  = new PolicyService();
        $versions = $service->getCurrentVersions();
        return view('legal.privacy', [
            'firestoreContent' => $service->getContent('privacy'),
            'version'          => $versions['privacy_version'],
            'updatedAt'        => $versions['privacy_updated_at'],
        ]);
    }
}
