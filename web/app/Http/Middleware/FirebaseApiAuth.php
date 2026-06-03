<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Kreait\Firebase\Contract\Auth;
use Throwable;

class FirebaseApiAuth
{
    public function __construct(private Auth $auth) {}

    public function handle(Request $request, Closure $next): mixed
    {
        $token = $request->bearerToken();

        if (! $token) {
            return response()->json(['error' => 'Missing authorization token'], 401);
        }

        try {
            $verified = $this->auth->verifyIdToken($token);
            $uid = $verified->claims()->get('sub');
            $request->merge(['firebase_uid' => $uid]);
        } catch (Throwable $e) {
            return response()->json(['error' => 'Invalid or expired token'], 401);
        }

        return $next($request);
    }
}
