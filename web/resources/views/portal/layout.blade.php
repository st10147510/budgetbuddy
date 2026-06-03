<!DOCTYPE html>
<html lang="en" class="h-full" style="background:#0D0D0D">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@yield('title', 'BudgetBuddy')</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        body { background: #0D0D0D; color: #fff; }
        .portal-nav { background: #1A1A1A; border-bottom: 1px solid rgba(255,255,255,0.06); }
        .portal-nav-link { color: #999; padding: 0.375rem 0.75rem; border-radius: 0.5rem; font-size: 0.875rem; font-weight: 500; transition: color 0.15s, background 0.15s; }
        .portal-nav-link:hover { color: #fff; background: rgba(255,255,255,0.06); }
        .portal-nav-link.active { color: #6EDCD3; background: rgba(110,220,211,0.1); }
        .portal-card { background: #1A1A1A; border: 1px solid rgba(255,255,255,0.06); border-radius: 1rem; }
        .portal-footer { border-top: 1px solid rgba(255,255,255,0.06); color: #555; font-size: 0.75rem; text-align: center; padding: 1rem; }
    </style>
</head>
<body class="h-full font-sans antialiased">

<div class="min-h-screen flex flex-col">

    {{-- ── Top nav ── --}}
    <header class="portal-nav sticky top-0 z-30">
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-14">

            {{-- Logo --}}
            <a href="{{ route('portal.dashboard') }}" class="flex items-center gap-2">
                <div class="w-7 h-7 rounded-lg flex items-center justify-center" style="background:#6EDCD3">
                    <i class="bi bi-piggy-bank text-sm" style="color:#0D0D0D"></i>
                </div>
                <span class="font-semibold text-white">Budget<span style="color:#6EDCD3">Buddy</span></span>
            </a>

            {{-- Nav links --}}
            <nav class="hidden sm:flex items-center gap-1">
                <a href="{{ route('portal.dashboard') }}"
                   class="portal-nav-link {{ request()->routeIs('portal.dashboard') ? 'active' : '' }}">
                    <i class="bi bi-grid-1x2 mr-1"></i>Dashboard
                </a>
                <a href="{{ route('portal.upload') }}"
                   class="portal-nav-link {{ request()->routeIs('portal.upload') ? 'active' : '' }}">
                    <i class="bi bi-upload mr-1"></i>Upload
                </a>
            </nav>

            {{-- User + sign out --}}
            <div class="flex items-center gap-3">
                @if(session('portal_user'))
                <span class="text-sm hidden sm:block" style="color:#666">
                    {{ session('portal_user.email') }}
                </span>
                @endif
                <form method="POST" action="{{ route('portal.logout') }}">
                    @csrf
                    <button class="flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium rounded-lg transition-colors"
                            style="color:#999; border:1px solid rgba(255,255,255,0.1)"
                            onmouseover="this.style.color='#fff';this.style.borderColor='rgba(255,255,255,0.2)'"
                            onmouseout="this.style.color='#999';this.style.borderColor='rgba(255,255,255,0.1)'">
                        <i class="bi bi-box-arrow-right"></i>
                        <span class="hidden sm:inline">Sign out</span>
                    </button>
                </form>
            </div>
        </div>
    </header>

    {{-- Alerts --}}
    @if(session('success') || session('error') || session('info'))
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-4 w-full">
        @if(session('success'))
            <div class="flex items-start gap-3 p-4 rounded-xl text-sm mb-3"
                 style="background:rgba(76,175,80,0.12);border:1px solid rgba(76,175,80,0.25);color:#81c784">
                <i class="bi bi-check-circle-fill flex-shrink-0 mt-0.5" style="color:#4CAF50"></i>
                {!! session('success') !!}
            </div>
        @endif
        @if(session('error'))
            <div class="flex items-start gap-3 p-4 rounded-xl text-sm mb-3"
                 style="background:rgba(244,67,54,0.12);border:1px solid rgba(244,67,54,0.25);color:#ef9a9a">
                <i class="bi bi-exclamation-circle-fill flex-shrink-0 mt-0.5" style="color:#F44336"></i>
                {{ session('error') }}
            </div>
        @endif
        @if(session('info'))
            <div class="flex items-start gap-3 p-4 rounded-xl text-sm mb-3"
                 style="background:rgba(110,220,211,0.1);border:1px solid rgba(110,220,211,0.2);color:#6EDCD3">
                <i class="bi bi-info-circle-fill flex-shrink-0 mt-0.5" style="color:#6EDCD3"></i>
                {!! session('info') !!}
            </div>
        @endif
    </div>
    @endif

    {{-- ── Page content ── --}}
    <main class="flex-1 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
        @yield('content')
    </main>

    <footer class="portal-footer">
        BudgetBuddy &copy; {{ date('Y') }}
    </footer>
</div>

@stack('scripts')
</body>
</html>
