<?php

namespace App\Providers;

use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    public function register(): void {}

    public function boot(): void
    {
        $this->configureRateLimiting();
    }

    private function configureRateLimiting(): void
    {
        // API authenticated endpoints: 120 req/min keyed by Firebase UID (or IP as fallback)
        RateLimiter::for('api', function (Request $request) {
            $key = $request->input('firebase_uid') ?? $request->ip();
            return Limit::perMinute(120)->by($key);
        });
    }
}
