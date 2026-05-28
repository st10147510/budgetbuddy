@extends('admin.layout')

@section('title', 'Dashboard — BudgetBuddy Admin')
@section('page-title', 'Dashboard')

@section('content')

@if(!$firebaseConfigured)
<div class="flex items-start gap-3 p-4 mb-6 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm">
    <svg class="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
    </svg>
    <div>
        <strong class="font-semibold">Firebase not configured.</strong>
        Place your service account JSON at <code class="bg-amber-100 px-1 rounded text-xs">web/firebase-service-account.json</code>
        and set <code class="bg-amber-100 px-1 rounded text-xs">FIREBASE_CREDENTIALS=firebase-service-account.json</code> in
        <code class="bg-amber-100 px-1 rounded text-xs">web/.env</code>.
    </div>
</div>
@endif

{{-- Stat cards --}}
<div class="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-8">
    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg bg-slate-100 flex items-center justify-center">
                <i class="bi bi-people text-slate-600"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">Total Users</span>
        </div>
        <div class="text-3xl font-bold text-slate-900">{{ number_format($stats['total_users']) }}</div>
    </div>
    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg bg-emerald-50 flex items-center justify-center">
                <i class="bi bi-person-check text-emerald-600"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">Active</span>
        </div>
        <div class="text-3xl font-bold text-emerald-600">{{ number_format($stats['active_users']) }}</div>
    </div>
    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg bg-red-50 flex items-center justify-center">
                <i class="bi bi-person-dash text-red-500"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">Disabled</span>
        </div>
        <div class="text-3xl font-bold text-red-500">{{ number_format($stats['disabled_users']) }}</div>
    </div>
    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg bg-blue-50 flex items-center justify-center">
                <i class="bi bi-person-plus text-blue-600"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">New This Week</span>
        </div>
        <div class="text-3xl font-bold text-blue-600">{{ number_format($stats['new_this_week']) }}</div>
    </div>
</div>

{{-- Quick actions --}}
<div class="bg-white rounded-2xl border border-slate-200 p-6">
    <h2 class="text-sm font-semibold text-slate-900 mb-4">Quick actions</h2>
    <div class="flex flex-wrap gap-3">
        <a href="{{ route('admin.users.index') }}"
           class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium
                  rounded-lg transition-colors duration-150">
            <i class="bi bi-people"></i>
            Manage Users
        </a>
    </div>
</div>

@endsection
