<!DOCTYPE html>
<html lang="en" class="h-full">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign in — BudgetBuddy Admin</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
</head>
<body class="h-full font-sans antialiased">

<div class="min-h-screen grid lg:grid-cols-2">

    {{-- ── Left — hero panel ── --}}
    <div class="hidden lg:flex flex-col justify-between p-12 bg-gradient-to-br from-slate-800 to-slate-900 text-white">
        <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-xl bg-white/10 flex items-center justify-center">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
            </div>
            <span class="font-bold text-xl">Budget<span class="text-blue-400">Buddy</span></span>
        </div>

        <div>
            <h1 class="text-4xl font-bold leading-tight mb-4">
                Admin panel
            </h1>
            <p class="text-slate-400 text-lg leading-relaxed">
                Manage users, review financial activity, and keep the platform running smoothly from one place.
            </p>
        </div>

        <div class="flex gap-6">
            @foreach(['User management', 'Cloud sync', 'Secure access'] as $feat)
                <div class="flex items-center gap-2 text-sm text-slate-400">
                    <svg class="w-4 h-4 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                    {{ $feat }}
                </div>
            @endforeach
        </div>
    </div>

    {{-- ── Right — sign-in form ── --}}
    <div class="flex items-center justify-center p-8 lg:p-16">
        <div class="w-full max-w-sm">

            {{-- Mobile logo --}}
            <div class="flex items-center gap-2 mb-10 lg:hidden">
                <div class="w-8 h-8 rounded-xl bg-slate-900 flex items-center justify-center">
                    <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
                <span class="font-bold text-slate-900">Budget<span class="text-blue-600">Buddy</span></span>
            </div>

            <h2 class="text-2xl font-bold text-slate-900 mb-1">Admin sign in</h2>
            <p class="text-slate-500 text-sm mb-8">Restricted access — authorised personnel only</p>

            @if($errors->any())
                <div class="flex items-start gap-3 p-4 mb-6 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm">
                    <svg class="w-4 h-4 text-red-500 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>
                    </svg>
                    {{ $errors->first('password') }}
                </div>
            @endif

            <form method="POST" action="{{ route('admin.login.post') }}" class="space-y-5">
                @csrf
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1.5">Admin password</label>
                    <input type="password" name="password" autofocus
                        class="w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm text-slate-900
                               placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent
                               @error('password') border-red-400 ring-2 ring-red-100 @enderror"
                        placeholder="Enter admin password">
                </div>
                <button type="submit"
                    class="w-full py-2.5 px-4 bg-slate-900 hover:bg-slate-800 text-white text-sm font-semibold rounded-xl
                           transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-slate-500 focus:ring-offset-2">
                    Sign in
                </button>
            </form>

            <p class="text-center text-xs text-slate-400 mt-8">BudgetBuddy &copy; {{ date('Y') }}</p>
        </div>
    </div>
</div>

</body>
</html>
