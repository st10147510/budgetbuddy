<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class SecurityHeaders
{
    public function handle(Request $request, Closure $next): mixed
    {
        $response = $next($request);

        $response->headers->set('X-Frame-Options', 'SAMEORIGIN');
        $response->headers->set('X-Content-Type-Options', 'nosniff');
        $response->headers->set('X-XSS-Protection', '0'); // modern browsers use CSP instead
        $response->headers->set('Referrer-Policy', 'strict-origin-when-cross-origin');
        $response->headers->set('Permissions-Policy', 'camera=(), microphone=(), geolocation=(), interest-cohort=()');

        // HSTS — only send over HTTPS to avoid breaking local dev
        if ($request->isSecure()) {
            $response->headers->set(
                'Strict-Transport-Security',
                'max-age=63072000; includeSubDomains; preload'
            );
        }

        // Content-Security-Policy
        // - 'unsafe-inline' required for Tailwind JIT and Bootstrap inline styles
        // - CDN allowlist covers Bootstrap Icons, Google Fonts, Chart.js, Swagger UI
        $csp = implode(' ', [
            "default-src 'self';",
            "script-src 'self' 'unsafe-inline' cdn.jsdelivr.net cdnjs.cloudflare.com unpkg.com;",
            "style-src 'self' 'unsafe-inline' fonts.googleapis.com cdn.jsdelivr.net cdnjs.cloudflare.com unpkg.com;",
            "font-src 'self' data: fonts.gstatic.com cdn.jsdelivr.net;",
            "img-src 'self' data: blob: https:;",
            "connect-src 'self';",
            "frame-ancestors 'none';",
            "base-uri 'self';",
            "form-action 'self';",
        ]);
        $response->headers->set('Content-Security-Policy', $csp);

        // Remove server fingerprinting headers
        // ini_set suppresses the PHP-level X-Powered-By before the SAPI emits it;
        // the ->remove() call covers any copy Laravel added to the response object.
        ini_set('expose_php', '0');
        header_remove('X-Powered-By');
        $response->headers->remove('X-Powered-By');
        $response->headers->remove('Server');

        return $response;
    }
}
