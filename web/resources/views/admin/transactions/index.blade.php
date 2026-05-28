@extends('admin.layout')

@section('title', 'Transactions — BudgetBuddy Admin')
@section('page-title', 'Transactions')

@section('content')

{{-- Filters --}}
<form method="GET" action="{{ route('admin.transactions') }}"
      class="bg-white rounded-2xl border border-slate-200 p-5 mb-6">
    <div class="grid grid-cols-2 xl:grid-cols-4 gap-4">

        <div>
            <label class="block text-xs font-medium text-slate-500 mb-1.5">User</label>
            <select name="uid"
                    class="w-full rounded-lg border border-slate-200 text-sm px-3 py-2 text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">All users</option>
                @foreach($userMap as $uid => $email)
                <option value="{{ $uid }}" {{ request('uid') === $uid ? 'selected' : '' }}>{{ $email }}</option>
                @endforeach
            </select>
        </div>

        <div>
            <label class="block text-xs font-medium text-slate-500 mb-1.5">Type</label>
            <select name="type"
                    class="w-full rounded-lg border border-slate-200 text-sm px-3 py-2 text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">Income &amp; Expense</option>
                <option value="income"  {{ request('type') === 'income'  ? 'selected' : '' }}>Income only</option>
                <option value="expense" {{ request('type') === 'expense' ? 'selected' : '' }}>Expense only</option>
            </select>
        </div>

        <div>
            <label class="block text-xs font-medium text-slate-500 mb-1.5">Category</label>
            <select name="category"
                    class="w-full rounded-lg border border-slate-200 text-sm px-3 py-2 text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">All categories</option>
                @foreach($categories as $id => $cat)
                <option value="{{ $id }}" {{ (int)request('category') === $id ? 'selected' : '' }}>
                    {{ $cat['icon'] }} {{ $cat['name'] }}
                </option>
                @endforeach
            </select>
        </div>

        <div>
            <label class="block text-xs font-medium text-slate-500 mb-1.5">Notes search</label>
            <input type="text" name="search" value="{{ request('search') }}"
                   placeholder="Search description…"
                   class="w-full rounded-lg border border-slate-200 text-sm px-3 py-2 text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500">
        </div>
    </div>

    <div class="flex items-center gap-3 mt-4">
        <button type="submit"
                class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors">
            Apply filters
        </button>
        @if(request()->hasAny(['uid','type','category','search']))
        <a href="{{ route('admin.transactions') }}"
           class="px-4 py-2 text-sm text-slate-500 hover:text-slate-700 transition-colors">
            Clear
        </a>
        @endif
        <span class="ml-auto text-xs text-slate-400">{{ $paginator->total() }} transaction{{ $paginator->total() !== 1 ? 's' : '' }}</span>
    </div>
</form>

{{-- Table --}}
<div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
    <div class="overflow-x-auto">
        <table class="w-full text-sm">
            <thead>
                <tr class="border-b border-slate-100 text-left">
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Date</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">User</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Category</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Description</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider text-right">Amount</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Type</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($paginator as $tx)
                @php
                    $isIncome = ($tx['type'] ?? '') === 'INCOME';
                    $catId    = $tx['categoryId'] ?? 10;
                    $cat      = $categories[$catId] ?? ['icon' => '📦', 'name' => 'Other'];
                    $date     = isset($tx['date'])
                        ? \Carbon\Carbon::createFromTimestampMs($tx['date'])->format('d M Y')
                        : '—';
                    $uid      = $tx['_uid'] ?? '';
                    $email    = $userMap[$uid] ?? $uid;
                @endphp
                <tr class="hover:bg-slate-50/60 transition-colors">
                    <td class="px-5 py-3.5 text-slate-500 whitespace-nowrap tabular-nums text-xs">{{ $date }}</td>

                    <td class="px-5 py-3.5">
                        <a href="{{ route('admin.users.show', $uid) }}"
                           class="text-blue-600 hover:underline text-xs truncate block max-w-[150px]">
                            {{ $email }}
                        </a>
                    </td>

                    <td class="px-5 py-3.5">
                        <span class="inline-flex items-center gap-1.5 text-xs text-slate-600 bg-slate-50 px-2.5 py-1 rounded-full border border-slate-100">
                            {{ $cat['icon'] }} {{ $cat['name'] }}
                        </span>
                    </td>

                    <td class="px-5 py-3.5 text-slate-700 max-w-xs truncate" title="{{ $tx['notes'] ?? '' }}">
                        {{ $tx['notes'] ?? '—' }}
                    </td>

                    <td class="px-5 py-3.5 tabular-nums font-semibold text-right
                               {{ $isIncome ? 'text-emerald-600' : 'text-red-500' }}">
                        {{ $isIncome ? '+' : '-' }}R{{ number_format($tx['amount'] ?? 0, 2) }}
                    </td>

                    <td class="px-5 py-3.5">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                            {{ $isIncome ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700' }}">
                            {{ ucfirst(strtolower($tx['type'] ?? 'expense')) }}
                        </span>
                    </td>
                </tr>
                @empty
                <tr>
                    <td colspan="6" class="px-5 py-16 text-center text-slate-400">
                        <i class="bi bi-receipt text-3xl block mb-3"></i>
                        <p class="text-sm">No transactions match your filters.</p>
                    </td>
                </tr>
                @endforelse
            </tbody>
        </table>
    </div>

    @if($paginator->hasPages())
    <div class="px-5 py-4 border-t border-slate-100">
        {{ $paginator->links() }}
    </div>
    @endif
</div>

@endsection
