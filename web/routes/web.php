<?php

use App\Http\Controllers\Admin\AuthController;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Controllers\Admin\UserController;
use App\Http\Controllers\Portal\AuthController as PortalAuthController;
use App\Http\Controllers\Portal\DashboardController as PortalDashboardController;
use App\Http\Controllers\Portal\UploadController;
use Illuminate\Support\Facades\Route;

// Root → portal login
Route::get('/', fn () => redirect()->route('portal.login'));

// ── Admin ─────────────────────────────────────────────────────────────────────
Route::prefix('admin')->name('admin.')->group(function () {
    Route::get('login',  [AuthController::class, 'showLogin'])->name('login');
    Route::post('login', [AuthController::class, 'login'])->name('login.post');
    Route::post('logout',[AuthController::class, 'logout'])->name('logout');

    Route::middleware('admin.auth')->group(function () {
        Route::get('dashboard', [DashboardController::class, 'index'])->name('dashboard');

        Route::prefix('users')->name('users.')->group(function () {
            Route::get('/',           [UserController::class, 'index'])->name('index');
            Route::get('{uid}',       [UserController::class, 'show'])->name('show');
            Route::post('{uid}/disable',        [UserController::class, 'disable'])->name('disable');
            Route::post('{uid}/enable',         [UserController::class, 'enable'])->name('enable');
            Route::post('{uid}/reset-password', [UserController::class, 'resetPassword'])->name('reset-password');
            Route::delete('{uid}',   [UserController::class, 'destroy'])->name('destroy');
        });
    });
});

// ── User portal ───────────────────────────────────────────────────────────────
Route::prefix('portal')->name('portal.')->group(function () {
    Route::get('login',  [PortalAuthController::class, 'showLogin'])->name('login');
    Route::post('login', [PortalAuthController::class, 'login'])->name('login.post');
    Route::post('logout',[PortalAuthController::class, 'logout'])->name('logout');

    Route::middleware('portal.auth')->group(function () {
        Route::get('/',       [PortalDashboardController::class, 'index'])->name('dashboard');
        Route::get('upload',  [UploadController::class, 'show'])->name('upload');
        Route::post('upload', [UploadController::class, 'store'])->name('upload.post');
    });
});
