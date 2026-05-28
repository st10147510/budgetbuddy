@extends('admin.layout')

@section('title', 'Dashboard — BudgetBuddy Admin')
@section('page-title', 'Dashboard')

@section('content')

@if(!$firebaseConfigured)
<div class="alert alert-warning d-flex align-items-start gap-3 mb-4">
    <i class="bi bi-exclamation-triangle-fill fs-5 mt-1"></i>
    <div>
        <strong>Firebase not configured.</strong>
        Place your service account JSON at <code>web/firebase-service-account.json</code>
        and set <code>FIREBASE_CREDENTIALS=firebase-service-account.json</code> in <code>web/.env</code>.
        Get it from the <a href="https://console.firebase.google.com" target="_blank">Firebase Console</a>
        → Project Settings → Service Accounts → Generate new private key.
    </div>
</div>
@endif

<div class="row g-4 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="card stat-card p-3">
            <div class="text-muted small mb-1">Total Users</div>
            <div class="fs-3 fw-bold">{{ number_format($stats['total_users']) }}</div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card stat-card p-3">
            <div class="text-muted small mb-1">Active Users</div>
            <div class="fs-3 fw-bold text-success">{{ number_format($stats['active_users']) }}</div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card stat-card p-3">
            <div class="text-muted small mb-1">Disabled Users</div>
            <div class="fs-3 fw-bold text-danger">{{ number_format($stats['disabled_users']) }}</div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card stat-card p-3">
            <div class="text-muted small mb-1">New This Week</div>
            <div class="fs-3 fw-bold text-primary">{{ number_format($stats['new_this_week']) }}</div>
        </div>
    </div>
</div>

<div class="card border-0 rounded-3 shadow-sm p-3">
    <div class="d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Quick Actions</span>
    </div>
    <hr>
    <a href="{{ route('admin.users.index') }}" class="btn btn-outline-primary">
        <i class="bi bi-people me-1"></i> Manage Users
    </a>
</div>
@endsection
