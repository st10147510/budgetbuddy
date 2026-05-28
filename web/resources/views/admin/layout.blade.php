<!DOCTYPE html>
<html lang="en" class="h-full bg-slate-50">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@yield('title', 'BudgetBuddy Admin')</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
</head>
<body class="h-full font-sans antialiased">

<div class="flex h-full min-h-screen">

    {{-- ── Sidebar ── --}}
    <aside class="w-60 flex-shrink-0 bg-slate-900 flex flex-col fixed inset-y-0 left-0 z-40">

        {{-- Logo --}}
        <div class="flex items-center gap-2 px-5 py-5 border-b border-white/[0.06]">
            <div class="w-7 h-7 rounded-lg bg-blue-500 flex items-center justify-center">
                <i class="bi bi-piggy-bank text-white text-sm"></i>
            </div>
            <span class="font-semibold text-white tracking-tight">
                Budget<span class="text-blue-400">Buddy</span>
            </span>
            <span class="ml-auto text-[10px] font-medium text-slate-500 uppercase tracking-wider">Admin</span>
        </div>

        {{-- Nav --}}
        <nav class="flex-1 py-4 space-y-0.5 px-3">
            <a href="{{ route('admin.dashboard') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.dashboard') ? 'nav-active' : 'text-slate-400 hover:text-white hover:bg-white/[0.06]' }}">
                <i class="bi bi-speedometer2 text-base"></i>
                Dashboard
            </a>
            <a href="{{ route('admin.users.index') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.users.*') ? 'nav-active' : 'text-slate-400 hover:text-white hover:bg-white/[0.06]' }}">
                <i class="bi bi-people text-base"></i>
                Users
            </a>
        </nav>

        {{-- Logout --}}
        <div class="px-3 pb-5 pt-3 border-t border-white/[0.06]">
            <form method="POST" action="{{ route('admin.logout') }}">
                @csrf
                <button class="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-slate-400
                               hover:text-white hover:bg-white/[0.06] transition-all duration-150">
                    <i class="bi bi-box-arrow-left text-base"></i>
                    Sign out
                </button>
            </form>
        </div>
    </aside>

    {{-- ── Main content ── --}}
    <div class="flex-1 flex flex-col ml-60">

        {{-- Topbar --}}
        <header class="bg-white border-b border-slate-200 px-8 py-4 flex items-center justify-between">
            <h1 class="text-slate-900 font-semibold text-lg">@yield('page-title', 'Dashboard')</h1>
            <div class="flex items-center gap-2 text-slate-500 text-sm">
                <i class="bi bi-shield-lock"></i>
                <span>Admin Panel</span>
            </div>
        </header>

        {{-- Alerts --}}
        <div class="px-8 pt-4">
            @if(session('success'))
                <div class="flex items-start gap-3 p-4 mb-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm">
                    <i class="bi bi-check-circle-fill text-emerald-500 mt-0.5"></i>
                    {{ session('success') }}
                </div>
            @endif
            @if(session('error'))
                <div class="flex items-start gap-3 p-4 mb-4 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm">
                    <i class="bi bi-exclamation-circle-fill text-red-500 mt-0.5"></i>
                    {{ session('error') }}
                </div>
            @endif
        </div>

        {{-- Page content --}}
        <main class="flex-1 px-8 pb-8 pt-4">
            @yield('content')
        </main>
    </div>

</div>

@stack('scripts')
</body>
</html>
