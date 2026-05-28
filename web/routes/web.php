<?php

use App\Http\Controllers\Admin\AuthController;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Controllers\Admin\UserController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn () => redirect()->route('admin.login'));

Route::prefix('admin')->name('admin.')->group(function () {

    // Auth (no middleware)
    Route::get('login', [AuthController::class, 'showLogin'])->name('login');
    Route::post('login', [AuthController::class, 'login'])->name('login.post');
    Route::post('logout', [AuthController::class, 'logout'])->name('logout');

    // Protected admin routes
    Route::middleware('admin.auth')->group(function () {
        Route::get('dashboard', [DashboardController::class, 'index'])->name('dashboard');

        Route::prefix('users')->name('users.')->group(function () {
            Route::get('/', [UserController::class, 'index'])->name('index');
            Route::get('{uid}', [UserController::class, 'show'])->name('show');
            Route::post('{uid}/disable', [UserController::class, 'disable'])->name('disable');
            Route::post('{uid}/enable', [UserController::class, 'enable'])->name('enable');
            Route::post('{uid}/reset-password', [UserController::class, 'resetPassword'])->name('reset-password');
            Route::delete('{uid}', [UserController::class, 'destroy'])->name('destroy');
        });
    });
});
