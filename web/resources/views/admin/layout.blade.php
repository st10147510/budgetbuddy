<!DOCTYPE html>
<html lang="en" class="h-full" style="background:#0D0D0D">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@yield('title', 'BudgetBuddy Admin')</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        /* ── Dark theme overrides for admin content area ───────────────────────── */

        /* Cards / containers */
        main .bg-white               { background: #1A1A1A !important; }
        main .bg-slate-50            { background: #232323 !important; }
        main .bg-slate-100           { background: rgba(255,255,255,0.06) !important; }

        /* Borders */
        main .border-slate-200,
        main .border-slate-100       { border-color: rgba(255,255,255,0.07) !important; }
        main .border-slate-300       { border-color: rgba(255,255,255,0.12) !important; }
        main .divide-slate-100 > * + * { border-color: rgba(255,255,255,0.07) !important; }

        /* Text */
        main .text-slate-900         { color: #f0f0f0 !important; }
        main .text-slate-700         { color: #ccc    !important; }
        main .text-slate-600         { color: #aaa    !important; }
        main .text-slate-500         { color: #888    !important; }
        main .text-slate-400         { color: #666    !important; }
        main .text-slate-300         { color: #555    !important; }

        /* Row hovers */
        main .hover\:bg-slate-50\/60:hover { background: rgba(255,255,255,0.03) !important; }
        main .hover\:bg-slate-50:hover     { background: rgba(255,255,255,0.05) !important; }
        main .hover\:bg-slate-200:hover    { background: rgba(255,255,255,0.08) !important; }

        /* Inputs & selects */
        main input:not([type=submit]):not([type=button]),
        main select,
        main textarea {
            background:    #252525 !important;
            border-color:  rgba(255,255,255,0.1) !important;
            color:         #ccc !important;
        }
        main input::placeholder { color: #555 !important; }

        /* Semantic status badges — keep colour, darken bg */
        main .bg-emerald-50  { background: rgba(52,211,153,0.1)  !important; }
        main .bg-red-50      { background: rgba(248,113,113,0.1) !important; }
        main .bg-blue-50     { background: rgba(96,165,250,0.1)  !important; }
        main .bg-violet-50   { background: rgba(167,139,250,0.1) !important; }
        main .bg-indigo-50   { background: rgba(129,140,248,0.1) !important; }
        main .bg-amber-50    { background: rgba(251,191,36,0.1)  !important; }
        main .bg-rose-50     { background: rgba(251,113,133,0.1) !important; }
        main .bg-orange-50   { background: rgba(251,146,60,0.1)  !important; }

        main .border-emerald-200 { border-color: rgba(52,211,153,0.25)  !important; }
        main .border-red-200     { border-color: rgba(248,113,113,0.25) !important; }
        main .border-blue-200    { border-color: rgba(96,165,250,0.25)  !important; }
        main .border-amber-200   { border-color: rgba(251,191,36,0.25)  !important; }

        main .text-amber-800 { color: #fbbf24 !important; }
        main .bg-amber-100   { background: rgba(251,191,36,0.12) !important; }

        /* Upload filter tab inactive buttons */
        main a.border.border-slate-200 { background: #252525 !important; }

        /* Progress bar tracks */
        main .bg-slate-200 { background: rgba(255,255,255,0.08) !important; }

        /* Pagination links */
        main nav[role=navigation] span,
        main nav[role=navigation] a {
            background: #1A1A1A !important;
            border-color: rgba(255,255,255,0.1) !important;
            color: #aaa !important;
        }
        main nav[role=navigation] a:hover { background: #252525 !important; }
        main nav[role=navigation] [aria-current=page] span {
            background: #6EDCD3 !important;
            border-color: #6EDCD3 !important;
            color: #0D0D0D !important;
        }

        /* ── Sidebar active nav link ──────────────────────────────────────────── */
        .nav-active { color: #6EDCD3 !important; background: rgba(110,220,211,0.1) !important; }
    </style>
</head>
<body class="h-full font-sans antialiased" style="background:#0D0D0D">

<div class="flex h-full min-h-screen">

    {{-- ── Sidebar ── --}}
    <aside class="w-60 flex-shrink-0 flex flex-col fixed inset-y-0 left-0 z-40"
           style="background:#111; border-right:1px solid rgba(255,255,255,0.06)">

        {{-- Logo --}}
        <div class="flex items-center gap-2 px-5 py-5" style="border-bottom:1px solid rgba(255,255,255,0.06)">
            <div class="w-7 h-7 rounded-lg flex items-center justify-center" style="background:#6EDCD3">
                <i class="bi bi-piggy-bank text-sm" style="color:#0D0D0D"></i>
            </div>
            <span class="font-semibold text-white tracking-tight">
                Budget<span style="color:#6EDCD3">Buddy</span>
            </span>
            <span class="ml-auto text-[10px] font-medium uppercase tracking-wider" style="color:#555">Admin</span>
        </div>

        {{-- Nav --}}
        <nav class="flex-1 py-4 px-3 space-y-0.5">

            <p class="text-[10px] font-semibold uppercase tracking-widest px-3 pt-1 pb-2" style="color:#555">Overview</p>
            <a href="{{ route('admin.dashboard') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.dashboard') ? 'nav-active' : '' }}"
               style="{{ request()->routeIs('admin.dashboard') ? '' : 'color:#777' }}"
               onmouseover="if(!this.classList.contains('nav-active')){this.style.color='#fff';this.style.background='rgba(255,255,255,0.05)'}"
               onmouseout="if(!this.classList.contains('nav-active')){this.style.color='#777';this.style.background=''}">
                <i class="bi bi-speedometer2 text-base"></i>
                Dashboard
            </a>
            <a href="{{ route('admin.insights') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.insights') ? 'nav-active' : '' }}"
               style="{{ request()->routeIs('admin.insights') ? '' : 'color:#777' }}"
               onmouseover="if(!this.classList.contains('nav-active')){this.style.color='#fff';this.style.background='rgba(255,255,255,0.05)'}"
               onmouseout="if(!this.classList.contains('nav-active')){this.style.color='#777';this.style.background=''}">
                <i class="bi bi-bar-chart-line text-base"></i>
                Insights
            </a>

            <p class="text-[10px] font-semibold uppercase tracking-widest px-3 pt-4 pb-2" style="color:#555">Manage</p>
            <a href="{{ route('admin.users.index') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.users.*') ? 'nav-active' : '' }}"
               style="{{ request()->routeIs('admin.users.*') ? '' : 'color:#777' }}"
               onmouseover="if(!this.classList.contains('nav-active')){this.style.color='#fff';this.style.background='rgba(255,255,255,0.05)'}"
               onmouseout="if(!this.classList.contains('nav-active')){this.style.color='#777';this.style.background=''}">
                <i class="bi bi-people text-base"></i>
                Users
            </a>
            <a href="{{ route('admin.transactions') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.transactions') ? 'nav-active' : '' }}"
               style="{{ request()->routeIs('admin.transactions') ? '' : 'color:#777' }}"
               onmouseover="if(!this.classList.contains('nav-active')){this.style.color='#fff';this.style.background='rgba(255,255,255,0.05)'}"
               onmouseout="if(!this.classList.contains('nav-active')){this.style.color='#777';this.style.background=''}">
                <i class="bi bi-receipt text-base"></i>
                Transactions
            </a>
            <a href="{{ route('admin.uploads.index') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.uploads.*') ? 'nav-active' : '' }}"
               style="{{ request()->routeIs('admin.uploads.*') ? '' : 'color:#777' }}"
               onmouseover="if(!this.classList.contains('nav-active')){this.style.color='#fff';this.style.background='rgba(255,255,255,0.05)'}"
               onmouseout="if(!this.classList.contains('nav-active')){this.style.color='#777';this.style.background=''}">
                <i class="bi bi-cloud-upload text-base"></i>
                Uploads
            </a>
            <a href="{{ route('admin.policies.index') }}"
               class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150
                      {{ request()->routeIs('admin.policies.*') ? 'nav-active' : '' }}"
               style="{{ request()->routeIs('admin.policies.*') ? '' : 'color:#777' }}"
               onmouseover="if(!this.classList.contains('nav-active')){this.style.color='#fff';this.style.background='rgba(255,255,255,0.05)'}"
               onmouseout="if(!this.classList.contains('nav-active')){this.style.color='#777';this.style.background=''}">
                <i class="bi bi-file-earmark-check text-base"></i>
                Policies
            </a>
        </nav>

        {{-- Sign out --}}
        <div class="px-3 pb-5 pt-3" style="border-top:1px solid rgba(255,255,255,0.06)">
            <form method="POST" action="{{ route('admin.logout') }}">
                @csrf
                <button class="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium
                               transition-all duration-150"
                        style="color:#777"
                        onmouseover="this.style.color='#fff';this.style.background='rgba(255,255,255,0.05)'"
                        onmouseout="this.style.color='#777';this.style.background=''">
                    <i class="bi bi-box-arrow-left text-base"></i>
                    Sign out
                </button>
            </form>
        </div>
    </aside>

    {{-- ── Main ── --}}
    <div class="flex-1 flex flex-col ml-60" style="background:#0D0D0D">

        {{-- Topbar --}}
        <header class="px-8 py-4 flex items-center justify-between sticky top-0 z-30"
                style="background:#111; border-bottom:1px solid rgba(255,255,255,0.06)">
            <h1 class="text-white font-semibold text-lg">@yield('page-title', 'Dashboard')</h1>
            <div class="flex items-center gap-2 text-sm" style="color:#555">
                <i class="bi bi-shield-lock"></i>
                <span>Admin Panel</span>
            </div>
        </header>

        {{-- Alerts --}}
        <div class="px-8 pt-4">
            @if(session('success'))
                <div class="flex items-start gap-3 p-4 mb-4 rounded-xl text-sm"
                     style="background:rgba(52,211,153,0.1);border:1px solid rgba(52,211,153,0.2);color:#34d399">
                    <i class="bi bi-check-circle-fill mt-0.5" style="color:#34d399"></i>
                    {{ session('success') }}
                </div>
            @endif
            @if(session('error'))
                <div class="flex items-start gap-3 p-4 mb-4 rounded-xl text-sm"
                     style="background:rgba(248,113,113,0.1);border:1px solid rgba(248,113,113,0.2);color:#f87171">
                    <i class="bi bi-exclamation-circle-fill mt-0.5" style="color:#f87171"></i>
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
