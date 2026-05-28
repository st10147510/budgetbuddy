@extends('admin.layout')

@section('title', 'Upload Monitor — BudgetBuddy Admin')
@section('page-title', 'Upload Monitor')

@section('content')

{{-- Status filter tabs --}}
@php
$tabs = [
    null       => 'All',
    'pending'  => 'Pending',
    'processing'=> 'Processing',
    'done'     => 'Done',
    'failed'   => 'Failed',
];
$statusStyles = [
    'pending'    => 'bg-slate-100 text-slate-600',
    'processing' => 'bg-blue-50 text-blue-700',
    'done'       => 'bg-emerald-50 text-emerald-700',
    'failed'     => 'bg-red-50 text-red-700',
];
@endphp

<div class="flex items-center gap-2 mb-6 flex-wrap">
    @foreach($tabs as $key => $label)
    <a href="{{ route('admin.uploads.index', $key ? ['status' => $key] : []) }}"
       class="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors
              {{ $status === $key
                 ? 'bg-blue-600 text-white'
                 : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50' }}">
        {{ $label }}
        @if($key && isset($statusCounts[$key]))
            <span class="text-[11px] font-semibold {{ $status === $key ? 'text-blue-200' : 'text-slate-400' }}">
                {{ $statusCounts[$key] }}
            </span>
        @endif
    </a>
    @endforeach

    <span class="ml-auto text-xs text-slate-400">{{ $jobs->total() }} total job{{ $jobs->total() !== 1 ? 's' : '' }}</span>
</div>

{{-- Table --}}
<div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
    <div class="overflow-x-auto">
        <table class="w-full text-sm">
            <thead>
                <tr class="border-b border-slate-100 text-left">
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">ID</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">User</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">File</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Status</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Rows</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Category</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Submitted</th>
                    <th class="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Actions</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($jobs as $job)
                <tr class="hover:bg-slate-50/60 transition-colors">
                    <td class="px-5 py-3.5 text-slate-400 tabular-nums">#{{ $job->id }}</td>

                    <td class="px-5 py-3.5">
                        <a href="{{ route('admin.users.show', $job->uid) }}"
                           class="text-blue-600 hover:underline text-xs truncate block max-w-[160px]">
                            {{ $emailMap[$job->uid] ?? $job->uid }}
                        </a>
                    </td>

                    <td class="px-5 py-3.5">
                        <div class="flex items-center gap-2">
                            <i class="bi bi-file-earmark-pdf text-red-400"></i>
                            <span class="truncate max-w-[180px] font-medium text-slate-700" title="{{ $job->filename }}">
                                {{ $job->filename }}
                            </span>
                        </div>
                        @if($job->storage_url)
                        <a href="{{ $job->storage_url }}" target="_blank"
                           class="text-[11px] text-slate-400 hover:text-blue-600 mt-0.5 block">
                            View in storage ↗
                        </a>
                        @endif
                    </td>

                    <td class="px-5 py-3.5">
                        <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium
                            {{ $statusStyles[$job->status] ?? 'bg-slate-100 text-slate-600' }}">
                            @if($job->status === 'processing')
                                <span class="w-1.5 h-1.5 rounded-full bg-blue-500 animate-pulse"></span>
                            @endif
                            {{ ucfirst($job->status) }}
                        </span>
                        @if($job->error)
                        <p class="text-[11px] text-red-500 mt-1 max-w-[180px] truncate" title="{{ $job->error }}">
                            {{ $job->error }}
                        </p>
                        @endif
                    </td>

                    <td class="px-5 py-3.5 tabular-nums text-slate-700 font-semibold">
                        {{ $job->rows_imported ?? 0 }}
                    </td>

                    <td class="px-5 py-3.5 text-slate-500 text-xs">
                        {{ $job->default_category ? 'Category '.$job->default_category : 'Auto' }}
                    </td>

                    <td class="px-5 py-3.5 text-slate-400 text-xs whitespace-nowrap">
                        {{ $job->created_at->diffForHumans() }}
                        <div class="text-[11px] text-slate-300">{{ $job->created_at->format('d M H:i') }}</div>
                    </td>

                    <td class="px-5 py-3.5">
                        @if(in_array($job->status, ['failed', 'done']))
                        <form method="POST" action="{{ route('admin.uploads.retry', $job->id) }}">
                            @csrf
                            <button type="submit"
                                    class="inline-flex items-center gap-1 text-xs px-3 py-1.5 rounded-lg
                                           bg-slate-100 hover:bg-blue-50 hover:text-blue-700 text-slate-600
                                           transition-colors font-medium">
                                <i class="bi bi-arrow-clockwise"></i>
                                Retry
                            </button>
                        </form>
                        @endif
                    </td>
                </tr>
                @empty
                <tr>
                    <td colspan="8" class="px-5 py-16 text-center text-slate-400">
                        <i class="bi bi-cloud-upload text-3xl block mb-3"></i>
                        <p class="text-sm">No uploads{{ $status ? ' with status "'.$status.'"' : '' }}.</p>
                    </td>
                </tr>
                @endforelse
            </tbody>
        </table>
    </div>

    @if($jobs->hasPages())
    <div class="px-5 py-4 border-t border-slate-100">
        {{ $jobs->links() }}
    </div>
    @endif
</div>

@endsection
