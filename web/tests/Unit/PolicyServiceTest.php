<?php

namespace Tests\Unit;

use App\Services\FirestoreService;
use App\Services\PolicyService;
use Mockery;
use Tests\TestCase;

class PolicyServiceTest extends TestCase
{
    protected function tearDown(): void
    {
        Mockery::close();
        parent::tearDown();
    }

    private function makePolicyService(array $policyDoc = [], array $acceptanceDoc = []): PolicyService
    {
        $firestore = Mockery::mock(FirestoreService::class);
        $firestore->shouldReceive('getGlobalDocument')->andReturn(
            array_merge([
                'terms_version'   => '1.0',
                'privacy_version' => '1.0',
                'terms_content'   => '',
                'privacy_content' => '',
            ], $policyDoc)
        )->byDefault();
        $firestore->shouldReceive('getDocumentInCollection')->andReturn($acceptanceDoc)->byDefault();
        $firestore->shouldReceive('setDocument')->byDefault();

        $this->app->instance(FirestoreService::class, $firestore);
        return app(PolicyService::class);
    }

    // ── getCurrentVersions ────────────────────────────────────────────────────

    public function test_get_current_versions_returns_terms_and_privacy(): void
    {
        $service = $this->makePolicyService(['terms_version' => '2.0', 'privacy_version' => '1.5']);
        $versions = $service->getCurrentVersions();

        $this->assertSame('2.0', $versions['terms_version']);
        $this->assertSame('1.5', $versions['privacy_version']);
    }

    public function test_get_current_versions_defaults_to_1_0_when_firestore_empty(): void
    {
        $this->makePolicyService([]);   // Firestore returns empty doc (already has defaults in makePolicyService)
        $service  = app(PolicyService::class);
        $versions = $service->getCurrentVersions();

        $this->assertSame('1.0', $versions['terms_version']);
        $this->assertSame('1.0', $versions['privacy_version']);
    }

    // ── hasUserAccepted ───────────────────────────────────────────────────────

    public function test_has_user_accepted_returns_true_when_version_matches(): void
    {
        $service = $this->makePolicyService(
            ['terms_version' => '1.0'],
            ['version' => '1.0']
        );

        $this->assertTrue($service->hasUserAccepted('uid-123', 'terms'));
    }

    public function test_has_user_accepted_returns_false_when_version_mismatches(): void
    {
        $service = $this->makePolicyService(
            ['terms_version' => '2.0'],
            ['version' => '1.0']   // user accepted old version
        );

        $this->assertFalse($service->hasUserAccepted('uid-123', 'terms'));
    }

    public function test_has_user_accepted_returns_false_when_no_acceptance_record(): void
    {
        $service = $this->makePolicyService(
            ['terms_version' => '1.0'],
            []   // no record
        );

        $this->assertFalse($service->hasUserAccepted('uid-123', 'terms'));
    }

    // ── hasUserAcceptedAll ────────────────────────────────────────────────────

    public function test_has_user_accepted_all_true_when_both_match(): void
    {
        $firestore = Mockery::mock(FirestoreService::class);
        $firestore->shouldReceive('getGlobalDocument')->andReturn([
            'terms_version' => '1.0', 'privacy_version' => '1.0',
        ]);
        $firestore->shouldReceive('getDocumentInCollection')
            ->with(Mockery::any(), 'policy_acceptances', 'terms')
            ->andReturn(['version' => '1.0']);
        $firestore->shouldReceive('getDocumentInCollection')
            ->with(Mockery::any(), 'policy_acceptances', 'privacy')
            ->andReturn(['version' => '1.0']);
        $this->app->instance(FirestoreService::class, $firestore);

        $this->assertTrue(app(PolicyService::class)->hasUserAcceptedAll('uid-123'));
    }

    public function test_has_user_accepted_all_false_when_one_missing(): void
    {
        $firestore = Mockery::mock(FirestoreService::class);
        $firestore->shouldReceive('getGlobalDocument')->andReturn([
            'terms_version' => '1.0', 'privacy_version' => '1.0',
        ]);
        $firestore->shouldReceive('getDocumentInCollection')
            ->with(Mockery::any(), 'policy_acceptances', 'terms')
            ->andReturn(['version' => '1.0']);
        $firestore->shouldReceive('getDocumentInCollection')
            ->with(Mockery::any(), 'policy_acceptances', 'privacy')
            ->andReturn([]);   // no privacy acceptance
        $this->app->instance(FirestoreService::class, $firestore);

        $this->assertFalse(app(PolicyService::class)->hasUserAcceptedAll('uid-123'));
    }

    // ── getContent ────────────────────────────────────────────────────────────

    public function test_get_content_returns_html_from_firestore(): void
    {
        $service = $this->makePolicyService(['terms_content' => '<h1>Terms</h1>']);
        $this->assertSame('<h1>Terms</h1>', $service->getContent('terms'));
    }

    public function test_get_content_returns_empty_string_as_fallback(): void
    {
        $service = $this->makePolicyService([]);
        $this->assertSame('', $service->getContent('missing_type'));
    }
}
