@extends('admin.layout')

@section('title', ($user->displayName ?? $user->email) . ' — BudgetBuddy Admin')
@section('page-title', 'User Detail')

@section('content')

<div class="flex flex-col xl:flex-row gap-6">

    {{-- ── Left column ── --}}
    <div class="xl:w-72 flex-shrink-0 space-y-4">

        {{-- Profile card --}}
        <div class="bg-white rounded-2xl border border-slate-200 p-5">
            <div class="flex items-center gap-3 mb-4">
                @if(!empty($profile['photoUrl']))
                    <img src="{{ $profile['photoUrl'] }}" alt="avatar"
                         class="w-12 h-12 rounded-full object-cover ring-2 ring-slate-100">
                @else
                    <div class="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center
                                text-blue-700 font-bold text-lg flex-shrink-0">
                        {{ strtoupper(substr($user->email ?? 'U', 0, 1)) }}
                    </div>
                @endif
                <div class="min-w-0">
                    <div class="font-semibold text-slate-900 truncate">{{ $user->displayName ?? 'No Name' }}</div>
                    <div class="text-slate-500 text-xs truncate">{{ $user->email ?? 'No Email' }}</div>
                </div>
            </div>

            <div class="space-y-2 text-xs">
                <div class="flex justify-between py-1.5 border-b border-slate-100">
                    <span class="text-slate-500">UID</span>
                    <span class="font-mono text-slate-700 text-[10px] max-w-[140px] truncate">{{ $user->uid }}</span>
                </div>
                <div class="flex justify-between py-1.5 border-b border-slate-100">
                    <span class="text-slate-500">Status</span>
                    @if($user->disabled)
                        <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-700 border border-red-200">
                            <span class="w-1.5 h-1.5 rounded-full bg-red-500"></span>Disabled
                        </span>
                    @else
                        <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>Active
                        </span>
                    @endif
                </div>
                <div class="flex justify-between py-1.5 border-b border-slate-100">
                    <span class="text-slate-500">Signed Up</span>
                    <span class="text-slate-700">{{ $user->metadata->createdAt?->format('Y-m-d') ?? '—' }}</span>
                </div>
                <div class="flex justify-between py-1.5">
                    <span class="text-slate-500">Last Sign-in</span>
                    <span class="text-slate-700">{{ $user->metadata->lastLoginAt?->format('Y-m-d') ?? 'Never' }}</span>
                </div>
            </div>

            <div class="mt-4 space-y-2">
                @if($user->disabled)
                    <form method="POST" action="{{ route('admin.users.enable', $user->uid) }}">
                        @csrf
                        <button class="w-full flex items-center justify-center gap-2 py-2 px-4 bg-emerald-600 hover:bg-emerald-700
                                       text-white text-sm font-medium rounded-lg transition-colors">
                            <i class="bi bi-person-check"></i> Enable Account
                        </button>
                    </form>
                @else
                    <form method="POST" action="{{ route('admin.users.disable', $user->uid) }}">
                        @csrf
                        <button class="w-full flex items-center justify-center gap-2 py-2 px-4 bg-amber-500 hover:bg-amber-600
                                       text-white text-sm font-medium rounded-lg transition-colors">
                            <i class="bi bi-person-dash"></i> Disable Account
                        </button>
                    </form>
                @endif

                @if($user->email)
                    <form method="POST" action="{{ route('admin.users.reset-password', $user->uid) }}">
                        @csrf
                        <button class="w-full flex items-center justify-center gap-2 py-2 px-4 border border-slate-300
                                       text-slate-700 text-sm font-medium rounded-lg hover:border-slate-400 hover:bg-slate-50 transition-colors">
                            <i class="bi bi-envelope"></i> Send Password Reset
                        </button>
                    </form>
                @endif

                <form method="POST" action="{{ route('admin.users.destroy', $user->uid) }}"
                      onsubmit="return confirm('Permanently delete this user and all their data?')">
                    @csrf @method('DELETE')
                    <button class="w-full flex items-center justify-center gap-2 py-2 px-4 border border-red-300
                                   text-red-600 text-sm font-medium rounded-lg hover:bg-red-50 transition-colors">
                        <i class="bi bi-trash"></i> Delete User
                    </button>
                </form>

                <a href="{{ route('admin.users.index') }}"
                   class="w-full flex items-center justify-center gap-2 py-2 px-4 text-slate-500 text-sm font-medium
                          rounded-lg hover:bg-slate-50 transition-colors">
                    ← Back to Users
                </a>
            </div>
        </div>

        {{-- Badges --}}
        @if($badges->count())
        <div class="bg-white rounded-2xl border border-slate-200 p-5">
            <h3 class="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-3">
                <i class="bi bi-patch-check text-amber-500 mr-1"></i>Badges Earned
            </h3>
            <div class="flex flex-wrap gap-2">
                @foreach($badges as $badge)
                    @php $label = \App\Http\Controllers\Admin\UserController::badgeLabel($badge['badgeType'] ?? ''); @endphp
                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium
                                 bg-slate-100 text-slate-700 border border-slate-200">
                        {{ $label[0] }} {{ $label[1] }}
                    </span>
                @endforeach
            </div>
        </div>
        @endif

    </div>

    {{-- ── Right column ── --}}
    <div class="flex-1 min-w-0 space-y-5">

        {{-- Summary stat row --}}
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
            @foreach([
                ['icon' => 'bi-receipt', 'label' => 'Transactions', 'val' => $transactions->count(), 'color' => 'blue'],
                ['icon' => 'bi-wallet2',  'label' => 'Budgets',      'val' => $budgets->count(),      'color' => 'violet'],
                ['icon' => 'bi-bullseye','label' => 'Goals',         'val' => $goals->count(),        'color' => 'emerald'],
                ['icon' => 'bi-credit-card','label' => 'Debts',      'val' => $debts->count(),        'color' => 'rose'],
            ] as $s)
                <div class="bg-white rounded-xl border border-slate-200 p-4 text-center">
                    <i class="bi {{ $s['icon'] }} text-{{ $s['color'] }}-500 text-lg block mb-1"></i>
                    <div class="text-2xl font-bold text-slate-900">{{ $s['val'] }}</div>
                    <div class="text-xs text-slate-500 mt-0.5">{{ $s['label'] }}</div>
                </div>
            @endforeach
        </div>

        {{-- Income / Expense / Net --}}
        <div class="grid grid-cols-3 gap-3">
            <div class="bg-white rounded-xl border border-slate-200 p-4 text-center">
                <div class="text-xs text-slate-500 mb-1">Total Income</div>
                <div class="text-xl font-bold text-emerald-600">R{{ number_format($totalIncome, 2) }}</div>
            </div>
            <div class="bg-white rounded-xl border border-slate-200 p-4 text-center">
                <div class="text-xs text-slate-500 mb-1">Total Expenses</div>
                <div class="text-xl font-bold text-red-500">R{{ number_format($totalExpense, 2) }}</div>
            </div>
            <div class="bg-white rounded-xl border border-slate-200 p-4 text-center">
                <div class="text-xs text-slate-500 mb-1">Net Balance</div>
                <div class="text-xl font-bold {{ $netBalance >= 0 ? 'text-emerald-600' : 'text-red-500' }}">
                    R{{ number_format($netBalance, 2) }}
                </div>
            </div>
        </div>

        {{-- Transactions table --}}
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2">
                <i class="bi bi-receipt text-slate-400"></i>
                <h3 class="text-sm font-semibold text-slate-900">Transactions</h3>
                <span class="ml-auto text-xs text-slate-400">({{ $transactions->count() }})</span>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full text-sm">
                    <thead class="bg-slate-50">
                        <tr>
                            @foreach(['Amount','Category','Type','Date','Notes'] as $h)
                                <th class="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">{{ $h }}</th>
                            @endforeach
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                        @forelse($transactions as $tx)
                            @php
                                $cat = $categories->get($tx['categoryId'] ?? 0);
                                $catLabel = $cat ? ($cat['icon'].' '.$cat['name']) : 'Cat #'.($tx['categoryId'] ?? '?');
                                $isIncome = ($tx['type'] ?? '') === 'INCOME';
                                $dateMs = $tx['date'] ?? 0;
                                $date = $dateMs ? \Carbon\Carbon::createFromTimestampMs($dateMs)->format('Y-m-d') : '—';
                            @endphp
                            <tr class="hover:bg-slate-50/60">
                                <td class="px-5 py-3 font-semibold {{ $isIncome ? 'text-emerald-600' : 'text-red-500' }}">
                                    {{ $isIncome ? '+' : '-' }}R{{ number_format($tx['amount'] ?? 0, 2) }}
                                </td>
                                <td class="px-5 py-3 text-slate-700">{{ $catLabel }}</td>
                                <td class="px-5 py-3">
                                    <span class="px-2 py-0.5 rounded-full text-xs font-medium
                                                 {{ $isIncome ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-red-50 text-red-700 border border-red-200' }}">
                                        {{ $tx['type'] ?? '—' }}
                                    </span>
                                </td>
                                <td class="px-5 py-3 text-slate-500 text-xs">{{ $date }}</td>
                                <td class="px-5 py-3 text-slate-500 text-xs max-w-[160px] truncate">{{ $tx['notes'] ?: '—' }}</td>
                            </tr>
                        @empty
                            <tr><td colspan="5" class="px-5 py-8 text-center text-slate-400">No transactions.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

        {{-- Budgets --}}
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2">
                <i class="bi bi-wallet2 text-slate-400"></i>
                <h3 class="text-sm font-semibold text-slate-900">Budgets</h3>
                <span class="ml-auto text-xs text-slate-400">({{ $budgets->count() }})</span>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full text-sm">
                    <thead class="bg-slate-50">
                        <tr>
                            @foreach(['Category','Limit','Min Goal','Period'] as $h)
                                <th class="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">{{ $h }}</th>
                            @endforeach
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                        @forelse($budgets as $b)
                            @php
                                $cat = $categories->get($b['categoryId'] ?? 0);
                                $catLabel = $cat ? ($cat['icon'].' '.$cat['name']) : 'Cat #'.($b['categoryId'] ?? '?');
                            @endphp
                            <tr class="hover:bg-slate-50/60">
                                <td class="px-5 py-3 text-slate-700">{{ $catLabel }}</td>
                                <td class="px-5 py-3 font-medium text-slate-900">R{{ number_format($b['limitAmount'] ?? 0, 2) }}</td>
                                <td class="px-5 py-3 text-slate-500">
                                    {{ ($b['minAmount'] ?? 0) > 0 ? 'R'.number_format($b['minAmount'], 2) : '—' }}
                                </td>
                                <td class="px-5 py-3 text-slate-500 text-xs">
                                    {{ \Carbon\Carbon::create($b['year'] ?? date('Y'), $b['month'] ?? 1)->format('M Y') }}
                                </td>
                            </tr>
                        @empty
                            <tr><td colspan="4" class="px-5 py-8 text-center text-slate-400">No budgets.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

        {{-- Goals --}}
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2">
                <i class="bi bi-bullseye text-slate-400"></i>
                <h3 class="text-sm font-semibold text-slate-900">Savings Goals</h3>
                <span class="ml-auto text-xs text-slate-400">({{ $goals->count() }})</span>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full text-sm">
                    <thead class="bg-slate-50">
                        <tr>
                            @foreach(['Name','Progress','Saved','Target','Target Date'] as $h)
                                <th class="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">{{ $h }}</th>
                            @endforeach
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                        @forelse($goals as $g)
                            @php
                                $pct = ($g['targetAmount'] ?? 0) > 0
                                    ? min(100, round(($g['savedAmount'] ?? 0) / $g['targetAmount'] * 100)) : 0;
                                $targetDate = ($g['targetDate'] ?? 0) > 0
                                    ? \Carbon\Carbon::createFromTimestampMs($g['targetDate'])->format('Y-m-d') : '—';
                            @endphp
                            <tr class="hover:bg-slate-50/60">
                                <td class="px-5 py-3 text-slate-900 font-medium">
                                    {{ $g['name'] ?? '—' }}
                                    @if($g['isCompleted'] ?? false)
                                        <span class="ml-1.5 px-1.5 py-0.5 rounded-full text-[10px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">Done</span>
                                    @endif
                                </td>
                                <td class="px-5 py-3" style="min-width:120px">
                                    <div class="flex items-center gap-2">
                                        <div class="flex-1 h-1.5 bg-slate-200 rounded-full overflow-hidden">
                                            <div class="h-full bg-emerald-500 rounded-full" style="width:{{ $pct }}%"></div>
                                        </div>
                                        <span class="text-xs text-slate-500 w-8 text-right">{{ $pct }}%</span>
                                    </div>
                                </td>
                                <td class="px-5 py-3 text-slate-700">R{{ number_format($g['savedAmount'] ?? 0, 2) }}</td>
                                <td class="px-5 py-3 text-slate-700">R{{ number_format($g['targetAmount'] ?? 0, 2) }}</td>
                                <td class="px-5 py-3 text-slate-500 text-xs">{{ $targetDate }}</td>
                            </tr>
                        @empty
                            <tr><td colspan="5" class="px-5 py-8 text-center text-slate-400">No goals.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

        {{-- Debts --}}
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2">
                <i class="bi bi-credit-card text-slate-400"></i>
                <h3 class="text-sm font-semibold text-slate-900">Debts</h3>
                <span class="ml-auto text-xs text-slate-400">({{ $debts->count() }})</span>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full text-sm">
                    <thead class="bg-slate-50">
                        <tr>
                            @foreach(['Name','Progress','Balance','Original','Rate','Min Payment'] as $h)
                                <th class="px-5 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">{{ $h }}</th>
                            @endforeach
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                        @forelse($debts as $d)
                            @php
                                $orig = $d['originalBalance'] ?? 0;
                                $bal  = $d['balance'] ?? 0;
                                $pct  = $orig > 0 ? min(100, round(($orig - $bal) / $orig * 100)) : 0;
                            @endphp
                            <tr class="hover:bg-slate-50/60">
                                <td class="px-5 py-3 text-slate-900 font-medium">
                                    {{ $d['name'] ?? '—' }}
                                    @if($d['isPaidOff'] ?? false)
                                        <span class="ml-1.5 px-1.5 py-0.5 rounded-full text-[10px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">Paid Off</span>
                                    @endif
                                </td>
                                <td class="px-5 py-3" style="min-width:120px">
                                    <div class="flex items-center gap-2">
                                        <div class="flex-1 h-1.5 bg-slate-200 rounded-full overflow-hidden">
                                            <div class="h-full bg-blue-500 rounded-full" style="width:{{ $pct }}%"></div>
                                        </div>
                                        <span class="text-xs text-slate-500 w-8 text-right">{{ $pct }}%</span>
                                    </div>
                                </td>
                                <td class="px-5 py-3 font-semibold text-red-500">R{{ number_format($bal, 2) }}</td>
                                <td class="px-5 py-3 text-slate-500">R{{ number_format($orig, 2) }}</td>
                                <td class="px-5 py-3 text-slate-700">{{ number_format($d['interestRate'] ?? 0, 2) }}%</td>
                                <td class="px-5 py-3 text-slate-700">R{{ number_format($d['minimumPayment'] ?? 0, 2) }}</td>
                            </tr>
                        @empty
                            <tr><td colspan="6" class="px-5 py-8 text-center text-slate-400">No debts.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</div>

@endsection
