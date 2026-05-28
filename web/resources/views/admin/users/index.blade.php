@extends('admin.layout')

@section('title', 'Users — BudgetBuddy Admin')
@section('page-title', 'Users')

@section('content')

@if(!$firebaseConfigured)
<div class="alert alert-warning d-flex align-items-start gap-3 mb-4">
    <i class="bi bi-exclamation-triangle-fill fs-5 mt-1"></i>
    <div>
        <strong>Firebase not configured.</strong>
        Place your service account JSON at <code>web/firebase-service-account.json</code>
        and set <code>FIREBASE_CREDENTIALS=firebase-service-account.json</code> in <code>web/.env</code>.
    </div>
</div>
@endif

<div class="card border-0 rounded-3 shadow-sm">
    <div class="card-header bg-white border-0 pt-3 pb-0 px-3 d-flex justify-content-between align-items-center">
        <span class="fw-semibold">All Users <span class="text-muted fw-normal">({{ $total }})</span></span>
        <form method="GET" action="{{ route('admin.users.index') }}" class="d-flex gap-2">
            <input type="text" name="search" class="form-control form-control-sm" placeholder="Search email or name…"
                value="{{ $search }}" style="width:240px;">
            <button class="btn btn-sm btn-primary">Search</button>
            @if($search)
                <a href="{{ route('admin.users.index') }}" class="btn btn-sm btn-outline-secondary">Clear</a>
            @endif
        </form>
    </div>
    <div class="card-body p-0">
        <table class="table table-hover mb-0">
            <thead class="table-light">
                <tr>
                    <th class="ps-3">Email</th>
                    <th>Display Name</th>
                    <th>Status</th>
                    <th>Signed Up</th>
                    <th>Last Sign-in</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                @forelse($users as $user)
                    <tr>
                        <td class="ps-3">{{ $user['email'] ?? '—' }}</td>
                        <td>{{ $user['display_name'] ?? '—' }}</td>
                        <td>
                            @if($user['disabled'])
                                <span class="badge badge-disabled">Disabled</span>
                            @else
                                <span class="badge badge-active">Active</span>
                            @endif
                        </td>
                        <td class="text-muted small">{{ $user['created_at'] ?? '—' }}</td>
                        <td class="text-muted small">{{ $user['last_sign_in'] ?? 'Never' }}</td>
                        <td class="text-end pe-3">
                            <a href="{{ route('admin.users.show', $user['uid']) }}" class="btn btn-sm btn-outline-secondary">
                                View
                            </a>
                        </td>
                    </tr>
                @empty
                    <tr><td colspan="6" class="text-center text-muted py-4">No users found.</td></tr>
                @endforelse
            </tbody>
        </table>
    </div>

    @if($lastPage > 1)
        <div class="card-footer bg-white border-0 d-flex justify-content-between align-items-center py-2 px-3">
            <span class="text-muted small">Page {{ $page }} of {{ $lastPage }}</span>
            <div class="d-flex gap-2">
                @if($page > 1)
                    <a href="{{ route('admin.users.index', ['page' => $page - 1, 'search' => $search]) }}"
                        class="btn btn-sm btn-outline-secondary">← Prev</a>
                @endif
                @if($page < $lastPage)
                    <a href="{{ route('admin.users.index', ['page' => $page + 1, 'search' => $search]) }}"
                        class="btn btn-sm btn-outline-secondary">Next →</a>
                @endif
            </div>
        </div>
    @endif
</div>
@endsection
