@extends('portal.layout')

@section('title', 'Dashboard — BudgetBuddy')

@section('content')

{{-- ── Welcome hero ── --}}
<div class="rounded-2xl p-8 mb-8" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.06)">
    <h1 class="text-2xl font-bold text-white mb-1">
        Good {{ now()->hour < 12 ? 'morning' : (now()->hour < 17 ? 'afternoon' : 'evening') }},
        {{ session('portal_user.displayName') ?? session('portal_user.email') }} 👋
    </h1>
    <p class="text-sm mb-6" style="color:#999">Here's a snapshot of your finances</p>

    {{-- This month --}}
    <p class="text-xs font-semibold uppercase tracking-widest mb-2" style="color:#6EDCD3">{{ $monthLabel }}</p>
    <div class="grid grid-cols-3 gap-3 mb-6">
        @foreach([
            ['Income',   $monthlyIncome,  '#4CAF50'],
            ['Expenses', $monthlyExpense, '#F44336'],
            ['Net',      $monthlyNet,     $monthlyNet >= 0 ? '#4CAF50' : '#F44336'],
        ] as [$label, $val, $col])
        <div class="rounded-xl p-4" style="background:#2A2A2A">
            <div class="text-xs mb-1" style="color:#666">{{ $label }}</div>
            <div class="text-xl font-bold" style="color:{{ $col }}">R{{ number_format($val, 2) }}</div>
        </div>
        @endforeach
    </div>

    {{-- All-time --}}
    <p class="text-xs font-semibold uppercase tracking-widest mb-2" style="color:#999">All time</p>
    <div class="grid grid-cols-3 gap-3">
        @foreach([
            ['Income',   $totalIncome,  '#4CAF50'],
            ['Expenses', $totalExpense, '#F44336'],
            ['Net',      $netBalance,   $netBalance >= 0 ? '#4CAF50' : '#F44336'],
        ] as [$label, $val, $col])
        <div class="rounded-xl p-4" style="background:#2A2A2A">
            <div class="text-xs mb-1" style="color:#666">{{ $label }}</div>
            <div class="text-xl font-bold" style="color:{{ $col }}">R{{ number_format($val, 2) }}</div>
        </div>
        @endforeach
    </div>
</div>

<div class="grid lg:grid-cols-3 gap-6">

    {{-- ── Recent transactions ── --}}
    <div class="lg:col-span-2">
        <div class="overflow-hidden rounded-2xl" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.06)">
            <div class="px-5 py-4 flex items-center justify-between" style="border-bottom:1px solid rgba(255,255,255,0.06)">
                <h2 class="text-sm font-semibold text-white">Recent Transactions</h2>
                <span class="text-xs" style="color:#555">{{ $transactions->count() }} total</span>
            </div>
            <div>
                @forelse($transactions->take(10) as $tx)
                    @php
                        $isIncome = ($tx['type'] ?? '') === 'INCOME';
                        $dateMs   = $tx['date'] ?? 0;
                        $date     = $dateMs ? \Carbon\Carbon::createFromTimestampMs($dateMs)->format('d M') : '—';
                        $cat      = $categories->get($tx['categoryId'] ?? 0);
                        $catLabel = $cat ? ($cat['icon'].' '.$cat['name']) : '📦 Other';
                    @endphp
                    <div class="flex items-center gap-4 px-5 py-3.5 transition-colors"
                         style="border-bottom:1px solid rgba(255,255,255,0.04)"
                         onmouseover="this.style.background='rgba(255,255,255,0.03)'"
                         onmouseout="this.style.background='transparent'">
                        <div class="w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0"
                             style="background:{{ $isIncome ? 'rgba(76,175,80,0.15)' : 'rgba(244,67,54,0.12)' }}">
                            <span class="text-base">{{ $cat['icon'] ?? '📦' }}</span>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="text-sm font-medium text-white truncate">{{ $catLabel }}</div>
                            <div class="text-xs truncate" style="color:#666">{{ $tx['notes'] ?: $date }}</div>
                        </div>
                        <div class="text-sm font-semibold" style="color:{{ $isIncome ? '#4CAF50' : '#F44336' }}">
                            {{ $isIncome ? '+' : '-' }}R{{ number_format($tx['amount'] ?? 0, 2) }}
                        </div>
                    </div>
                @empty
                    <div class="px-5 py-12 text-center" style="color:#555">
                        <i class="bi bi-receipt text-3xl block mb-3"></i>
                        <p class="text-sm">No transactions yet.</p>
                        <a href="{{ route('portal.upload') }}" class="mt-3 inline-flex items-center gap-1.5 text-sm"
                           style="color:#6EDCD3">
                            Upload a bank statement →
                        </a>
                    </div>
                @endforelse
            </div>
        </div>
    </div>

    {{-- ── Sidebar ── --}}
    <div class="space-y-5">

        {{-- Upload CTA --}}
        <div class="rounded-2xl p-6" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.06)">
            <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-4"
                 style="background:rgba(110,220,211,0.12)">
                <i class="bi bi-cloud-upload text-lg" style="color:#6EDCD3"></i>
            </div>
            <h3 class="font-semibold text-white mb-1 text-sm">Upload Bank Statement</h3>
            <p class="text-xs leading-relaxed mb-4" style="color:#666">
                Upload a PDF bank statement and we'll automatically extract and import your transactions.
            </p>
            <a href="{{ route('portal.upload') }}" class="bb-btn w-full">
                <i class="bi bi-upload"></i> Upload Statement
            </a>
        </div>

        {{-- Upload jobs --}}
        @if($jobs->count())
        <div class="overflow-hidden rounded-2xl" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.06)">
            <div class="px-5 py-4" style="border-bottom:1px solid rgba(255,255,255,0.06)">
                <h3 class="text-sm font-semibold text-white">Recent Uploads</h3>
            </div>
            <div>
                @foreach($jobs as $job)
                    <div class="flex items-center gap-3 px-5 py-3"
                         style="border-bottom:1px solid rgba(255,255,255,0.04)">
                        @if($job->status === 'done')
                            <i class="bi bi-check-circle-fill" style="color:#4CAF50"></i>
                        @elseif($job->status === 'failed')
                            <i class="bi bi-x-circle-fill" style="color:#F44336"></i>
                        @else
                            <i class="bi bi-hourglass-split animate-spin" style="color:#F5D5A8"></i>
                        @endif
                        <div class="flex-1 min-w-0">
                            <div class="text-xs font-medium text-white truncate">{{ $job->filename }}</div>
                            <div class="text-[10px]" style="color:#555">{{ $job->created_at->diffForHumans() }}</div>
                        </div>
                        @if($job->status === 'done')
                            <span class="text-[10px] font-medium" style="color:#4CAF50">{{ $job->rows_imported }} rows</span>
                        @elseif($job->status === 'failed')
                            <span class="text-[10px] font-medium" style="color:#F44336">Failed</span>
                        @else
                            <span class="text-[10px] font-medium" style="color:#F5D5A8">Processing…</span>
                        @endif
                    </div>
                @endforeach
            </div>
        </div>
        @endif

        {{-- Goals summary --}}
        @if($goals->count())
        <div class="overflow-hidden rounded-2xl" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.06)">
            <div class="px-5 py-4" style="border-bottom:1px solid rgba(255,255,255,0.06)">
                <h3 class="text-sm font-semibold text-white">Savings Goals</h3>
            </div>
            <div>
                @foreach($goals->take(3) as $g)
                    @php
                        $pct = ($g['targetAmount'] ?? 0) > 0
                            ? min(100, round(($g['savedAmount'] ?? 0) / $g['targetAmount'] * 100)) : 0;
                    @endphp
                    <div class="px-5 py-3.5" style="border-bottom:1px solid rgba(255,255,255,0.04)">
                        <div class="flex justify-between text-xs mb-2">
                            <span class="font-medium text-white">{{ $g['name'] ?? '—' }}</span>
                            <span style="color:#6EDCD3">{{ $pct }}%</span>
                        </div>
                        <div class="h-1.5 rounded-full" style="background:#2A2A2A">
                            <div class="h-full rounded-full transition-all" style="width:{{ $pct }}%;background:#6EDCD3"></div>
                        </div>
                        <div class="flex justify-between text-[10px] mt-1" style="color:#555">
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
