<!DOCTYPE html>
<html lang="en" class="h-full bg-slate-50">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@yield('title', 'BudgetBuddy')</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
</head>
<body class="h-full font-sans antialiased">

<div class="min-h-screen flex flex-col">

    {{-- ── Top nav ── --}}
    <header class="bg-white border-b border-slate-200 sticky top-0 z-30">
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-14">
            {{-- Logo --}}
            <a href="{{ route('portal.dashboard') }}" class="flex items-center gap-2">
                <div class="w-7 h-7 rounded-lg bg-blue-600 flex items-center justify-center">
                    <i class="bi bi-piggy-bank text-white text-sm"></i>
                </div>
                <span class="font-semibold text-slate-900">Budget<span class="text-blue-600">Buddy</span></span>
            </a>

            {{-- Nav links --}}
            <nav class="hidden sm:flex items-center gap-1">
                <a href="{{ route('portal.dashboard') }}"
                   class="px-3 py-1.5 text-sm font-medium rounded-lg transition-colors
                          {{ request()->routeIs('portal.dashboard') ? 'text-blue-600 bg-blue-50' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100' }}">
                    <i class="bi bi-grid-1x2 mr-1.5"></i>Dashboard
                </a>
                <a href="{{ route('portal.upload') }}"
                   class="px-3 py-1.5 text-sm font-medium rounded-lg transition-colors
                          {{ request()->routeIs('portal.upload') ? 'text-blue-600 bg-blue-50' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100' }}">
                    <i class="bi bi-upload mr-1.5"></i>Upload Statement
                </a>
            </nav>

            {{-- User menu --}}
            <div class="flex items-center gap-3">
                @if(session('portal_user'))
                    <span class="text-sm text-slate-500 hidden sm:block">{{ session('portal_user.email') }}</span>
                @endif
                <form method="POST" action="{{ route('portal.logout') }}">
                    @csrf
                    <button class="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium text-slate-600
                                   border border-slate-300 rounded-lg hover:border-slate-400 hover:bg-slate-50 transition-colors">
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
            <div class="flex items-start gap-3 p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm mb-3">
                <i class="bi bi-check-circle-fill text-emerald-500 flex-shrink-0 mt-0.5"></i>
                {!! session('success') !!}
            </div>
        @endif
        @if(session('error'))
            <div class="flex items-start gap-3 p-4 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm mb-3">
                <i class="bi bi-exclamation-circle-fill text-red-500 flex-shrink-0 mt-0.5"></i>
                {{ session('error') }}
            </div>
        @endif
        @if(session('info'))
            <div class="flex items-start gap-3 p-4 rounded-xl bg-blue-50 border border-blue-200 text-blue-800 text-sm mb-3">
                <i class="bi bi-info-circle-fill text-blue-500 flex-shrink-0 mt-0.5"></i>
                {!! session('info') !!}
            </div>
        @endif
    </div>
    @endif

    {{-- ── Page content ── --}}
    <main class="flex-1 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
        @yield('content')
    </main>

    <footer class="border-t border-slate-200 py-4 text-center text-xs text-slate-400">
        BudgetBuddy &copy; {{ date('Y') }}
    </footer>
</div>

@stack('scripts')
</body>
</html>
