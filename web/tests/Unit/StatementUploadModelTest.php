<?php

namespace Tests\Unit;

use App\Models\StatementUpload;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class StatementUploadModelTest extends TestCase
{
    use RefreshDatabase;

    // ── Fillable ───────────────────────────────────────────────────────────────

    public function test_fillable_fields_accepted(): void
    {
        $upload = StatementUpload::create([
            'uid'              => 'user-abc',
            'filename'         => 'statement.pdf',
            'path'             => 'statements/user-abc/statement.pdf',
            'status'           => 'pending',
            'rows_imported'    => 0,
            'default_category' => null,
        ]);

        $this->assertDatabaseHas('statement_uploads', ['uid' => 'user-abc', 'status' => 'pending']);
    }

    public function test_status_defaults_to_pending_when_not_set(): void
    {
        $upload = StatementUpload::create([
            'uid'      => 'user-xyz',
            'filename' => 'file.pdf',
            'path'     => 'some/path.pdf',
            'status'   => 'pending',
        ]);

        $this->assertSame('pending', $upload->status);
    }

    public function test_status_can_be_updated_to_processing(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'u1', 'filename' => 'f.pdf', 'path' => 'p', 'status' => 'pending',
        ]);

        $upload->update(['status' => 'processing']);

        $this->assertSame('processing', $upload->fresh()->status);
    }

    public function test_status_can_be_set_to_done(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'u1', 'filename' => 'f.pdf', 'path' => 'p',
            'status' => 'processing', 'rows_imported' => 12,
        ]);

        $upload->update(['status' => 'done']);

        $this->assertSame('done', $upload->fresh()->status);
    }

    public function test_status_can_be_set_to_failed_with_error(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'u1', 'filename' => 'f.pdf', 'path' => 'p',
            'status' => 'processing',
        ]);

        $upload->update(['status' => 'failed', 'error' => 'Parse error']);

        $fresh = $upload->fresh();
        $this->assertSame('failed', $fresh->status);
        $this->assertSame('Parse error', $fresh->error);
    }

    public function test_rows_imported_stored_as_integer(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'u1', 'filename' => 'f.pdf', 'path' => 'p',
            'status' => 'done', 'rows_imported' => 47,
        ]);

        $this->assertSame(47, $upload->fresh()->rows_imported);
    }

    public function test_storage_url_stored(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'u1', 'filename' => 'f.pdf', 'path' => 'p',
            'status' => 'done', 'storage_url' => 'gs://bucket/path/file.pdf',
        ]);

        $this->assertSame('gs://bucket/path/file.pdf', $upload->fresh()->storage_url);
    }

    public function test_default_category_nullable(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'u1', 'filename' => 'f.pdf', 'path' => 'p',
            'status' => 'pending', 'default_category' => null,
        ]);

        $this->assertNull($upload->fresh()->default_category);
    }

    public function test_default_category_stored_as_string(): void
    {
        $upload = StatementUpload::create([
            'uid' => 'u1', 'filename' => 'f.pdf', 'path' => 'p',
            'status' => 'pending', 'default_category' => '3',
        ]);

        $this->assertSame('3', $upload->fresh()->default_category);
    }

    // ── Multiple records for same UID ──────────────────────────────────────────

    public function test_multiple_uploads_same_uid(): void
    {
        foreach (range(1, 3) as $i) {
            StatementUpload::create([
                'uid' => 'same-user', 'filename' => "file{$i}.pdf",
                'path' => "path/{$i}.pdf", 'status' => 'done', 'rows_imported' => $i * 10,
            ]);
        }

        $count = StatementUpload::where('uid', 'same-user')->count();
        $this->assertSame(3, $count);
    }
}
