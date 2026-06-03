<?php

namespace Tests\Feature;

use App\Services\FirestoreService;
use App\Services\PolicyService;
use Mockery;
use Tests\TestCase;

class PolicyApiTest extends TestCase
{
    private const UID = 'test-uid-abc';

    protected function setUp(): void
    {
        parent::setUp();

        // Bind a fake FirestoreService so PolicyService never hits the network
        $fakeFirestore = Mockery::mock(FirestoreService::class);
        $fakeFirestore->shouldReceive('getGlobalDocument')
            ->andReturn([
                'terms_version'   => '1.0',
                'privacy_version' => '1.0',
                'terms_content'   => '<p>Terms content</p>',
                'privacy_content' => '<p>Privacy content</p>',
            ])->byDefault();
        $fakeFirestore->shouldReceive('getDocumentInCollection')
            ->andReturn(['version' => '1.0'])->byDefault();
        $fakeFirestore->shouldReceive('setDocument')->byDefault();

        $this->app->instance(FirestoreService::class, $fakeFirestore);
    }

    protected function tearDown(): void
    {
        Mockery::close();
        parent::tearDown();
    }

    // ── GET /api/v1/policies/current ──────────────────────────────────────────

    public function test_current_returns_200_without_auth(): void
    {
        $this->getJson('/api/v1/policies/current')->assertStatus(200);
    }

    public function test_current_response_contains_terms_version(): void
    {
        $this->getJson('/api/v1/policies/current')
             ->assertJsonPath('data.terms_version', '1.0');
    }

    public function test_current_response_contains_privacy_version(): void
    {
        $this->getJson('/api/v1/policies/current')
             ->assertJsonPath('data.privacy_version', '1.0');
    }

    public function test_current_response_contains_terms_content(): void
    {
        $this->getJson('/api/v1/policies/current')
             ->assertJsonPath('data.terms_content', '<p>Terms content</p>');
    }

    public function test_current_response_contains_privacy_content(): void
    {
        $this->getJson('/api/v1/policies/current')
             ->assertJsonPath('data.privacy_content', '<p>Privacy content</p>');
    }

    public function test_current_has_data_wrapper(): void
    {
        $this->getJson('/api/v1/policies/current')
             ->assertJsonStructure(['data' => ['terms_version', 'privacy_version']]);
    }

    // ── POST /api/v1/policies/accept ─────────────────────────────────────────

    public function test_accept_requires_authentication(): void
    {
        $this->postJson('/api/v1/policies/accept', ['type' => 'all'])->assertStatus(401);
    }

    public function test_accept_all_returns_200(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->postJson('/api/v1/policies/accept', ['type' => 'all'])
             ->assertStatus(200)
             ->assertJson(['message' => 'Acceptance recorded.']);
    }

    public function test_accept_terms_returns_200(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->postJson('/api/v1/policies/accept', ['type' => 'terms'])
             ->assertStatus(200);
    }

    public function test_accept_privacy_returns_200(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->postJson('/api/v1/policies/accept', ['type' => 'privacy'])
             ->assertStatus(200);
    }

    public function test_accept_invalid_type_returns_422(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->postJson('/api/v1/policies/accept', ['type' => 'cookie'])
             ->assertStatus(422)
             ->assertJsonValidationErrors(['type']);
    }

    public function test_accept_missing_type_returns_422(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->postJson('/api/v1/policies/accept', [])
             ->assertStatus(422)
             ->assertJsonValidationErrors(['type']);
    }

    // ── GET /api/v1/policies/status ──────────────────────────────────────────

    public function test_status_requires_authentication(): void
    {
        $this->getJson('/api/v1/policies/status')->assertStatus(401);
    }

    public function test_status_returns_200_when_authenticated(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->getJson('/api/v1/policies/status')
             ->assertStatus(200);
    }

    public function test_status_response_has_correct_structure(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->getJson('/api/v1/policies/status')
             ->assertJsonStructure([
                 'data' => [
                     'terms_accepted',
                     'privacy_accepted',
                     'all_accepted',
                     'current_versions',
                 ],
             ]);
    }

    public function test_status_returns_accepted_when_versions_match(): void
    {
        // Firestore returns version '1.0' for acceptances, current version is '1.0'
        $this->withFakeFirebaseAuth(self::UID)
             ->getJson('/api/v1/policies/status')
             ->assertJsonPath('data.terms_accepted', true)
             ->assertJsonPath('data.privacy_accepted', true)
             ->assertJsonPath('data.all_accepted', true);
    }

    public function test_status_returns_not_accepted_when_version_mismatch(): void
    {
        // Override: user accepted '0.9', but current is '1.0'
        $fakeFirestore = Mockery::mock(FirestoreService::class);
        $fakeFirestore->shouldReceive('getGlobalDocument')->andReturn([
            'terms_version'   => '1.0',
            'privacy_version' => '1.0',
        ]);
        $fakeFirestore->shouldReceive('getDocumentInCollection')->andReturn(['version' => '0.9']);
        $this->app->instance(FirestoreService::class, $fakeFirestore);

        $this->withFakeFirebaseAuth(self::UID)
             ->getJson('/api/v1/policies/status')
             ->assertJsonPath('data.terms_accepted', false)
             ->assertJsonPath('data.all_accepted', false);
    }
}
