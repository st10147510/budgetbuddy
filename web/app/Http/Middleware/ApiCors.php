<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

/**
 * Minimal CORS middleware for the mobile API.
 * Mobile clients don't send Origin headers, so this handles browser-based
 * developer tools and future web clients without opening up to any origin.
 */
class ApiCors
{
    private const ALLOWED_ORIGINS = [
        'https://thebudgetbuddy.co.za',
        'http://localhost:8000', // local dev only
        'http://10.0.2.2:8000', // Android emulator dev
    ];

    public function handle(Request $request, Closure $next): mixed
    {
        $origin = $request->header('Origin');

        if ($request->isMethod('OPTIONS')) {
            return response('', 204)
                ->header('Access-Control-Allow-Origin', $this->resolveOrigin($origin))
                ->header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
                ->header('Access-Control-Allow-Headers', 'Authorization, Content-Type, Accept')
                ->header('Access-Control-Max-Age', '86400');
        }

        $response = $next($request);

        $response->headers->set('Access-Control-Allow-Origin', $this->resolveOrigin($origin));
        $response->headers->set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
        $response->headers->set('Access-Control-Allow-Headers', 'Authorization, Content-Type, Accept');
        $response->headers->set('Vary', 'Origin');

        return $response;
    }

    private function resolveOrigin(?string $origin): string
    {
        if ($origin && in_array($origin, self::ALLOWED_ORIGINS, true)) {
            return $origin;
        }
        return 'null'; // deny unknown origins
    }
}
