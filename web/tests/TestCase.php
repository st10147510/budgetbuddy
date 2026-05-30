<?php

namespace Tests;

use App\Http\Middleware\FirebaseApiAuth;
use Illuminate\Foundation\Testing\TestCase as BaseTestCase;

abstract class TestCase extends BaseTestCase
{
    /**
     * Bypass Firebase token verification and inject a fake UID into the request.
     * Call this instead of withoutMiddleware() so $request->firebase_uid is set.
     */
    protected function withFakeFirebaseAuth(string $uid = 'test-uid-123'): static
    {
        $this->app->instance(FirebaseApiAuth::class, new class($uid) {
            public function __construct(private string $uid) {}
            public function handle($request, $next): mixed
            {
                $request->merge(['firebase_uid' => $this->uid]);
                return $next($request);
            }
        });

        return $this;
    }
}
