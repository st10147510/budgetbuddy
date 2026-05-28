@extends('admin.layout')

@section('title', ($user->displayName ?? $user->email) . ' — BudgetBuddy Admin')
@section('page-title', 'User Detail')

@section('content')

<div class="row g-4">

    {{-- ── Left column: profile + actions ──────────────────────────────────── --}}
    <div class="col-lg-4">

        {{-- Profile card --}}
        <div class="card border-0 rounded-3 shadow-sm p-4 mb-4">
            <div class="d-flex align-items-center gap-3 mb-3">
                @if(!empty($profile['photoUrl']))
                    <img src="{{ $profile['photoUrl'] }}" alt="avatar"
                         style="width:52px;height:52px;border-radius:50%;object-fit:cover;">
                @else
                    <div style="width:52px;height:52px;border-radius:50%;background:#dbeafe;
                                display:flex;align-items:center;justify-content:center;
                                font-size:1.4rem;font-weight:700;color:#1e40af;">
                        {{ strtoupper(substr($user->email ?? 'U', 0, 1)) }}
                    </div>
                @endif
                <div>
                    <div class="fw-semibold">{{ $user->displayName ?? 'No Name' }}</div>
                    <div class="text-muted small">{{ $user->email ?? 'No Email' }}</div>
                </div>
            </div>

            <dl class="row small mb-0">
                <dt class="col-5 text-muted">UID</dt>
                <dd class="col-7 text-break font-monospace" style="font-size:.7rem">{{ $user->uid }}</dd>

                <dt class="col-5 text-muted">Status</dt>
                <dd class="col-7">
                    @if($user->disabled)
                        <span class="badge badge-disabled">Disabled</span>
                    @else
                        <span class="badge badge-active">Active</span>
                    @endif
                </dd>

                <dt class="col-5 text-muted">Signed Up</dt>
                <dd class="col-7">{{ $user->metadata->createdAt?->format('Y-m-d H:i') ?? '—' }}</dd>

                <dt class="col-5 text-muted">Last Sign-in</dt>
                <dd class="col-7">{{ $user->metadata->lastLoginAt?->format('Y-m-d H:i') ?? 'Never' }}</dd>
            </dl>

            <hr>

            <div class="d-flex flex-column gap-2">
                @if($user->disabled)
                    <form method="POST" action="{{ route('admin.users.enable', $user->uid) }}">
                        @csrf
                        <button class="btn btn-success btn-sm w-100">
                            <i class="bi bi-person-check me-1"></i>Enable Account
                        </button>
                    </form>
                @else
                    <form method="POST" action="{{ route('admin.users.disable', $user->uid) }}">
                        @csrf
                        <button class="btn btn-warning btn-sm w-100">
                            <i class="bi bi-person-dash me-1"></i>Disable Account
                        </button>
                    </form>
                @endif

                @if($user->email)
                    <form method="POST" action="{{ route('admin.users.reset-password', $user->uid) }}">
                        @csrf
                        <button class="btn btn-outline-primary btn-sm w-100">
                            <i class="bi bi-envelope me-1"></i>Send Password Reset
                        </button>
                    </form>
                @endif

                <form method="POST" action="{{ route('admin.users.destroy', $user->uid) }}"
                      onsubmit="return confirm('Permanently delete this user and all their data? This cannot be undone.')">
                    @csrf @method('DELETE')
                    <button class="btn btn-outline-danger btn-sm w-100">
                        <i class="bi bi-trash me-1"></i>Delete User
                    </button>
                </form>

                <a href="{{ route('admin.users.index') }}" class="btn btn-light btn-sm w-100">
                    ← Back to Users
                </a>
            </div>
        </div>

        {{-- Badges --}}
        @if($badges->count())
        <div class="card border-0 rounded-3 shadow-sm p-3">
            <div class="fw-semibold mb-3"><i class="bi bi-patch-check me-1 text-warning"></i>Badges Earned</div>
            <div class="d-flex flex-wrap gap-2">
                @foreach($badges as $badge)
                    @php $label = \App\Http\Controllers\Admin\UserController::badgeLabel($badge['badgeType'] ?? ''); @endphp
                    <span class="badge bg-light text-dark border px-2 py-1">
                        {{ $label[0] }} {{ $label[1] }}
                    </span>
                @endforeach
            </div>
        </div>
        @endif

    </div>

    {{-- ── Right column: financial data ─────────────────────────────────────── --}}
    <div class="col-lg-8">

        {{-- Financial summary --}}
        <div class="row g-3 mb-4">
            <div class="col-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 text-center">
                    <div class="text-muted small mb-1"><i class="bi bi-receipt me-1"></i>Transactions</div>
                    <div class="fs-4 fw-bold">{{ $transactions->count() }}</div>
                </div>
            </div>
            <div class="col-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 text-center">
                    <div class="text-muted small mb-1"><i class="bi bi-wallet2 me-1"></i>Budgets</div>
                    <div class="fs-4 fw-bold">{{ $budgets->count() }}</div>
                </div>
            </div>
            <div class="col-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 text-center">
                    <div class="text-muted small mb-1"><i class="bi bi-bullseye me-1"></i>Goals</div>
                    <div class="fs-4 fw-bold">{{ $goals->count() }}</div>
                </div>
            </div>
            <div class="col-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 text-center">
                    <div class="text-muted small mb-1"><i class="bi bi-credit-card me-1"></i>Debts</div>
                    <div class="fs-4 fw-bold">{{ $debts->count() }}</div>
                </div>
            </div>
        </div>

        {{-- Income / Expense / Net balance --}}
        <div class="row g-3 mb-4">
            <div class="col-4">
                <div class="card border-0 shadow-sm p-3 text-center">
                    <div class="text-muted small mb-1">Total Income</div>
                    <div class="fs-5 fw-bold text-success">R{{ number_format($totalIncome, 2) }}</div>
                </div>
            </div>
            <div class="col-4">
                <div class="card border-0 shadow-sm p-3 text-center">
                    <div class="text-muted small mb-1">Total Expenses</div>
                    <div class="fs-5 fw-bold text-danger">R{{ number_format($totalExpense, 2) }}</div>
                </div>
            </div>
            <div class="col-4">
                <div class="card border-0 shadow-sm p-3 text-center">
                    <div class="text-muted small mb-1">Net Balance</div>
                    <div class="fs-5 fw-bold {{ $netBalance >= 0 ? 'text-success' : 'text-danger' }}">
                        R{{ number_format($netBalance, 2) }}
                    </div>
                </div>
            </div>
        </div>

        {{-- Transactions --}}
        <div class="card border-0 rounded-3 shadow-sm mb-4">
            <div class="card-header bg-white border-0 pt-3 pb-0 fw-semibold">
                <i class="bi bi-receipt me-1"></i>Transactions
                <span class="text-muted fw-normal">({{ $transactions->count() }})</span>
            </div>
            <div class="card-body p-0">
                <table class="table table-sm table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-3">Amount</th>
                            <th>Category</th>
                            <th>Type</th>
                            <th>Date</th>
                            <th>Notes</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($transactions as $tx)
                            @php
                                $cat = $categories->get($tx['categoryId'] ?? 0);
                                $catLabel = $cat ? ($cat['icon'] . ' ' . $cat['name']) : 'Cat #' . ($tx['categoryId'] ?? '?');
                                $isIncome = ($tx['type'] ?? '') === 'INCOME';
                                $dateMs = $tx['date'] ?? 0;
                                $date = $dateMs ? \Carbon\Carbon::createFromTimestampMs($dateMs)->format('Y-m-d') : '—';
                            @endphp
                            <tr>
                                <td class="ps-3 fw-semibold {{ $isIncome ? 'text-success' : 'text-danger' }}">
                                    {{ $isIncome ? '+' : '-' }}R{{ number_format($tx['amount'] ?? 0, 2) }}
                                </td>
                                <td>{{ $catLabel }}</td>
                                <td>
                                    <span class="badge {{ $isIncome ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger' }}">
                                        {{ $tx['type'] ?? '—' }}
                                    </span>
                                </td>
                                <td class="text-muted small">{{ $date }}</td>
                                <td class="text-muted small">{{ $tx['notes'] ?: '—' }}</td>
                            </tr>
                        @empty
                            <tr><td colspan="5" class="text-center text-muted py-3">No transactions.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

        {{-- Budgets --}}
        <div class="card border-0 rounded-3 shadow-sm mb-4">
            <div class="card-header bg-white border-0 pt-3 pb-0 fw-semibold">
                <i class="bi bi-wallet2 me-1"></i>Budgets
                <span class="text-muted fw-normal">({{ $budgets->count() }})</span>
            </div>
            <div class="card-body p-0">
                <table class="table table-sm table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-3">Category</th>
                            <th>Limit</th>
                            <th>Min Goal</th>
                            <th>Period</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($budgets as $b)
                            @php
                                $cat = $categories->get($b['categoryId'] ?? 0);
                                $catLabel = $cat ? ($cat['icon'] . ' ' . $cat['name']) : 'Cat #' . ($b['categoryId'] ?? '?');
                            @endphp
                            <tr>
                                <td class="ps-3">{{ $catLabel }}</td>
                                <td>R{{ number_format($b['limitAmount'] ?? 0, 2) }}</td>
                                <td>
                                    @if(($b['minAmount'] ?? 0) > 0)
                                        R{{ number_format($b['minAmount'], 2) }}
                                    @else
                                        <span class="text-muted">—</span>
                                    @endif
                                </td>
                                <td class="text-muted small">
                                    {{ \Carbon\Carbon::create($b['year'] ?? date('Y'), $b['month'] ?? 1)->format('M Y') }}
                                </td>
                            </tr>
                        @empty
                            <tr><td colspan="4" class="text-center text-muted py-3">No budgets.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

        {{-- Goals --}}
        <div class="card border-0 rounded-3 shadow-sm mb-4">
            <div class="card-header bg-white border-0 pt-3 pb-0 fw-semibold">
                <i class="bi bi-bullseye me-1"></i>Savings Goals
                <span class="text-muted fw-normal">({{ $goals->count() }})</span>
            </div>
            <div class="card-body p-0">
                <table class="table table-sm table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-3">Name</th>
                            <th>Progress</th>
                            <th>Saved</th>
                            <th>Target</th>
                            <th>Target Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($goals as $g)
                            @php
                                $pct = ($g['targetAmount'] ?? 0) > 0
                                    ? min(100, round(($g['savedAmount'] ?? 0) / $g['targetAmount'] * 100))
                                    : 0;
                                $targetDate = ($g['targetDate'] ?? 0) > 0
                                    ? \Carbon\Carbon::createFromTimestampMs($g['targetDate'])->format('Y-m-d')
                                    : '—';
                            @endphp
                            <tr>
                                <td class="ps-3">
                                    {{ $g['name'] ?? '—' }}
                                    @if($g['isCompleted'] ?? false)
                                        <span class="badge badge-active ms-1">Done</span>
                                    @endif
                                </td>
                                <td style="min-width:90px">
                                    <div class="d-flex align-items-center gap-1">
                                        <div class="progress flex-grow-1" style="height:6px">
                                            <div class="progress-bar bg-success" style="width:{{ $pct }}%"></div>
                                        </div>
                                        <span class="small text-muted">{{ $pct }}%</span>
                                    </div>
                                </td>
                                <td>R{{ number_format($g['savedAmount'] ?? 0, 2) }}</td>
                                <td>R{{ number_format($g['targetAmount'] ?? 0, 2) }}</td>
                                <td class="text-muted small">{{ $targetDate }}</td>
                            </tr>
                        @empty
                            <tr><td colspan="5" class="text-center text-muted py-3">No goals.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

        {{-- Debts --}}
        <div class="card border-0 rounded-3 shadow-sm">
            <div class="card-header bg-white border-0 pt-3 pb-0 fw-semibold">
                <i class="bi bi-credit-card me-1"></i>Debts
                <span class="text-muted fw-normal">({{ $debts->count() }})</span>
            </div>
            <div class="card-body p-0">
                <table class="table table-sm table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-3">Name</th>
                            <th>Progress</th>
                            <th>Balance</th>
                            <th>Original</th>
                            <th>Rate</th>
                            <th>Min Payment</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($debts as $d)
                            @php
                                $orig = $d['originalBalance'] ?? 0;
                                $bal  = $d['balance'] ?? 0;
                                $pct  = $orig > 0 ? min(100, round(($orig - $bal) / $orig * 100)) : 0;
                            @endphp
                            <tr>
                                <td class="ps-3">
                                    {{ $d['name'] ?? '—' }}
                                    @if($d['isPaidOff'] ?? false)
                                        <span class="badge badge-active ms-1">Paid Off</span>
                                    @endif
                                </td>
                                <td style="min-width:90px">
                                    <div class="d-flex align-items-center gap-1">
                                        <div class="progress flex-grow-1" style="height:6px">
                                            <div class="progress-bar bg-primary" style="width:{{ $pct }}%"></div>
                                        </div>
                                        <span class="small text-muted">{{ $pct }}%</span>
                                    </div>
                                </td>
                                <td class="text-danger fw-semibold">R{{ number_format($bal, 2) }}</td>
                                <td class="text-muted">R{{ number_format($orig, 2) }}</td>
                                <td>{{ number_format($d['interestRate'] ?? 0, 2) }}%</td>
                                <td>R{{ number_format($d['minimumPayment'] ?? 0, 2) }}</td>
                            </tr>
                        @empty
                            <tr><td colspan="6" class="text-center text-muted py-3">No debts.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</div>
@endsection
