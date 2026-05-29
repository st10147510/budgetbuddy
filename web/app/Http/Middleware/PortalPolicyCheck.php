<?php

namespace App\Http\Middleware;

use App\Services\PolicyService;
use Closure;
use Illuminate\Http\Request;

class PortalPolicyCheck
{
    public function handle(Request $request, Closure $next): mixed
    {
        $user = session('portal_user');
        if (! $user) {
            return $next($request);
        }

        // Skip the check on the accept page itself to avoid a redirect loop
        if ($request->routeIs('portal.policies.accept') || $request->routeIs('portal.policies.show')) {
            return $next($request);
        }

        try {
            $service = new PolicyService();
            if (! $service->hasUserAcceptedAll($user['uid'])) {
                return redirect()->route('portal.policies.accept');
            }
        } catch (\Throwable) {
            // If Firestore is unreachable, don't block the user
        }

        return $next($request);
    }
}
