<?php

use App\Http\Controllers\Admin\AuthController;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Controllers\LegalController;
use App\Http\Controllers\Admin\InsightsController;
use App\Http\Controllers\Admin\PolicyController as AdminPolicyController;
use App\Http\Controllers\Admin\TransactionController;
use App\Http\Controllers\Admin\UploadMonitorController;
use App\Http\Controllers\Admin\UserController;
use App\Http\Controllers\Portal\AuthController as PortalAuthController;
use App\Http\Controllers\Portal\DashboardController as PortalDashboardController;
use App\Http\Controllers\Portal\PolicyController as PortalPolicyController;
use App\Http\Controllers\Portal\UploadController;
use Illuminate\Support\Facades\Route;

// ── Landing & Legal ────────────────────────────────────────────────────────────
Route::get('/', fn () => view('landing'))->name('landing');
Route::get('/terms',   [LegalController::class, 'terms'])->name('legal.terms');
Route::get('/privacy', [LegalController::class, 'privacy'])->name('legal.privacy');

// ── API Documentation (Swagger UI) ────────────────────────────────────────────
Route::get('/api/v1/docs', fn () => view('api.docs'))->name('api.docs');

// ── Admin ─────────────────────────────────────────────────────────────────────
Route::prefix('admin')->name('admin.')->group(function () {
    Route::get('login',  [AuthController::class, 'showLogin'])->name('login');
    Route::post('login', [AuthController::class, 'login'])->name('login.post')
        ->middleware('throttle:3,15');   // 3 attempts / 15 min per IP
    Route::post('logout',[AuthController::class, 'logout'])->name('logout');

    Route::middleware('admin.auth')->group(function () {
        Route::get('dashboard', [DashboardController::class, 'index'])->name('dashboard');

        Route::prefix('users')->name('users.')->group(function () {
            Route::get('export',      [UserController::class, 'export'])->name('export');
            Route::get('/',           [UserController::class, 'index'])->name('index');
            Route::get('{uid}',       [UserController::class, 'show'])->name('show');
            Route::post('{uid}/disable',        [UserController::class, 'disable'])->name('disable');
            Route::post('{uid}/enable',         [UserController::class, 'enable'])->name('enable');
            Route::post('{uid}/reset-password', [UserController::class, 'resetPassword'])->name('reset-password');
            Route::delete('{uid}',   [UserController::class, 'destroy'])->name('destroy');
        });

        Route::get('insights',     [InsightsController::class, 'index'])->name('insights');
        Route::get('transactions', [TransactionController::class, 'index'])->name('transactions');

        Route::prefix('uploads')->name('uploads.')->group(function () {
            Route::get('/',          [UploadMonitorController::class, 'index'])->name('index');
            Route::post('{id}/retry',[UploadMonitorController::class, 'retry'])->name('retry');
        });

        Route::prefix('policies')->name('policies.')->group(function () {
            Route::get('/',  [AdminPolicyController::class, 'index'])->name('index');
            Route::post('/', [AdminPolicyController::class, 'update'])->name('update');
        });
    });
});

// ── User portal ───────────────────────────────────────────────────────────────
Route::prefix('portal')->name('portal.')->group(function () {
    Route::get('login',  [PortalAuthController::class, 'showLogin'])->name('login');
    Route::post('login', [PortalAuthController::class, 'login'])->name('login.post')
        ->middleware('throttle:5,15');   // 5 attempts / 15 min per IP
    Route::post('logout',[PortalAuthController::class, 'logout'])->name('logout');

    Route::middleware(['portal.auth', 'portal.policies'])->group(function () {
        Route::get('/',       [PortalDashboardController::class, 'index'])->name('dashboard');
        Route::get('upload',  [UploadController::class, 'show'])->name('upload');
        Route::post('upload', [UploadController::class, 'store'])->name('upload.post');
    });

    Route::middleware('portal.auth')->group(function () {
        Route::get('policies/accept',  [PortalPolicyController::class, 'show'])->name('policies.accept');
        Route::post('policies/accept', [PortalPolicyController::class, 'accept'])->name('policies.accept.post');
        Route::get('policies',         [PortalPolicyController::class, 'show'])->name('policies.show');
    });
});
