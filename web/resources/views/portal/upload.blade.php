@extends('portal.layout')

@section('title', 'Upload Statement — BudgetBuddy')

@section('content')

<div class="max-w-2xl mx-auto">

    {{-- Header --}}
    <div class="mb-8">
        <h1 class="text-2xl font-bold text-white mb-1">Upload Bank Statement</h1>
        <p class="text-sm" style="color:#888">
            Upload a PDF bank statement and we'll extract your transactions in the background.
            Supported: FNB, Nedbank, Standard Bank, Absa, and most South African banks.
        </p>
    </div>

    {{-- Upload card --}}
    <div class="portal-card overflow-hidden mb-6">
        <div class="px-6 py-4" style="border-bottom:1px solid rgba(255,255,255,0.06)">
            <h2 class="text-sm font-semibold text-white">Statement file</h2>
        </div>
        <div class="p-6">
            <form method="POST" action="{{ route('portal.upload.post') }}" enctype="multipart/form-data" id="upload-form">
                @csrf

                {{-- Drop zone --}}
                <div id="drop-zone"
                     class="relative flex flex-col items-center justify-center gap-3 py-14 px-8 rounded-xl
                            cursor-pointer text-center transition-all duration-200"
                     style="border:2px dashed rgba(255,255,255,0.12); background:rgba(255,255,255,0.02)">
                    <input type="file" name="statement" id="file-input" accept=".pdf"
                           class="absolute inset-0 opacity-0 cursor-pointer w-full h-full" required>

                    <div id="drop-idle">
                        <div class="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-3"
                             style="background:rgba(255,255,255,0.05)">
                            <i class="bi bi-file-earmark-pdf text-2xl" style="color:#6EDCD3"></i>
                        </div>
                        <p class="text-sm font-medium text-white">
                            Drop your PDF here, or <span style="color:#6EDCD3">browse</span>
                        </p>
                        <p class="text-xs mt-1" style="color:#555">PDF files only — max 10 MB</p>
                    </div>

                    <div id="drop-selected" class="hidden">
                        <div class="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-3"
                             style="background:rgba(110,220,211,0.12)">
                            <i class="bi bi-file-earmark-check text-2xl" style="color:#6EDCD3"></i>
                        </div>
                        <p class="text-sm font-medium text-white" id="file-name">—</p>
                        <p class="text-xs mt-1" style="color:#888" id="file-size">—</p>
                    </div>
                </div>

                @error('statement')
                    <p class="mt-2 text-xs" style="color:#ef9a9a">{{ $message }}</p>
                @enderror

                {{-- Category hint --}}
                <div class="mt-5">
                    <label class="block text-sm font-medium text-white mb-1.5">
                        Category hint <span style="color:#555" class="font-normal">(optional)</span>
                    </label>
                    <select name="default_category"
                            class="w-full rounded-xl px-4 py-2.5 text-sm
                                   focus:outline-none focus:ring-2 transition"
                            style="background:#252525; border:1px solid rgba(255,255,255,0.1);
                                   color:#ccc; focus:ring-color:#6EDCD3">
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
                    <p class="mt-1 text-xs" style="color:#555">
                        Assign a fallback category if we can't determine one automatically.
                    </p>
                </div>

                {{-- Submit --}}
                <div class="mt-6 flex items-center gap-3">
                    <button type="submit" id="submit-btn"
                            class="flex-1 flex items-center justify-center gap-2 py-3 text-sm font-semibold
                                   rounded-xl transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2"
                            style="background:#6EDCD3; color:#0D0D0D; focus:ring-color:#6EDCD3">
                        <i class="bi bi-cloud-upload" id="submit-icon"></i>
                        <span id="submit-label">Upload &amp; Process</span>
                    </button>
                    <a href="{{ route('portal.dashboard') }}"
                       class="px-5 py-3 text-sm font-medium rounded-xl transition-colors"
                       style="color:#888; border:1px solid rgba(255,255,255,0.1)"
                       onmouseover="this.style.color='#fff';this.style.borderColor='rgba(255,255,255,0.2)'"
                       onmouseout="this.style.color='#888';this.style.borderColor='rgba(255,255,255,0.1)'">
                        Cancel
                    </a>
                </div>
            </form>
        </div>
    </div>

    {{-- How it works --}}
    <div class="portal-card p-6">
        <h3 class="text-sm font-semibold text-white mb-4">How it works</h3>
        <div class="space-y-4">
            @foreach([
                ['icon' => 'bi-upload',      'title' => 'Upload',     'desc' => 'Upload your PDF bank statement — we support most South African bank formats.'],
                ['icon' => 'bi-robot',       'title' => 'Extract',    'desc' => 'Our parser reads the PDF text and identifies transaction rows, dates, and amounts.'],
                ['icon' => 'bi-tags',        'title' => 'Categorise', 'desc' => 'Transactions are matched to your existing categories using keyword rules.'],
                ['icon' => 'bi-cloud-check', 'title' => 'Sync',       'desc' => 'Extracted transactions are saved to your BudgetBuddy account and sync to your app.'],
            ] as $step)
                <div class="flex items-start gap-4">
                    <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                         style="background:rgba(110,220,211,0.1)">
                        <i class="bi {{ $step['icon'] }} text-sm" style="color:#6EDCD3"></i>
                    </div>
                    <div>
                        <div class="text-sm font-medium text-white">{{ $step['title'] }}</div>
                        <div class="text-xs mt-0.5 leading-relaxed" style="color:#666">{{ $step['desc'] }}</div>
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
    zone.style.borderColor = '#6EDCD3';
    zone.style.background  = 'rgba(110,220,211,0.05)';
}

input.addEventListener('change', () => showFile(input.files[0]));

zone.addEventListener('dragover', e => {
    e.preventDefault();
    zone.style.borderColor = '#6EDCD3';
    zone.style.background  = 'rgba(110,220,211,0.05)';
});
zone.addEventListener('dragleave', () => {
    if (!input.files[0]) {
        zone.style.borderColor = 'rgba(255,255,255,0.12)';
        zone.style.background  = 'rgba(255,255,255,0.02)';
    }
});
zone.addEventListener('drop', e => {
    e.preventDefault();
    if (e.dataTransfer.files[0]) {
        const dt = new DataTransfer();
        dt.items.add(e.dataTransfer.files[0]);
        input.files = dt.files;
        showFile(input.files[0]);
    }
});

form.addEventListener('submit', () => {
    btn.disabled = true;
    btn.style.opacity = '0.7';
    btnIcon.className = 'bi bi-hourglass-split';
    btnLbl.textContent = 'Uploading…';
});
</script>
@endpush

@endsection
