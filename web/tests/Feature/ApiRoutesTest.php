<?php

namespace Tests\Feature;

use Tests\TestCase;

class ApiRoutesTest extends TestCase
{
    public function test_api_root_redirects_to_docs(): void
    {
        $this->get('/api/v1')->assertRedirect('/api/v1/docs');
    }

    public function test_swagger_docs_page_returns_200(): void
    {
        $this->get('/api/v1/docs')->assertStatus(200);
    }

    public function test_swagger_docs_contains_budgetbuddy_title(): void
    {
        $this->get('/api/v1/docs')->assertSee('BudgetBuddy');
    }

    public function test_swagger_docs_loads_swagger_ui_bundle(): void
    {
        $this->get('/api/v1/docs')->assertSee('swagger-ui-bundle.js', false);
    }

    public function test_swagger_docs_references_openapi_spec(): void
    {
        $this->get('/api/v1/docs')->assertSee('api-docs/openapi.yaml', false);
    }

    public function test_openapi_yaml_spec_is_accessible(): void
    {
        $this->get('/api-docs/openapi.yaml')->assertStatus(200);
    }

    public function test_terms_page_returns_200(): void
    {
        $this->get('/terms')->assertStatus(200);
    }

    public function test_privacy_page_returns_200(): void
    {
        $this->get('/privacy')->assertStatus(200);
    }

    public function test_authenticated_api_endpoint_without_token_returns_401(): void
    {
        $this->getJson('/api/v1/policies/status')->assertStatus(401);
    }

    public function test_authenticated_post_without_token_returns_401(): void
    {
        $this->postJson('/api/v1/policies/accept', ['type' => 'all'])->assertStatus(401);
    }

    public function test_statement_list_without_token_returns_401(): void
    {
        $this->getJson('/api/v1/statements')->assertStatus(401);
    }

    public function test_unknown_api_route_returns_404(): void
    {
        $this->getJson('/api/v1/nonexistent')->assertStatus(404);
    }
}
