@extends('admin.layout')

@section('title', 'Users — BudgetBuddy Admin')
@section('page-title', 'Users')

@section('content')

@if(!$firebaseConfigured)
<div class="flex items-start gap-3 p-4 mb-6 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm">
    <svg class="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
    </svg>
    <div><strong class="font-semibold">Firebase not configured.</strong> Place your service account JSON at
        <code class="bg-amber-100 px-1 rounded text-xs">web/firebase-service-account.json</code> and set
        <code class="bg-amber-100 px-1 rounded text-xs">FIREBASE_CREDENTIALS</code> in <code class="bg-amber-100 px-1 rounded text-xs">web/.env</code>.
    </div>
</div>
@endif

<div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">

    {{-- Header --}}
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 px-6 py-4 border-b border-slate-100">
        <div>
            <h2 class="text-sm font-semibold text-slate-900">All users</h2>
            <p class="text-xs text-slate-500 mt-0.5">{{ number_format($total) }} total accounts</p>
        </div>
        <form method="GET" action="{{ route('admin.users.index') }}" class="flex items-center gap-2">
            <div class="relative">
                <i class="bi bi-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm"></i>
                <input type="text" name="search" value="{{ $search }}"
                    placeholder="Search email or name…"
                    class="pl-9 pr-4 py-2 text-sm rounded-lg border border-slate-300 bg-white text-slate-900
                           placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent w-56">
            </div>
            <button type="submit"
                class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors">
                Search
            </button>
            @if($search)
                <a href="{{ route('admin.users.index') }}"
                   class="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-900 rounded-lg border border-slate-300
                          hover:border-slate-400 transition-colors">
                    Clear
                </a>
            @endif
        </form>
        <a href="{{ route('admin.users.export') }}"
           class="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-900
                  rounded-lg border border-slate-300 hover:border-slate-400 bg-white transition-colors">
            <i class="bi bi-download text-sm"></i>
            Export CSV
        </a>
    </div>

    {{-- Table --}}
    <div class="overflow-x-auto">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-slate-50 text-left">
                    <th class="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">User</th>
                    <th class="px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Status</th>
                    <th class="px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Signed up</th>
                    <th class="px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Last sign-in</th>
                    <th class="px-6 py-3"></th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($users as $user)
                    <tr class="hover:bg-slate-50/60 transition-colors">
                        <td class="px-6 py-3.5">
                            <div class="font-medium text-slate-900">{{ $user['email'] ?? '—' }}</div>
                            @if($user['display_name'])
                                <div class="text-xs text-slate-500 mt-0.5">{{ $user['display_name'] }}</div>
                            @endif
                        </td>
                        <td class="px-4 py-3.5">
                            @if($user['disabled'])
                                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-700 border border-red-200">
                                    <span class="w-1.5 h-1.5 rounded-full bg-red-500"></span>Disabled
                                </span>
                            @else
                                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>Active
                                </span>
                            @endif
                        </td>
                        <td class="px-4 py-3.5 text-slate-500 text-xs">{{ $user['created_at'] ?? '—' }}</td>
                        <td class="px-4 py-3.5 text-slate-500 text-xs">{{ $user['last_sign_in'] ?? 'Never' }}</td>
                        <td class="px-6 py-3.5 text-right">
                            <a href="{{ route('admin.users.show', $user['uid']) }}"
                               class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-600
                                      border border-slate-300 rounded-lg hover:border-slate-400 hover:text-slate-900 transition-colors">
                                View
                                <i class="bi bi-arrow-right text-xs"></i>
                            </a>
                        </td>
                    </tr>
                @empty
                    <tr>
                        <td colspan="5" class="px-6 py-12 text-center text-slate-400">
                            <i class="bi bi-people text-3xl mb-3 block"></i>
                            No users found.
                        </td>
                    </tr>
                @endforelse
            </tbody>
        </table>
    </div>

    {{-- Pagination --}}
    @if($lastPage > 1)
        <div class="flex items-center justify-between px-6 py-3.5 border-t border-slate-100 bg-slate-50/50">
            <span class="text-xs text-slate-500">Page {{ $page }} of {{ $lastPage }}</span>
            <div class="flex gap-2">
                @if($page > 1)
                    <a href="{{ route('admin.users.index', ['page' => $page - 1, 'search' => $search]) }}"
                       class="px-3 py-1.5 text-xs font-medium text-slate-600 border border-slate-300 rounded-lg hover:border-slate-400 transition-colors">
                        ← Prev
                    </a>
                @endif
                @if($page < $lastPage)
                    <a href="{{ route('admin.users.index', ['page' => $page + 1, 'search' => $search]) }}"
                       class="px-3 py-1.5 text-xs font-medium text-slate-600 border border-slate-300 rounded-lg hover:border-slate-400 transition-colors">
                        Next →
                    </a>
                @endif
            </div>
        </div>
    @endif
</div>

@endsection
