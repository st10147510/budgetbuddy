<?php

namespace Tests\Feature;

use Tests\TestCase;

class AdminAuthTest extends TestCase
{
    private function setAdminPassword(string $password = 'secret'): void
    {
        config(['admin.password' => $password]);
    }

    // ── Login page ─────────────────────────────────────────────────────────────

    public function test_login_page_accessible_when_unauthenticated(): void
    {
        $response = $this->get('/admin/login');

        $response->assertStatus(200);
        $response->assertViewIs('admin.login');
    }

    public function test_login_page_redirects_to_dashboard_when_already_authenticated(): void
    {
        $response = $this->withSession(['admin_authenticated' => true])
                         ->get('/admin/login');

        $response->assertRedirect(route('admin.dashboard'));
    }

    // ── Login POST ─────────────────────────────────────────────────────────────

    public function test_login_with_correct_password_sets_session_and_redirects(): void
    {
        $this->setAdminPassword('correctpass');

        $response = $this->post('/admin/login', ['password' => 'correctpass']);

        $response->assertRedirect(route('admin.dashboard'));
        $this->assertEquals(true, session('admin_authenticated'));
    }

    public function test_login_with_wrong_password_returns_error(): void
    {
        $this->setAdminPassword('correctpass');

        $response = $this->post('/admin/login', ['password' => 'wrongpass']);

        $response->assertSessionHasErrors('password');
        $this->assertNull(session('admin_authenticated'));
    }

    public function test_login_with_empty_password_fails_validation(): void
    {
        $response = $this->post('/admin/login', ['password' => '']);

        $response->assertSessionHasErrors('password');
    }

    public function test_login_with_missing_password_fails_validation(): void
    {
        $response = $this->post('/admin/login', []);

        $response->assertSessionHasErrors('password');
    }

    public function test_login_fails_when_admin_password_not_configured(): void
    {
        config(['admin.password' => null]);

        $response = $this->post('/admin/login', ['password' => 'anything']);

        $response->assertSessionHasErrors('password');
        $this->assertNull(session('admin_authenticated'));
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    public function test_logout_clears_session_and_redirects_to_login(): void
    {
        $response = $this->withSession(['admin_authenticated' => true])
                         ->post('/admin/logout');

        $response->assertRedirect(route('admin.login'));
        $this->assertNull(session('admin_authenticated'));
    }

    // ── Protected routes redirect when not authenticated ───────────────────────

    public function test_dashboard_redirects_to_login_without_session(): void
    {
        $response = $this->get('/admin/dashboard');

        $response->assertRedirect(route('admin.login'));
    }

    public function test_users_index_redirects_to_login_without_session(): void
    {
        $response = $this->get('/admin/users');

        $response->assertRedirect(route('admin.login'));
    }

    public function test_policies_index_redirects_to_login_without_session(): void
    {
        $response = $this->get('/admin/policies');

        $response->assertRedirect(route('admin.login'));
    }

    // ── Rate limiting on login POST ────────────────────────────────────────────

    public function test_login_endpoint_exists_for_post(): void
    {
        // Smoke-test the route is wired (don't attempt to exhaust the 3-attempt limit)
        $this->setAdminPassword('test');
        $response = $this->post('/admin/login', ['password' => 'test']);

        // Either a redirect (success) or validation error — not a 404 or 405
        $this->assertNotEquals(404, $response->status());
        $this->assertNotEquals(405, $response->status());
    }
}
