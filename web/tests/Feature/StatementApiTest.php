<?php

namespace Tests\Feature;

use App\Models\StatementUpload;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Queue;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class StatementApiTest extends TestCase
{
    use RefreshDatabase;

    private const UID = 'test-uid-stmt';

    protected function setUp(): void
    {
        parent::setUp();
        Storage::fake('local');
        Queue::fake();
    }

    // ── GET /api/v1/statements ───────────────────────────────────────────────

    public function test_list_requires_authentication(): void
    {
        $this->getJson('/api/v1/statements')->assertStatus(401);
    }

    public function test_list_returns_empty_array_for_new_user(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->getJson('/api/v1/statements')
             ->assertStatus(200)
             ->assertJsonPath('data', []);
    }

    public function test_list_returns_only_current_users_jobs(): void
    {
        StatementUpload::create([
            'uid' => self::UID, 'filename' => 'mine.pdf',
            'path' => 'statements/mine.pdf', 'status' => 'pending',
            'default_category' => 0,
        ]);
        StatementUpload::create([
            'uid' => 'other-uid', 'filename' => 'theirs.pdf',
            'path' => 'statements/theirs.pdf', 'status' => 'pending',
            'default_category' => 0,
        ]);

        $response = $this->withFakeFirebaseAuth(self::UID)->getJson('/api/v1/statements');
        $response->assertStatus(200);
        $this->assertCount(1, $response->json('data'));
        $this->assertSame('mine.pdf', $response->json('data.0.filename'));
    }

    public function test_list_caps_at_twenty_results(): void
    {
        for ($i = 0; $i < 25; $i++) {
            StatementUpload::create([
                'uid' => self::UID, 'filename' => "stmt-{$i}.pdf",
                'path' => "statements/stmt-{$i}.pdf", 'status' => 'pending',
                'default_category' => 0,
            ]);
        }

        $response = $this->withFakeFirebaseAuth(self::UID)->getJson('/api/v1/statements');
        $this->assertCount(20, $response->json('data'));
    }

    public function test_list_response_has_expected_fields(): void
    {
        StatementUpload::create([
            'uid' => self::UID, 'filename' => 'test.pdf',
            'path' => 'statements/test.pdf', 'status' => 'pending',
            'default_category' => 0,
        ]);

        $this->withFakeFirebaseAuth(self::UID)
             ->getJson('/api/v1/statements')
             ->assertJsonStructure(['data' => [['id', 'filename', 'status', 'rows_imported', 'error', 'created_at']]]);
    }

    // ── POST /api/v1/statements ──────────────────────────────────────────────

    public function test_upload_requires_authentication(): void
    {
        $this->postJson('/api/v1/statements')->assertStatus(401);
    }

    public function test_upload_requires_file(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->postJson('/api/v1/statements', [])
             ->assertStatus(422)
             ->assertJsonValidationErrors(['file']);
    }

    public function test_upload_rejects_non_pdf(): void
    {
        $file = UploadedFile::fake()->create('statement.txt', 100, 'text/plain');

        $this->withFakeFirebaseAuth(self::UID)
             ->post('/api/v1/statements', ['file' => $file], ['Accept' => 'application/json'])
             ->assertStatus(422)
             ->assertJsonValidationErrors(['file']);
    }

    public function test_upload_accepts_pdf_and_returns_202(): void
    {
        $file = UploadedFile::fake()->create('FNB_Statement.pdf', 500, 'application/pdf');

        $this->withFakeFirebaseAuth(self::UID)
             ->post('/api/v1/statements', ['file' => $file], ['Accept' => 'application/json'])
             ->assertStatus(202)
             ->assertJsonStructure(['message', 'data' => ['id', 'filename', 'status']]);
    }

    public function test_upload_stores_record_in_database(): void
    {
        $file = UploadedFile::fake()->create('January.pdf', 200, 'application/pdf');

        $this->withFakeFirebaseAuth(self::UID)
             ->post('/api/v1/statements', ['file' => $file], ['Accept' => 'application/json']);

        $this->assertDatabaseHas('statement_uploads', [
            'uid'    => self::UID,
            'status' => 'pending',
        ]);
    }

    public function test_upload_queues_processing_job(): void
    {
        $file = UploadedFile::fake()->create('statement.pdf', 200, 'application/pdf');

        $this->withFakeFirebaseAuth(self::UID)
             ->post('/api/v1/statements', ['file' => $file], ['Accept' => 'application/json']);

        Queue::assertPushed(\App\Jobs\ProcessBankStatement::class);
    }

    public function test_upload_response_status_is_pending(): void
    {
        $file = UploadedFile::fake()->create('stmt.pdf', 200, 'application/pdf');

        $response = $this->withFakeFirebaseAuth(self::UID)
                         ->post('/api/v1/statements', ['file' => $file], ['Accept' => 'application/json']);

        $this->assertSame('pending', $response->json('data.status'));
    }

    // ── GET /api/v1/statements/{id} ──────────────────────────────────────────

    public function test_get_single_requires_authentication(): void
    {
        $this->getJson('/api/v1/statements/1')->assertStatus(401);
    }

    public function test_get_single_returns_job_details(): void
    {
        $upload = StatementUpload::create([
            'uid' => self::UID, 'filename' => 'detail.pdf',
            'path' => 'statements/detail.pdf', 'status' => 'done',
            'rows_imported' => 42, 'default_category' => 0,
        ]);

        $this->withFakeFirebaseAuth(self::UID)
             ->getJson("/api/v1/statements/{$upload->id}")
             ->assertStatus(200)
             ->assertJsonPath('data.filename', 'detail.pdf')
             ->assertJsonPath('data.status', 'done')
             ->assertJsonPath('data.rows_imported', 42);
    }

    public function test_get_single_returns_404_for_other_users_job(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'other-uid', 'filename' => 'private.pdf',
            'path' => 'statements/private.pdf', 'status' => 'pending',
            'default_category' => 0,
        ]);

        $this->withFakeFirebaseAuth(self::UID)
             ->getJson("/api/v1/statements/{$upload->id}")
             ->assertStatus(404);
    }

    public function test_get_single_returns_404_for_nonexistent_id(): void
    {
        $this->withFakeFirebaseAuth(self::UID)
             ->getJson('/api/v1/statements/99999')
             ->assertStatus(404);
    }
}
