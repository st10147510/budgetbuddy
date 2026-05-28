<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('statement_uploads', function (Blueprint $table) {
            $table->id();
            $table->string('uid');                          // Firebase UID
            $table->string('filename');                     // original filename
            $table->string('path');                         // storage path
            $table->string('status')->default('pending');   // pending|processing|done|failed
            $table->integer('rows_imported')->default(0);
            $table->text('error')->nullable();
            $table->string('default_category')->nullable();
            $table->timestamps();

            $table->index('uid');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('statement_uploads');
    }
};
