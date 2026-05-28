@extends('portal.layout')

@section('title', 'Upload Statement — BudgetBuddy')

@section('content')

<div class="max-w-2xl mx-auto">

    {{-- Header --}}
    <div class="mb-8">
        <h1 class="text-2xl font-bold text-slate-900 mb-1">Upload Bank Statement</h1>
        <p class="text-slate-500 text-sm">
            Upload a PDF bank statement and we'll extract your transactions in the background.
            Supported: FNB, Nedbank, Standard Bank, Absa, and most South African banks.
        </p>
    </div>

    {{-- Upload card --}}
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden mb-6">
        <div class="px-6 py-5 border-b border-slate-100">
            <h2 class="text-sm font-semibold text-slate-900">Statement file</h2>
        </div>
        <div class="p-6">
            <form method="POST" action="{{ route('portal.upload.post') }}" enctype="multipart/form-data" id="upload-form">
                @csrf

                {{-- Drop zone --}}
                <div id="drop-zone"
                     class="relative flex flex-col items-center justify-center gap-3 py-14 px-8 rounded-xl
                            border-2 border-dashed border-slate-300 cursor-pointer text-center
                            hover:border-blue-400 hover:bg-blue-50/40 transition-all duration-200">
                    <input type="file" name="statement" id="file-input" accept=".pdf"
                           class="absolute inset-0 opacity-0 cursor-pointer w-full h-full" required>
                    <div id="drop-idle">
                        <div class="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center mx-auto mb-3">
                            <i class="bi bi-file-earmark-pdf text-slate-400 text-2xl"></i>
                        </div>
                        <p class="font-medium text-slate-700 text-sm">Drop your PDF here, or <span class="text-blue-600">browse</span></p>
                        <p class="text-xs text-slate-400 mt-1">PDF files only — max 20 MB</p>
                    </div>
                    <div id="drop-selected" class="hidden">
                        <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center mx-auto mb-3">
                            <i class="bi bi-file-earmark-check text-blue-500 text-2xl"></i>
                        </div>
                        <p class="font-medium text-slate-900 text-sm" id="file-name">—</p>
                        <p class="text-xs text-slate-400 mt-1" id="file-size">—</p>
                    </div>
                </div>

                @error('statement')
                    <p class="mt-2 text-xs text-red-600">{{ $message }}</p>
                @enderror

                {{-- Options --}}
                <div class="mt-5 space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-1.5">Category hint <span class="text-slate-400 font-normal">(optional)</span></label>
                        <select name="default_category"
                            class="w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm text-slate-700
                                   focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                            <option value="">Auto-detect categories</option>
                            <option value="1">🛒 Food &amp; Groceries</option>
                            <option value="2">🚗 Transport</option>
                            <option value="3">🎬 Entertainment</option>
                            <option value="4">💊 Healthcare</option>
                            <option value="5">💡 Utilities</option>
                            <option value="6">🏠 Housing</option>
                            <option value="7">📚 Education</option>
                            <option value="8">👗 Clothing</option>
                            <option value="9">💰 Savings</option>
                            <option value="10">📦 Other</option>
                        </select>
                        <p class="mt-1 text-xs text-slate-400">Assign a fallback category if we can't determine one automatically.</p>
                    </div>
                </div>

                {{-- Submit --}}
                <div class="mt-6 flex items-center gap-3">
                    <button type="submit" id="submit-btn"
                        class="flex-1 flex items-center justify-center gap-2 py-3 bg-blue-600 hover:bg-blue-700
                               text-white text-sm font-semibold rounded-xl transition-colors
                               focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2">
                        <i class="bi bi-cloud-upload" id="submit-icon"></i>
                        <span id="submit-label">Upload & Process</span>
                    </button>
                    <a href="{{ route('portal.dashboard') }}"
                       class="px-5 py-3 text-sm font-medium text-slate-600 border border-slate-300 rounded-xl
                              hover:border-slate-400 hover:bg-slate-50 transition-colors">
                        Cancel
                    </a>
                </div>
            </form>
        </div>
    </div>

    {{-- How it works --}}
    <div class="bg-white rounded-2xl border border-slate-200 p-6">
        <h3 class="text-sm font-semibold text-slate-900 mb-4">How it works</h3>
        <div class="space-y-4">
            @foreach([
                ['icon' => 'bi-upload', 'title' => 'Upload', 'desc' => 'Upload your PDF bank statement — we support most South African bank formats.'],
                ['icon' => 'bi-robot',  'title' => 'Extract', 'desc' => 'Our parser reads the PDF text and identifies transaction rows, dates, and amounts.'],
                ['icon' => 'bi-tags',   'title' => 'Categorise', 'desc' => 'Transactions are matched to your existing categories using keyword rules.'],
                ['icon' => 'bi-cloud-check', 'title' => 'Sync', 'desc' => 'Extracted transactions are saved to your BudgetBuddy account and sync to your app.'],
            ] as $step)
                <div class="flex items-start gap-4">
                    <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
                        <i class="bi {{ $step['icon'] }} text-blue-600 text-sm"></i>
                    </div>
                    <div>
                        <div class="text-sm font-medium text-slate-900">{{ $step['title'] }}</div>
                        <div class="text-xs text-slate-500 mt-0.5 leading-relaxed">{{ $step['desc'] }}</div>
                    </div>
                </div>
            @endforeach
        </div>
    </div>

</div>

@push('scripts')
<script>
const input   = document.getElementById('file-input');
const zone    = document.getElementById('drop-zone');
const idle    = document.getElementById('drop-idle');
const sel     = document.getElementById('drop-selected');
const fname   = document.getElementById('file-name');
const fsize   = document.getElementById('file-size');
const form    = document.getElementById('upload-form');
const btn     = document.getElementById('submit-btn');
const btnIcon = document.getElementById('submit-icon');
const btnLbl  = document.getElementById('submit-label');

function showFile(file) {
    if (!file) return;
    fname.textContent = file.name;
    fsize.textContent = (file.size / 1024 / 1024).toFixed(2) + ' MB';
    idle.classList.add('hidden');
    sel.classList.remove('hidden');
    zone.classList.add('border-blue-400', 'bg-blue-50/40');
}

input.addEventListener('change', () => showFile(input.files[0]));

zone.addEventListener('dragover', e => { e.preventDefault(); zone.classList.add('drop-active'); });
zone.addEventListener('dragleave', () => zone.classList.remove('drop-active'));
zone.addEventListener('drop', e => {
    e.preventDefault();
    zone.classList.remove('drop-active');
    if (e.dataTransfer.files[0]) {
        const dt = new DataTransfer();
        dt.items.add(e.dataTransfer.files[0]);
        input.files = dt.files;
        showFile(input.files[0]);
    }
});

form.addEventListener('submit', () => {
    btn.disabled = true;
    btnIcon.className = 'bi bi-hourglass-split animate-spin';
    btnLbl.textContent = 'Uploading…';
});
</script>
@endpush

@endsection
