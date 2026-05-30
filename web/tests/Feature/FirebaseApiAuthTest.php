<?php

namespace Tests\Feature;

use App\Http\Middleware\FirebaseApiAuth;
use Kreait\Firebase\Contract\Auth;
use Lcobucci\JWT\Token\Plain;
use Mockery;
use Tests\TestCase;

class FirebaseApiAuthTest extends TestCase
{
    protected function tearDown(): void
    {
        Mockery::close();
        parent::tearDown();
    }

    // ── No token ───────────────────────────────────────────────────────────────

    public function test_missing_bearer_token_returns_401(): void
    {
        $response = $this->getJson('/api/v1/policies/status');

        $response->assertStatus(401);
        $response->assertJson(['error' => 'Missing authorization token']);
    }

    public function test_missing_token_on_accept_endpoint_returns_401(): void
    {
        $response = $this->postJson('/api/v1/policies/accept', ['type' => 'terms']);

        $response->assertStatus(401);
    }

    // ── Invalid / expired token ────────────────────────────────────────────────

    public function test_invalid_token_returns_401_with_message(): void
    {
        $auth = Mockery::mock(Auth::class);
        $auth->shouldReceive('verifyIdToken')
             ->andThrow(new \Kreait\Firebase\Exception\Auth\FailedToVerifyToken('bad token'));
        $this->app->instance(Auth::class, $auth);

        $response = $this->withToken('invalid.token.here')
                         ->getJson('/api/v1/policies/status');

        $response->assertStatus(401);
        $response->assertJson(['error' => 'Invalid or expired token']);
    }

    // ── Valid token ────────────────────────────────────────────────────────────

    public function test_valid_token_injects_firebase_uid_into_request(): void
    {
        // Use withFakeFirebaseAuth to bypass real Firebase verification
        // and confirm the UID flows into the controller.
        $this->withFakeFirebaseAuth('my-uid-456');

        // Mock FirestoreService so the controller doesn't try real Firestore
        $fs = Mockery::mock(\App\Services\FirestoreService::class);
        $fs->shouldReceive('getGlobalDocument')->andReturn([
            'terms_version' => '1.0', 'privacy_version' => '1.0',
        ]);
        $fs->shouldReceive('getDocumentInCollection')->andReturn([]);
        $this->app->instance(\App\Services\FirestoreService::class, $fs);

        $response = $this->getJson('/api/v1/policies/status');

        $response->assertStatus(200);
        // If UID was not injected the controller would fail; 200 proves it was.
    }

    // ── Public endpoints bypass middleware ─────────────────────────────────────

    public function test_public_current_endpoint_does_not_require_token(): void
    {
        $fs = Mockery::mock(\App\Services\FirestoreService::class);
        $fs->shouldReceive('getGlobalDocument')->andReturn([
            'terms_version'   => '1.0', 'privacy_version'   => '1.0',
            'terms_content'   => '',    'privacy_content'   => '',
        ]);
        $this->app->instance(\App\Services\FirestoreService::class, $fs);

        $response = $this->getJson('/api/v1/policies/current');

        $response->assertStatus(200);
    }

    // ── Throttle smoke-test ────────────────────────────────────────────────────

    public function test_401_response_is_json_content_type(): void
    {
        $response = $this->getJson('/api/v1/policies/status');

        $response->assertStatus(401);
        $this->assertStringContainsString('application/json', $response->headers->get('Content-Type'));
    }
}
