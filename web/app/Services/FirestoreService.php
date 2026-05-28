<?php

namespace App\Services;

use Google\Auth\Credentials\ServiceAccountCredentials;
use GuzzleHttp\Client;
use Throwable;

class FirestoreService
{
    private Client $http;
    private string $projectId;
    private string $baseUrl;
    private ?string $accessToken = null;

    public function __construct()
    {
        $this->http = new Client(['timeout' => 30]);
        $credsPath = config('firebase.projects.app.credentials');
        $creds = json_decode(file_get_contents(base_path($credsPath)), true);
        $this->projectId = $creds['project_id'];
        $this->baseUrl = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents";

        $credentials = new ServiceAccountCredentials(
            'https://www.googleapis.com/auth/datastore',
            $creds
        );
        $token = $credentials->fetchAuthToken();
        $this->accessToken = $token['access_token'];
    }

    public function getCollection(string $userId, string $collection): array
    {
        try {
            $url = "{$this->baseUrl}/users/{$userId}/{$collection}";
            $response = $this->http->get($url, [
                'headers' => ['Authorization' => "Bearer {$this->accessToken}"],
                'query'   => ['pageSize' => 500],
            ]);
            $body = json_decode($response->getBody(), true);
            return $this->parseDocuments($body['documents'] ?? []);
        } catch (Throwable) {
            return [];
        }
    }

    public function getUserDocument(string $userId): array
    {
        try {
            $url = "{$this->baseUrl}/users/{$userId}";
            $response = $this->http->get($url, [
                'headers' => ['Authorization' => "Bearer {$this->accessToken}"],
            ]);
            $body = json_decode($response->getBody(), true);
            return $this->parseFields($body['fields'] ?? []);
        } catch (Throwable) {
            return [];
        }
    }

    /**
     * Write a document with an auto-generated ID (POST).
     */
    public function addDocument(string $userId, string $collection, array $data): string
    {
        $url      = "{$this->baseUrl}/users/{$userId}/{$collection}";
        $response = $this->http->post($url, [
            'headers' => [
                'Authorization' => "Bearer {$this->accessToken}",
                'Content-Type'  => 'application/json',
            ],
            'json' => ['fields' => $this->encodeFields($data)],
        ]);

        $result = json_decode($response->getBody(), true);
        return $result['name'] ?? '';
    }

    /**
     * Write (upsert) a document with a specific ID (PATCH).
     * Matches Android's txCol(userId).document(id.toString()).set(...) pattern.
     */
    public function setDocument(string $userId, string $collection, string $docId, array $data): void
    {
        $url = "{$this->baseUrl}/users/{$userId}/{$collection}/{$docId}";
        $this->http->patch($url, [
            'headers' => [
                'Authorization' => "Bearer {$this->accessToken}",
                'Content-Type'  => 'application/json',
            ],
            'json' => ['fields' => $this->encodeFields($data)],
        ]);
    }

    private function encodeFields(array $data): array
    {
        $fields = [];
        foreach ($data as $key => $value) {
            $fields[$key] = $this->encodeValue($value);
        }
        return $fields;
    }

    private function encodeValue(mixed $value): array
    {
        return match (true) {
            is_null($value)   => ['nullValue' => null],
            is_bool($value)   => ['booleanValue' => $value],
            is_int($value)    => ['integerValue' => (string) $value],
            is_float($value)  => ['doubleValue' => $value],
            is_string($value) => ['stringValue' => $value],
            is_array($value) && array_is_list($value) => [
                'arrayValue' => ['values' => array_map([$this, 'encodeValue'], $value)]
            ],
            is_array($value) => ['mapValue' => ['fields' => $this->encodeFields($value)]],
            default => ['stringValue' => (string) $value],
        };
    }

    private function parseDocuments(array $documents): array
    {
        return array_map(fn ($doc) => $this->parseFields($doc['fields'] ?? []), $documents);
    }

    private function parseFields(array $fields): array
    {
        $result = [];
        foreach ($fields as $key => $value) {
            $result[$key] = $this->parseValue($value);
        }
        return $result;
    }

    private function parseValue(array $value): mixed
    {
        return match (true) {
            isset($value['stringValue'])    => $value['stringValue'],
            isset($value['integerValue'])   => (int) $value['integerValue'],
            isset($value['doubleValue'])    => (float) $value['doubleValue'],
            isset($value['booleanValue'])   => (bool) $value['booleanValue'],
            isset($value['nullValue'])      => null,
            isset($value['timestampValue']) => $value['timestampValue'],
            isset($value['mapValue'])       => $this->parseFields($value['mapValue']['fields'] ?? []),
            isset($value['arrayValue'])     => array_map(
                fn ($v) => $this->parseValue($v),
                $value['arrayValue']['values'] ?? []
            ),
            default => null,
        };
    }
}
