<?php

namespace Tests\Feature;

use Tests\TestCase;

class ApiCorsTest extends TestCase
{
    private const ALLOWED = 'https://thebudgetbuddy.co.za';
    private const UNKNOWN = 'https://evil.example.com';

    public function test_options_preflight_returns_204(): void
    {
        $this->options('/api/v1/policies/current', [], ['Origin' => self::ALLOWED])
             ->assertStatus(204);
    }

    public function test_allowed_origin_reflected_in_cors_header(): void
    {
        $response = $this->get('/api/v1/policies/current', ['Origin' => self::ALLOWED]);
        $this->assertSame(self::ALLOWED, $response->headers->get('Access-Control-Allow-Origin'));
    }

    public function test_unknown_origin_is_denied(): void
    {
        $response = $this->get('/api/v1/policies/current', ['Origin' => self::UNKNOWN]);
        $this->assertSame('null', $response->headers->get('Access-Control-Allow-Origin'));
    }

    public function test_localhost_origin_allowed_for_dev(): void
    {
        $response = $this->get('/api/v1/policies/current', ['Origin' => 'http://localhost:8000']);
        $this->assertSame('http://localhost:8000', $response->headers->get('Access-Control-Allow-Origin'));
    }

    public function test_android_emulator_origin_allowed(): void
    {
        $response = $this->get('/api/v1/policies/current', ['Origin' => 'http://10.0.2.2:8000']);
        $this->assertSame('http://10.0.2.2:8000', $response->headers->get('Access-Control-Allow-Origin'));
    }

    public function test_preflight_exposes_allowed_methods(): void
    {
        $response = $this->options('/api/v1/policies/current', [], ['Origin' => self::ALLOWED]);
        $this->assertStringContainsString('GET', $response->headers->get('Access-Control-Allow-Methods') ?? '');
        $this->assertStringContainsString('POST', $response->headers->get('Access-Control-Allow-Methods') ?? '');
    }

    public function test_preflight_exposes_authorization_header(): void
    {
        $response = $this->options('/api/v1/policies/current', [], ['Origin' => self::ALLOWED]);
        $this->assertStringContainsString('Authorization', $response->headers->get('Access-Control-Allow-Headers') ?? '');
    }
}
