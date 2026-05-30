<!DOCTYPE html>
<html lang="en" class="h-full">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign in — BudgetBuddy</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        body { color: #fff; font-family: 'Inter', sans-serif; }
        .hero-panel {
            background: linear-gradient(135deg, #1A1A1A 0%, #0D0D0D 100%);
            border-right: 1px solid rgba(255,255,255,0.06);
            position: relative;
            overflow: hidden;
        }
        .hero-panel::before {
            content: '';
            position: absolute;
            top: -120px; right: -120px;
            width: 400px; height: 400px;
            background: radial-gradient(circle, rgba(110,220,211,0.12) 0%, transparent 70%);
            border-radius: 50%;
        }
        .hero-panel::after {
            content: '';
            position: absolute;
            bottom: -80px; left: -80px;
            width: 300px; height: 300px;
            background: radial-gradient(circle, rgba(232,132,122,0.08) 0%, transparent 70%);
            border-radius: 50%;
        }
    </style>
</head>
<body class="h-full font-sans antialiased">

<div class="min-h-screen grid lg:grid-cols-2">

    {{-- ── Left — dark hero ── --}}
    <div class="hidden lg:flex flex-col justify-between p-12 hero-panel relative z-10">

        {{-- Logo --}}
        <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-xl flex items-center justify-center" style="background:#6EDCD3">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="#0D0D0D" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
            </div>
            <span class="font-bold text-xl text-white">Budget<span style="color:#6EDCD3">Buddy</span></span>
        </div>

        {{-- Headline --}}
        <div>
            <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-medium mb-6"
                 style="background:rgba(110,220,211,0.1);color:#6EDCD3;border:1px solid rgba(110,220,211,0.2)">
                <span class="w-1.5 h-1.5 rounded-full bg-bb-teal inline-block" style="background:#6EDCD3"></span>
                Your finances, simplified
            </div>
            <h1 class="text-4xl font-bold leading-tight mb-4 text-white">
                Take control of<br>your money
            </h1>
            <p class="text-lg leading-relaxed" style="color:#999">
                Upload your bank statements and we'll automatically extract, categorise, and visualise your transactions.
            </p>
        </div>

        {{-- Feature pills --}}
        <div class="space-y-3">
            @foreach([
                ['bi-lightning-charge', 'Smart extraction', 'PDF statements parsed in seconds'],
                ['bi-tag',             'Auto-categorise',   'Transactions grouped automatically'],
                ['bi-shield-lock',     'Secure & private',  'Your data stays yours'],
            ] as [$icon, $title, $sub])
            <div class="flex items-center gap-3 p-3 rounded-xl" style="background:rgba(255,255,255,0.04)">
                <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                     style="background:rgba(110,220,211,0.12)">
                    <i class="bi {{ $icon }} text-sm" style="color:#6EDCD3"></i>
                </div>
                <div>
                    <div class="text-sm font-medium text-white">{{ $title }}</div>
                    <div class="text-xs" style="color:#666">{{ $sub }}</div>
                </div>
            </div>
            @endforeach
        </div>
    </div>

    {{-- ── Right — sign-in form ── --}}
    <div class="flex items-center justify-center p-8 lg:p-16">
        <div class="w-full max-w-sm">

            {{-- Mobile logo --}}
            <div class="flex items-center gap-2 mb-10 lg:hidden">
                <div class="w-8 h-8 rounded-xl flex items-center justify-center" style="background:#6EDCD3">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="#0D0D0D" stroke-width="2.5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
                <span class="font-bold text-white">Budget<span style="color:#6EDCD3">Buddy</span></span>
            </div>

            <h2 class="text-2xl font-bold text-white mb-1">Welcome back</h2>
            <p class="text-sm mb-8" style="color:#666">Sign in to your BudgetBuddy account</p>

            @if(session('error'))
            <div class="flex items-start gap-3 p-4 mb-6 rounded-xl text-sm"
                 style="background:rgba(244,67,54,0.12);border:1px solid rgba(244,67,54,0.2);color:#ef9a9a">
                <svg class="w-4 h-4 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20" style="color:#F44336">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>
                </svg>
                {{ session('error') }}
            </div>
            @endif

            <form method="POST" action="{{ route('portal.login.post') }}" class="space-y-5">
                @csrf
                <div>
                    <label class="block text-sm font-medium mb-1.5" style="color:#ccc">Email address</label>
                    <input type="email" name="email" value="{{ old('email') }}" autofocus required
                           class="bb-input @error('email') !border-red-500 @enderror"
                           placeholder="you@example.com">
                    @error('email')
                        <p class="mt-1.5 text-xs" style="color:#F44336">{{ $message }}</p>
                    @enderror
                </div>
                <div>
                    <label class="block text-sm font-medium mb-1.5" style="color:#ccc">Password</label>
                    <input type="password" name="password" required
                           class="bb-input @error('password') !border-red-500 @enderror"
                           placeholder="••••••••">
                    @error('password')
                        <p class="mt-1.5 text-xs" style="color:#F44336">{{ $message }}</p>
                    @enderror
                </div>
                <button type="submit" class="bb-btn w-full">
                    Sign in
                </button>
            </form>

            <p class="text-center text-xs mt-8" style="color:#555">
                Don't have an account? Download the <strong style="color:#999">BudgetBuddy</strong> app.
            </p>
        </div>
    </div>

</div>
</body>
</html>
