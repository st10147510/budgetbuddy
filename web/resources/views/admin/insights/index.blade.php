@extends('admin.layout')

@section('title', 'Insights — BudgetBuddy Admin')
@section('page-title', 'Insights')

@section('content')

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Row 1: two charts side by side                            --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<div class="grid xl:grid-cols-3 gap-6 mb-8">

    {{-- Category spend (2/3) --}}
    <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200 p-6">
        <h2 class="text-sm font-semibold text-slate-900 mb-1">Top expense categories</h2>
        <p class="text-xs text-slate-400 mb-5">Total spend across all users, all time</p>
        <div class="relative h-64">
            <canvas id="categoryChart"></canvas>
        </div>
    </div>

    {{-- User health donut (1/3) --}}
    <div class="bg-white rounded-2xl border border-slate-200 p-6">
        <h2 class="text-sm font-semibold text-slate-900 mb-1">User financial health</h2>
        <p class="text-xs text-slate-400 mb-5">Net balance per user (income − expenses)</p>
        <div class="relative h-52">
            <canvas id="healthChart"></canvas>
        </div>
        <div class="mt-4 space-y-1.5 text-sm">
            <div class="flex justify-between"><span class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block"></span>Surplus</span><span class="font-semibold text-slate-700">{{ $surplus }}</span></div>
            <div class="flex justify-between"><span class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-red-500 inline-block"></span>Deficit</span><span class="font-semibold text-slate-700">{{ $deficit }}</span></div>
            <div class="flex justify-between"><span class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-slate-300 inline-block"></span>Break-even</span><span class="font-semibold text-slate-700">{{ $breakeven }}</span></div>
            <div class="flex justify-between"><span class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-slate-100 inline-block"></span>No activity</span><span class="font-semibold text-slate-700">{{ $noActivity }}</span></div>
        </div>
    </div>

</div>

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Row 2: Monthly trend                                      --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<div class="bg-white rounded-2xl border border-slate-200 p-6 mb-8">
    <h2 class="text-sm font-semibold text-slate-900 mb-1">Monthly transaction volume</h2>
    <p class="text-xs text-slate-400 mb-5">Total income and expenses logged per month across all users</p>
    <div class="relative h-52">
        <canvas id="monthlyChart"></canvas>
    </div>
</div>

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Row 3: Most active users                                  --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
    <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <h2 class="text-sm font-semibold text-slate-900">Most active users</h2>
        <span class="text-xs text-slate-400">By transaction count</span>
    </div>
    <div class="overflow-x-auto">
        <table class="w-full text-sm">
            <thead>
                <tr class="border-b border-slate-100 text-left">
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">#</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">User</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Transactions</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Income</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Expenses</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Net</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($topUsers as $uid => $u)
                <tr class="hover:bg-slate-50/60 transition-colors">
                    <td class="px-5 py-3.5 text-slate-400 tabular-nums">{{ $loop->iteration }}</td>
                    <td class="px-5 py-3.5">
                        <a href="{{ route('admin.users.show', $uid) }}"
                           class="text-blue-600 hover:underline truncate block max-w-[220px]">
                            {{ $u['email'] }}
                        </a>
                    </td>
                    <td class="px-5 py-3.5 font-semibold text-slate-900 tabular-nums">{{ number_format($u['txCount']) }}</td>
                    <td class="px-5 py-3.5 text-emerald-600 tabular-nums">R{{ number_format($u['income'], 0) }}</td>
                    <td class="px-5 py-3.5 text-red-500 tabular-nums">R{{ number_format($u['expense'], 0) }}</td>
                    <td class="px-5 py-3.5 font-semibold tabular-nums {{ $u['net'] >= 0 ? 'text-emerald-600' : 'text-red-500' }}">
                        R{{ number_format($u['net'], 0) }}
                    </td>
                </tr>
                @empty
                <tr>
                    <td colspan="6" class="px-5 py-12 text-center text-slate-400 text-sm">No transaction data yet.</td>
                </tr>
                @endforelse
            </tbody>
        </table>
    </div>
</div>

@endsection

@push('scripts')
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<script>
(function () {
    // ── Category spend ──────────────────────────────────────────────────
    @php
    $catNames  = [];
    $catAmounts= [];
    $defaultCats = [
        1=>'🛒 Food & Groceries',2=>'🚗 Transport',3=>'🎬 Entertainment',
        4=>'💊 Healthcare',5=>'💡 Utilities',6=>'🏠 Housing',
        7=>'📚 Education',8=>'👗 Clothing',9=>'💰 Savings',10=>'📦 Other',
    ];
    foreach($catSpend as $id => $amt) {
        $catNames[]   = $defaultCats[$id] ?? 'Category '.$id;
        $catAmounts[] = round($amt, 2);
    }
    @endphp
    new Chart(document.getElementById('categoryChart'), {
        type: 'bar',
        data: {
            labels: @json($catNames),
            datasets: [{
                data:            @json($catAmounts),
                backgroundColor: 'rgba(239,68,68,0.15)',
                borderColor:     'rgba(239,68,68,0.7)',
                borderWidth: 1.5,
                borderRadius: 4,
                borderSkipped: false,
            }],
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: { callbacks: { label: ctx => ` R${ctx.raw.toLocaleString()}` } },
            },
            scales: {
                x: { grid: { color: 'rgba(255,255,255,0.06)' }, ticks: { color: '#666', font: { size: 10 }, callback: v => 'R'+v.toLocaleString() } },
                y: { grid: { display: false }, ticks: { color: '#999', font: { size: 11 } } },
            },
        },
    });

    // ── Health donut ────────────────────────────────────────────────────
    new Chart(document.getElementById('healthChart'), {
        type: 'doughnut',
        data: {
            labels: ['Surplus', 'Deficit', 'Break-even', 'No activity'],
            datasets: [{
                data:             [{{ $surplus }}, {{ $deficit }}, {{ $breakeven }}, {{ $noActivity }}],
                backgroundColor:  ['rgba(16,185,129,0.8)','rgba(239,68,68,0.8)','rgba(148,163,184,0.6)','rgba(80,80,80,0.6)'],
                borderColor:      ['#10b981','#ef4444','#94a3b8','#555'],
                borderWidth: 1.5,
            }],
        },
        options: {
            cutout: '70%',
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
        },
    });

    // ── Monthly trend ───────────────────────────────────────────────────
    @php
    $mLabels  = array_keys($monthlyVolume);
    $mIncome  = array_column(array_values($monthlyVolume), 'income');
    $mExpense = array_column(array_values($monthlyVolume), 'expense');
    @endphp
    new Chart(document.getElementById('monthlyChart'), {
        type: 'bar',
        data: {
            labels: @json($mLabels),
            datasets: [
                {
                    label: 'Income',
                    data: @json($mIncome),
                    backgroundColor: 'rgba(16,185,129,0.25)',
                    borderColor:     'rgba(16,185,129,0.8)',
                    borderWidth: 1.5, borderRadius: 4, borderSkipped: false,
                },
                {
                    label: 'Expenses',
                    data: @json($mExpense),
                    backgroundColor: 'rgba(239,68,68,0.15)',
                    borderColor:     'rgba(239,68,68,0.7)',
                    borderWidth: 1.5, borderRadius: 4, borderSkipped: false,
                },
            ],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top', labels: { font: { size: 11 }, color: '#888', boxWidth: 12, padding: 16 } },
                tooltip: { callbacks: { label: ctx => ` R${ctx.raw.toLocaleString()}` } },
            },
            scales: {
                x: { grid: { display: false }, ticks: { color: '#666', font: { size: 10 } } },
                y: { grid: { color: 'rgba(255,255,255,0.06)' }, ticks: { color: '#666', font: { size: 10 }, callback: v => 'R'+v.toLocaleString() } },
            },
        },
    });
}());
</script>
@endpush
