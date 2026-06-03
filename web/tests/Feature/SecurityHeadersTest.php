<?php

namespace Tests\Feature;

use Tests\TestCase;

class SecurityHeadersTest extends TestCase
{
    private function getResponse()
    {
        return $this->get('/');
    }

    public function test_x_frame_options_is_sameorigin(): void
    {
        $this->getResponse()->assertHeader('X-Frame-Options', 'SAMEORIGIN');
    }

    public function test_x_content_type_options_is_nosniff(): void
    {
        $this->getResponse()->assertHeader('X-Content-Type-Options', 'nosniff');
    }

    public function test_referrer_policy_is_strict(): void
    {
        $this->getResponse()->assertHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
    }

    public function test_x_powered_by_header_is_absent(): void
    {
        $response = $this->getResponse();
        $this->assertNull($response->headers->get('X-Powered-By'));
    }

    public function test_server_header_is_absent(): void
    {
        $response = $this->getResponse();
        $this->assertNull($response->headers->get('Server'));
    }

    public function test_content_security_policy_contains_self(): void
    {
        $csp = $this->getResponse()->headers->get('Content-Security-Policy');
        $this->assertNotNull($csp);
        $this->assertStringContainsString("default-src 'self'", $csp);
    }

    public function test_csp_allows_unpkg_for_swagger(): void
    {
        $csp = $this->getResponse()->headers->get('Content-Security-Policy');
        $this->assertStringContainsString('unpkg.com', $csp);
    }

    public function test_csp_allows_google_fonts(): void
    {
        $csp = $this->getResponse()->headers->get('Content-Security-Policy');
        $this->assertStringContainsString('fonts.googleapis.com', $csp);
    }

    public function test_frame_ancestors_none_prevents_clickjacking(): void
    {
        $csp = $this->getResponse()->headers->get('Content-Security-Policy');
        $this->assertStringContainsString("frame-ancestors 'none'", $csp);
    }

    public function test_permissions_policy_disables_camera_and_mic(): void
    {
        $policy = $this->getResponse()->headers->get('Permissions-Policy');
        $this->assertNotNull($policy);
        $this->assertStringContainsString('camera=()', $policy);
        $this->assertStringContainsString('microphone=()', $policy);
    }

    public function test_hsts_not_sent_over_http(): void
    {
        // HTTP (non-secure) request should not send HSTS
        $response = $this->getResponse();
        $this->assertNull($response->headers->get('Strict-Transport-Security'));
    }

    public function test_security_headers_present_on_api_routes(): void
    {
        $response = $this->get('/api/v1/policies/current');
        $this->assertNotNull($response->headers->get('X-Frame-Options'));
    }
}
