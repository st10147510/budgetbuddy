<?php

namespace App\Services;

use Throwable;

class PolicyService
{
    private const COLLECTION = 'app_config';
    private const DOC        = 'policies';

    private FirestoreService $firestore;

    public function __construct()
    {
        $this->firestore = app(FirestoreService::class);
    }

    public function getCurrentVersions(): array
    {
        $doc = $this->firestore->getGlobalDocument(self::COLLECTION, self::DOC);
        return [
            'terms_version'      => $doc['terms_version']      ?? '1.0',
            'privacy_version'    => $doc['privacy_version']    ?? '1.0',
            'terms_updated_at'   => $doc['terms_updated_at']   ?? null,
            'privacy_updated_at' => $doc['privacy_updated_at'] ?? null,
        ];
    }

    public function hasUserAcceptedAll(string $uid): bool
    {
        return $this->hasUserAccepted($uid, 'terms')
            && $this->hasUserAccepted($uid, 'privacy');
    }

    public function hasUserAccepted(string $uid, string $type): bool
    {
        $versions = $this->getCurrentVersions();
        $required = $versions["{$type}_version"] ?? '1.0';

        $acceptance = $this->firestore->getDocumentInCollection($uid, 'policy_acceptances', $type);
        return ($acceptance['version'] ?? '') === $required;
    }

    public function recordAcceptance(string $uid, string $type, string $platform = 'web'): void
    {
        $versions = $this->getCurrentVersions();
        $version  = $versions["{$type}_version"] ?? '1.0';

        $this->firestore->setDocument($uid, 'policy_acceptances', $type, [
            'version'    => $version,
            'acceptedAt' => now()->toISOString(),
            'platform'   => $platform,
        ]);
    }

    public function recordAllAcceptances(string $uid, string $platform = 'web'): void
    {
        $this->recordAcceptance($uid, 'terms', $platform);
        $this->recordAcceptance($uid, 'privacy', $platform);
    }

    public function getContent(string $type): string
    {
        try {
            $doc = $this->firestore->getGlobalDocument(self::COLLECTION, self::DOC);
            return $doc["{$type}_content"] ?? '';
        } catch (Throwable) {
            return '';
        }
    }

    public function updateVersion(string $type, string $version, string $content = ''): void
    {
        $existing = $this->firestore->getGlobalDocument(self::COLLECTION, self::DOC);
        $existing["{$type}_version"]    = $version;
        $existing["{$type}_updated_at"] = now()->toISOString();
        if ($content !== '') {
            $existing["{$type}_name"]    = $type === 'terms' ? 'Terms & Conditions' : 'Privacy Policy';
            $existing["{$type}_content"] = $content;
        }
        $this->firestore->setGlobalDocument(self::COLLECTION, self::DOC, $existing);
    }
}
