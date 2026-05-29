<?php

use App\Http\Controllers\Api\PolicyController;
use App\Http\Controllers\Api\StatementController;
use Illuminate\Support\Facades\Route;

// ── Public endpoints (no Firebase token required) ─────────────────────────────
Route::prefix('v1')->middleware(['api.cors', 'throttle:30,1'])->group(function () {
    Route::get('policies/current', [PolicyController::class, 'current']);
});

// ── Authenticated endpoints ───────────────────────────────────────────────────
Route::prefix('v1')->middleware(['api.cors', 'api.firebase', 'throttle:api'])->group(function () {

    // Statements
    Route::get('statements',       [StatementController::class, 'index']);
    Route::post('statements',      [StatementController::class, 'store'])
        ->middleware('throttle:10,1');    // 10 uploads / minute
    Route::get('statements/{id}',  [StatementController::class, 'show'])
        ->whereNumber('id');

    // Policies
    Route::get('policies/status',  [PolicyController::class, 'status']);
    Route::post('policies/accept', [PolicyController::class, 'accept']);
});
