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

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Section 1: Users                                           --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<p class="text-xs font-semibold text-slate-400 uppercase tracking-widest mb-3">Users</p>
<div class="grid grid-cols-2 xl:grid-cols-3 gap-4 mb-8">

    @php
    $userCards = [
        ['label'=>'Total Users',       'value'=>$stats['total_users'],       'icon'=>'bi-people',       'bg'=>'bg-slate-100',   'color'=>'text-slate-600',   'val_color'=>'text-slate-900'],
        ['label'=>'Active',            'value'=>$stats['active_users'],      'icon'=>'bi-person-check', 'bg'=>'bg-emerald-50',  'color'=>'text-emerald-600', 'val_color'=>'text-emerald-600'],
        ['label'=>'Disabled',          'value'=>$stats['disabled_users'],    'icon'=>'bi-person-dash',  'bg'=>'bg-red-50',      'color'=>'text-red-500',     'val_color'=>'text-red-500'],
        ['label'=>'New This Week',     'value'=>$stats['new_this_week'],     'icon'=>'bi-person-plus',  'bg'=>'bg-blue-50',     'color'=>'text-blue-600',    'val_color'=>'text-blue-600'],
        ['label'=>'Active Today',      'value'=>$stats['active_today'],      'icon'=>'bi-lightning',    'bg'=>'bg-violet-50',   'color'=>'text-violet-600',  'val_color'=>'text-violet-600'],
        ['label'=>'Active This Week',  'value'=>$stats['active_this_week'],  'icon'=>'bi-calendar-week','bg'=>'bg-indigo-50',   'color'=>'text-indigo-600',  'val_color'=>'text-indigo-600'],
    ];
    @endphp

    @foreach($userCards as $card)
    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg {{ $card['bg'] }} flex items-center justify-center">
                <i class="bi {{ $card['icon'] }} {{ $card['color'] }}"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">{{ $card['label'] }}</span>
        </div>
        <div class="text-3xl font-bold {{ $card['val_color'] }}">{{ number_format($card['value']) }}</div>
    </div>
    @endforeach

</div>

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Section 2: Financial engagement                           --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<p class="text-xs font-semibold text-slate-400 uppercase tracking-widest mb-3">Money tracked across all users</p>
<div class="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-8">

    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg bg-slate-100 flex items-center justify-center">
                <i class="bi bi-receipt text-slate-600"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">Transactions</span>
        </div>
        <div class="text-3xl font-bold text-slate-900">{{ number_format($stats['total_transactions']) }}</div>
        <div class="text-xs text-slate-400 mt-1">avg {{ $stats['avg_tx_per_user'] }} / active user</div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg bg-emerald-50 flex items-center justify-center">
                <i class="bi bi-graph-up-arrow text-emerald-600"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">Income tracked</span>
        </div>
        <div class="text-2xl font-bold text-emerald-600">R{{ number_format($stats['total_income'], 0) }}</div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg bg-red-50 flex items-center justify-center">
                <i class="bi bi-graph-down-arrow text-red-500"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">Expenses tracked</span>
        </div>
        <div class="text-2xl font-bold text-red-500">R{{ number_format($stats['total_expense'], 0) }}</div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg {{ $stats['net_balance'] >= 0 ? 'bg-emerald-50' : 'bg-orange-50' }} flex items-center justify-center">
                <i class="bi bi-wallet2 {{ $stats['net_balance'] >= 0 ? 'text-emerald-600' : 'text-orange-500' }}"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">Net balance</span>
        </div>
        <div class="text-2xl font-bold {{ $stats['net_balance'] >= 0 ? 'text-emerald-600' : 'text-orange-500' }}">
            R{{ number_format($stats['net_balance'], 0) }}
        </div>
    </div>

</div>

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Section 3: Feature adoption                               --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<p class="text-xs font-semibold text-slate-400 uppercase tracking-widest mb-3">Feature adoption</p>
<div class="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-8">

    @php
    $adoption = [
        ['label'=>'Using transactions', 'value'=>$stats['users_with_tx'],      'of'=>$stats['total_users'], 'icon'=>'bi-receipt',          'color'=>'blue'],
        ['label'=>'Set goals',          'value'=>$stats['users_with_goals'],   'of'=>$stats['total_users'], 'icon'=>'bi-bullseye',         'color'=>'violet'],
        ['label'=>'Created budgets',    'value'=>$stats['users_with_budgets'], 'of'=>$stats['total_users'], 'icon'=>'bi-pie-chart',        'color'=>'indigo'],
        ['label'=>'Tracking debts',     'value'=>$stats['users_with_debts'],   'of'=>$stats['total_users'], 'icon'=>'bi-credit-card',      'color'=>'rose'],
    ];
    $colorMap = [
        'blue'   => ['bg'=>'bg-blue-50',   'text'=>'text-blue-600',   'bar'=>'bg-blue-500'],
        'violet' => ['bg'=>'bg-violet-50', 'text'=>'text-violet-600', 'bar'=>'bg-violet-500'],
        'indigo' => ['bg'=>'bg-indigo-50', 'text'=>'text-indigo-600', 'bar'=>'bg-indigo-500'],
        'rose'   => ['bg'=>'bg-rose-50',   'text'=>'text-rose-600',   'bar'=>'bg-rose-500'],
    ];
    @endphp

    @foreach($adoption as $a)
    @php
        $pct   = $a['of'] > 0 ? round($a['value'] / $a['of'] * 100) : 0;
        $c     = $colorMap[$a['color']];
    @endphp
    <div class="bg-white rounded-2xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-lg {{ $c['bg'] }} flex items-center justify-center">
                <i class="bi {{ $a['icon'] }} {{ $c['text'] }}"></i>
            </div>
            <span class="text-sm text-slate-500 font-medium">{{ $a['label'] }}</span>
        </div>
        <div class="flex items-end gap-2 mb-3">
            <span class="text-3xl font-bold text-slate-900">{{ $a['value'] }}</span>
            <span class="text-sm text-slate-400 mb-1">of {{ $a['of'] }}</span>
        </div>
        <div class="h-1.5 rounded-full bg-slate-100">
            <div class="h-1.5 rounded-full {{ $c['bar'] }}" style="width: {{ $pct }}%"></div>
        </div>
        <div class="text-xs text-slate-400 mt-1.5">{{ $pct }}% adoption</div>
    </div>
    @endforeach

</div>

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Section 4: Signup trend + bank statement stats            --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<div class="grid xl:grid-cols-3 gap-6 mb-8">

    {{-- Signup chart (2/3 width) --}}
    <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200 p-6">
        <div class="flex items-center justify-between mb-5">
            <div>
                <h2 class="text-sm font-semibold text-slate-900">User signups — last 14 days</h2>
                <p class="text-xs text-slate-400 mt-0.5">New accounts created per day</p>
            </div>
        </div>
        <div class="relative h-36">
            <canvas id="signupChart"></canvas>
        </div>
    </div>

    {{-- Bank statement uploads (1/3 width) --}}
    <div class="bg-white rounded-2xl border border-slate-200 p-6">
        <div class="flex items-center gap-3 mb-5">
            <div class="w-9 h-9 rounded-lg bg-blue-50 flex items-center justify-center">
                <i class="bi bi-cloud-upload text-blue-600"></i>
            </div>
            <div>
                <h2 class="text-sm font-semibold text-slate-900">Bank statement uploads</h2>
                <p class="text-xs text-slate-400">Via the user portal</p>
            </div>
        </div>

        <div class="space-y-4">
            <div class="flex items-center justify-between py-3 border-b border-slate-100">
                <span class="text-sm text-slate-500">Total uploads</span>
                <span class="text-lg font-bold text-slate-900">{{ number_format($stats['upload_stats']->total_uploads ?? 0) }}</span>
            </div>
            <div class="flex items-center justify-between py-3 border-b border-slate-100">
                <span class="text-sm text-slate-500">Users uploading</span>
                <span class="text-lg font-bold text-slate-900">{{ number_format($stats['upload_stats']->users_uploading ?? 0) }}</span>
            </div>
            <div class="flex items-center justify-between py-3">
                <span class="text-sm text-slate-500">Transactions extracted</span>
                <span class="text-lg font-bold text-blue-600">{{ number_format($stats['upload_stats']->total_rows ?? 0) }}</span>
            </div>
        </div>
    </div>
</div>

{{-- ═══════════════════════════════════════════════════════════ --}}
{{-- Section 5: Recent signups + quick actions                 --}}
{{-- ═══════════════════════════════════════════════════════════ --}}
<div class="grid xl:grid-cols-3 gap-6">

    {{-- Recent signups table (2/3) --}}
    <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 class="text-sm font-semibold text-slate-900">Recent sign-ups</h2>
            <a href="{{ route('admin.users.index') }}" class="text-xs text-blue-600 hover:underline">View all →</a>
        </div>

        @if(empty($stats['recent_signups']))
        <div class="px-6 py-10 text-center text-slate-400 text-sm">No users yet.</div>
        @else
        <div class="divide-y divide-slate-100">
            @foreach($stats['recent_signups'] as $u)
            @php
                $initials = $u['displayName']
                    ? strtoupper(substr($u['displayName'], 0, 1))
                    : strtoupper(substr($u['email'] ?? '?', 0, 1));
                $joinedAgo = $u['createdAt']
                    ? \Carbon\Carbon::createFromTimestampMs($u['createdAt'])->diffForHumans()
                    : '—';
                $lastSeen  = $u['lastSignIn']
                    ? \Carbon\Carbon::createFromTimestampMs($u['lastSignIn'])->diffForHumans()
                    : 'Never';
            @endphp
            <div class="flex items-center gap-4 px-6 py-3.5">
                <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                    <span class="text-xs font-semibold text-blue-700">{{ $initials }}</span>
                </div>
                <div class="flex-1 min-w-0">
                    <div class="text-sm font-medium text-slate-900 truncate">
                        {{ $u['displayName'] ?: $u['email'] }}
                    </div>
                    <div class="text-xs text-slate-400 truncate">{{ $u['email'] }}</div>
                </div>
                <div class="text-right flex-shrink-0">
                    <div class="text-xs text-slate-500">Joined {{ $joinedAgo }}</div>
                    <div class="text-xs text-slate-400">Last seen {{ $lastSeen }}</div>
                </div>
                @if($u['disabled'])
                <span class="text-[10px] font-medium px-2 py-0.5 bg-red-50 text-red-600 rounded-full border border-red-100">Disabled</span>
                @endif
                <a href="{{ route('admin.users.show', $u['uid']) }}"
                   class="text-slate-400 hover:text-blue-600 transition-colors ml-1">
                    <i class="bi bi-arrow-right-short text-lg"></i>
                </a>
            </div>
            @endforeach
        </div>
        @endif
    </div>

    {{-- Quick actions (1/3) --}}
    <div class="bg-white rounded-2xl border border-slate-200 p-6">
        <h2 class="text-sm font-semibold text-slate-900 mb-4">Quick actions</h2>
        <div class="space-y-3">
            <a href="{{ route('admin.users.index') }}"
               class="flex items-center gap-3 px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium
                      rounded-xl transition-colors duration-150">
                <i class="bi bi-people"></i>
                Manage Users
            </a>
            <a href="{{ route('portal.dashboard') }}" target="_blank"
               class="flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl transition-colors duration-150"
               style="background:rgba(255,255,255,0.06);color:#ccc"
               onmouseover="this.style.background='rgba(255,255,255,0.1)'"
               onmouseout="this.style.background='rgba(255,255,255,0.06)'">
                <i class="bi bi-box-arrow-up-right"></i>
                Open User Portal
            </a>
        </div>

        <div class="mt-6 pt-5 border-t border-slate-100">
            <p class="text-xs text-slate-400 mb-3">Platform snapshot</p>
            <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                    <span class="text-slate-500">Activation rate</span>
                    <span class="font-semibold text-slate-800">
                        @php $ar = $stats['total_users'] > 0 ? round($stats['users_with_tx'] / $stats['total_users'] * 100) : 0 @endphp
                        {{ $ar }}%
                    </span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-500">DAU / total</span>
                    <span class="font-semibold text-slate-800">
                        @php $dau = $stats['total_users'] > 0 ? round($stats['active_today'] / $stats['total_users'] * 100) : 0 @endphp
                        {{ $dau }}%
                    </span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-500">WAU / total</span>
                    <span class="font-semibold text-slate-800">
                        @php $wau = $stats['total_users'] > 0 ? round($stats['active_this_week'] / $stats['total_users'] * 100) : 0 @endphp
                        {{ $wau }}%
                    </span>
                </div>
            </div>
        </div>
    </div>

</div>

@endsection

@push('scripts')
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<script>
(function () {
    const labels = @json(array_keys($stats['signups_by_day']));
    const data   = @json(array_values($stats['signups_by_day']));
    const max    = Math.max(...data, 1);

    new Chart(document.getElementById('signupChart'), {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                data,
                backgroundColor: 'rgba(59,130,246,0.15)',
                borderColor:     'rgba(59,130,246,0.8)',
                borderWidth: 1.5,
                borderRadius: 4,
                borderSkipped: false,
            }],
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false }, tooltip: {
                callbacks: { label: ctx => ` ${ctx.raw} new user${ctx.raw !== 1 ? 's' : ''}` },
            }},
            scales: {
                x: { grid: { display: false }, ticks: { color: '#666', font: { size: 10 } } },
                y: {
                    min: 0, max: max + 1,
                    ticks: { color: '#666', font: { size: 10 }, stepSize: 1, precision: 0 },
                    grid: { color: 'rgba(255,255,255,0.06)' },
                },
            },
        },
    });
}());
</script>
@endpush
