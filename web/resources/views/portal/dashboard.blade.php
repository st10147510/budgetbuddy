@extends('portal.layout')

@section('title', 'Dashboard — BudgetBuddy')

@section('content')

{{-- Welcome hero --}}
<div class="bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl p-8 text-white mb-8">
    <h1 class="text-2xl font-bold mb-1">
        Good {{ now()->hour < 12 ? 'morning' : (now()->hour < 17 ? 'afternoon' : 'evening') }},
        {{ session('portal_user.displayName') ?? session('portal_user.email') }} 👋
    </h1>
    <p class="text-blue-200 text-sm">Here's a snapshot of your finances</p>

    {{-- This month --}}
    <p class="text-blue-300 text-xs font-semibold uppercase tracking-widest mt-6 mb-2">{{ $monthLabel }}</p>
    <div class="grid grid-cols-3 gap-3 mb-5">
        <div class="bg-white/10 rounded-xl p-4">
            <div class="text-blue-200 text-xs mb-1">Income</div>
            <div class="text-xl font-bold">R{{ number_format($monthlyIncome, 2) }}</div>
        </div>
        <div class="bg-white/10 rounded-xl p-4">
            <div class="text-blue-200 text-xs mb-1">Expenses</div>
            <div class="text-xl font-bold">R{{ number_format($monthlyExpense, 2) }}</div>
        </div>
        <div class="bg-white/10 rounded-xl p-4">
            <div class="text-blue-200 text-xs mb-1">Net</div>
            <div class="text-xl font-bold {{ $monthlyNet >= 0 ? 'text-emerald-300' : 'text-red-300' }}">
                R{{ number_format($monthlyNet, 2) }}
            </div>
        </div>
    </div>

    {{-- Divider --}}
    <div class="border-t border-white/20 mb-4"></div>

    {{-- All-time --}}
    <p class="text-blue-300 text-xs font-semibold uppercase tracking-widest mb-2">All time</p>
    <div class="grid grid-cols-3 gap-3">
        <div class="bg-white/10 rounded-xl p-4">
            <div class="text-blue-200 text-xs mb-1">Income</div>
            <div class="text-xl font-bold">R{{ number_format($totalIncome, 2) }}</div>
        </div>
        <div class="bg-white/10 rounded-xl p-4">
            <div class="text-blue-200 text-xs mb-1">Expenses</div>
            <div class="text-xl font-bold">R{{ number_format($totalExpense, 2) }}</div>
        </div>
        <div class="bg-white/10 rounded-xl p-4">
            <div class="text-blue-200 text-xs mb-1">Net</div>
            <div class="text-xl font-bold {{ $netBalance >= 0 ? 'text-emerald-300' : 'text-red-300' }}">
                R{{ number_format($netBalance, 2) }}
            </div>
        </div>
    </div>
</div>

<div class="grid lg:grid-cols-3 gap-6">

    {{-- Recent transactions --}}
    <div class="lg:col-span-2">
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-sm font-semibold text-slate-900">Recent Transactions</h2>
                <span class="text-xs text-slate-400">{{ $transactions->count() }} total</span>
            </div>
            <div class="divide-y divide-slate-100">
                @forelse($transactions->take(10) as $tx)
                    @php
                        $isIncome = ($tx['type'] ?? '') === 'INCOME';
                        $dateMs = $tx['date'] ?? 0;
                        $date = $dateMs ? \Carbon\Carbon::createFromTimestampMs($dateMs)->format('d M') : '—';
                        $cat = $categories->get($tx['categoryId'] ?? 0);
                        $catLabel = $cat ? ($cat['icon'].' '.$cat['name']) : '📦 Other';
                    @endphp
                    <div class="flex items-center gap-4 px-5 py-3.5 hover:bg-slate-50/60 transition-colors">
                        <div class="w-9 h-9 rounded-full {{ $isIncome ? 'bg-emerald-50' : 'bg-red-50' }} flex items-center justify-center flex-shrink-0">
                            <span class="text-base">{{ $cat['icon'] ?? '📦' }}</span>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="text-sm font-medium text-slate-900 truncate">{{ $catLabel }}</div>
                            <div class="text-xs text-slate-500">{{ $tx['notes'] ?: $date }}</div>
                        </div>
                        <div class="text-sm font-semibold {{ $isIncome ? 'text-emerald-600' : 'text-red-500' }}">
                            {{ $isIncome ? '+' : '-' }}R{{ number_format($tx['amount'] ?? 0, 2) }}
                        </div>
                    </div>
                @empty
                    <div class="px-5 py-12 text-center text-slate-400">
                        <i class="bi bi-receipt text-3xl block mb-3"></i>
                        <p class="text-sm">No transactions yet.</p>
                        <a href="{{ route('portal.upload') }}" class="mt-3 inline-flex items-center gap-1.5 text-sm text-blue-600 hover:underline">
                            Upload a bank statement →
                        </a>
                    </div>
                @endforelse
            </div>
        </div>
    </div>

    {{-- Sidebar --}}
    <div class="space-y-5">

        {{-- Upload CTA --}}
        <div class="bg-white rounded-2xl border border-slate-200 p-6">
            <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center mb-4">
                <i class="bi bi-cloud-upload text-blue-600 text-lg"></i>
            </div>
            <h3 class="font-semibold text-slate-900 mb-1 text-sm">Upload Bank Statement</h3>
            <p class="text-slate-500 text-xs leading-relaxed mb-4">
                Upload a PDF bank statement and we'll automatically extract and import your transactions.
            </p>
            <a href="{{ route('portal.upload') }}"
               class="w-full flex items-center justify-center gap-2 py-2.5 bg-blue-600 hover:bg-blue-700
                      text-white text-sm font-semibold rounded-xl transition-colors">
                <i class="bi bi-upload"></i> Upload Statement
            </a>
        </div>

        {{-- Upload jobs status --}}
        @if($jobs->count())
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
                <h3 class="text-sm font-semibold text-slate-900">Recent Uploads</h3>
            </div>
            <div class="divide-y divide-slate-100">
                @foreach($jobs as $job)
                    <div class="flex items-center gap-3 px-5 py-3">
                        @if($job->status === 'done')
                            <i class="bi bi-check-circle-fill text-emerald-500"></i>
                        @elseif($job->status === 'failed')
                            <i class="bi bi-x-circle-fill text-red-500"></i>
                        @else
                            <i class="bi bi-hourglass-split text-amber-500 animate-spin"></i>
                        @endif
                        <div class="flex-1 min-w-0">
                            <div class="text-xs font-medium text-slate-900 truncate">{{ $job->filename }}</div>
                            <div class="text-[10px] text-slate-400">{{ $job->created_at->diffForHumans() }}</div>
                        </div>
                        @if($job->status === 'done')
                            <span class="text-[10px] text-emerald-600 font-medium">{{ $job->rows_imported }} rows</span>
                        @elseif($job->status === 'failed')
                            <span class="text-[10px] text-red-600 font-medium">Failed</span>
                        @else
                            <span class="text-[10px] text-amber-600 font-medium">Processing…</span>
                        @endif
                    </div>
                @endforeach
            </div>
        </div>
        @endif

        {{-- Goals summary --}}
        @if($goals->count())
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100">
                <h3 class="text-sm font-semibold text-slate-900">Savings Goals</h3>
            </div>
            <div class="divide-y divide-slate-100">
                @foreach($goals->take(3) as $g)
                    @php
                        $pct = ($g['targetAmount'] ?? 0) > 0
                            ? min(100, round(($g['savedAmount'] ?? 0) / $g['targetAmount'] * 100)) : 0;
                    @endphp
                    <div class="px-5 py-3.5">
                        <div class="flex justify-between text-xs mb-2">
                            <span class="font-medium text-slate-900">{{ $g['name'] ?? '—' }}</span>
                            <span class="text-slate-500">{{ $pct }}%</span>
                        </div>
                        <div class="h-1.5 bg-slate-100 rounded-full overflow-hidden">
                            <div class="h-full bg-emerald-500 rounded-full transition-all" style="width:{{ $pct }}%"></div>
                        </div>
                        <div class="flex justify-between text-[10px] text-slate-400 mt-1">
                            <span>R{{ number_format($g['savedAmount'] ?? 0, 0) }}</span>
                            <span>R{{ number_format($g['targetAmount'] ?? 0, 0) }}</span>
                        </div>
                    </div>
                @endforeach
            </div>
        </div>
        @endif

    </div>
</div>

@endsection
