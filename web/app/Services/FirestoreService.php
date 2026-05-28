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
        $this->http = new Client(['timeout' => 10]);
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
