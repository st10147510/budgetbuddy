@extends('admin.layout')

@section('title', 'Policy Management — BudgetBuddy Admin')
@section('page-title', 'Policies')

@section('content')

<p class="text-sm mb-6" style="color:#888">
    Edit policy content and publish a new version. Bumping the version triggers re-acceptance from all users on next login.
    Content is saved to Firestore under <code style="color:#6EDCD3;font-size:0.75rem">app_config/policies</code>.
</p>

@if(session('success'))
<div class="flex items-start gap-3 p-4 mb-6 rounded-xl text-sm"
     style="background:rgba(52,211,153,0.1);border:1px solid rgba(52,211,153,0.2);color:#34d399">
    <i class="bi bi-check-circle-fill mt-0.5"></i>
    {{ session('success') }}
</div>
@endif

<div class="grid grid-cols-1 xl:grid-cols-2 gap-5 mb-5">

    {{-- ── Terms & Conditions ─────────────────────────────────────────────── --}}
    <div class="rounded-2xl overflow-hidden" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.07)">

        {{-- Card header --}}
        <div class="flex items-center gap-3 px-6 py-4" style="border-bottom:1px solid rgba(255,255,255,0.07)">
            <div class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
                 style="background:rgba(110,220,211,0.12)">
                <i class="bi bi-file-earmark-text text-sm" style="color:#6EDCD3"></i>
            </div>
            <div class="flex-1 min-w-0">
                <div class="font-semibold text-white text-sm">Terms &amp; Conditions</div>
                <div class="flex items-center gap-2 mt-0.5">
                    <span class="text-xs font-medium px-2 py-0.5 rounded-full"
                          style="background:rgba(110,220,211,0.12);color:#6EDCD3">
                        Firestore: <strong>terms_version</strong> = "{{ $versions['terms_version'] }}"
                    </span>
                </div>
            </div>
            <a href="{{ route('legal.terms') }}" target="_blank"
               class="text-xs flex items-center gap-1 flex-shrink-0" style="color:#6EDCD3">
                Preview <i class="bi bi-box-arrow-up-right text-[10px]"></i>
            </a>
        </div>

        <form method="POST" action="{{ route('admin.policies.update') }}" class="p-6 space-y-4">
            @csrf
            <input type="hidden" name="type" value="terms">

            {{-- Content editor --}}
            <div>
                <div class="flex items-center justify-between mb-1.5">
                    <label class="text-xs font-medium text-white">Policy Content</label>
                    <span class="text-xs" style="color:#555">HTML supported</span>
                </div>
                <textarea name="content" rows="12"
                          class="w-full rounded-xl px-4 py-3 text-xs font-mono leading-relaxed resize-y
                                 focus:outline-none focus:ring-1"
                          style="background:#111;border:1px solid rgba(255,255,255,0.1);color:#ccc;
                                 min-height:240px;focus-ring-color:#6EDCD3"
                          placeholder="Enter HTML content for the Terms & Conditions…">{{ old('content', $termsContent) }}</textarea>
                <p class="text-xs mt-1" style="color:#555">
                    Saved to <code style="color:#6EDCD3">app_config/policies → terms_content</code> in Firestore.
                    The public <a href="{{ route('legal.terms') }}" target="_blank" style="color:#6EDCD3">/terms</a> page renders this content.
                </p>
            </div>

            {{-- Version bump --}}
            <div style="border-top:1px solid rgba(255,255,255,0.07)" class="pt-4">
                <label class="block text-xs font-medium text-white mb-1.5">
                    New Version Number
                    <span class="ml-1 font-normal" style="color:#555">— must be higher than current ({{ $versions['terms_version'] }})</span>
                </label>
                <input type="text" name="version"
                       class="w-full rounded-xl px-4 py-2.5 text-sm mb-1 focus:outline-none focus:ring-1"
                       style="background:#252525;border:1px solid rgba(255,255,255,0.1);color:#ccc"
                       placeholder="e.g. {{ number_format((float)$versions['terms_version'] + 0.1, 1) }}"
                       pattern="\d+\.\d+" required>
                <p class="text-xs mb-4" style="color:#555">
                    <i class="bi bi-exclamation-triangle mr-1" style="color:#fbbf24"></i>
                    Bumping the version forces all users to re-accept before they can continue.
                </p>

                <button type="submit"
                        class="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-semibold transition-colors"
                        style="background:#6EDCD3;color:#0D0D0D"
                        onmouseover="this.style.background='#5ac8bf'"
                        onmouseout="this.style.background='#6EDCD3'"
                        onclick="return confirm('Save content and bump Terms & Conditions to the new version?\n\nAll users will be prompted to re-accept on next login.')">
                    <i class="bi bi-cloud-upload"></i>
                    Save &amp; Publish New Version
                </button>
            </div>
        </form>
    </div>

    {{-- ── Privacy Policy ─────────────────────────────────────────────────── --}}
    <div class="rounded-2xl overflow-hidden" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.07)">

        {{-- Card header --}}
        <div class="flex items-center gap-3 px-6 py-4" style="border-bottom:1px solid rgba(255,255,255,0.07)">
            <div class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
                 style="background:rgba(110,220,211,0.12)">
                <i class="bi bi-shield-lock text-sm" style="color:#6EDCD3"></i>
            </div>
            <div class="flex-1 min-w-0">
                <div class="font-semibold text-white text-sm">Privacy Policy</div>
                <div class="flex items-center gap-2 mt-0.5">
                    <span class="text-xs font-medium px-2 py-0.5 rounded-full"
                          style="background:rgba(110,220,211,0.12);color:#6EDCD3">
                        Firestore: <strong>privacy_version</strong> = "{{ $versions['privacy_version'] }}"
                    </span>
                </div>
            </div>
            <a href="{{ route('legal.privacy') }}" target="_blank"
               class="text-xs flex items-center gap-1 flex-shrink-0" style="color:#6EDCD3">
                Preview <i class="bi bi-box-arrow-up-right text-[10px]"></i>
            </a>
        </div>

        <form method="POST" action="{{ route('admin.policies.update') }}" class="p-6 space-y-4">
            @csrf
            <input type="hidden" name="type" value="privacy">

            {{-- Content editor --}}
            <div>
                <div class="flex items-center justify-between mb-1.5">
                    <label class="text-xs font-medium text-white">Policy Content</label>
                    <span class="text-xs" style="color:#555">HTML supported</span>
                </div>
                <textarea name="content" rows="12"
                          class="w-full rounded-xl px-4 py-3 text-xs font-mono leading-relaxed resize-y
                                 focus:outline-none focus:ring-1"
                          style="background:#111;border:1px solid rgba(255,255,255,0.1);color:#ccc;
                                 min-height:240px"
                          placeholder="Enter HTML content for the Privacy Policy…">{{ old('content', $privacyContent) }}</textarea>
                <p class="text-xs mt-1" style="color:#555">
                    Saved to <code style="color:#6EDCD3">app_config/policies → privacy_content</code> in Firestore.
                    The public <a href="{{ route('legal.privacy') }}" target="_blank" style="color:#6EDCD3">/privacy</a> page renders this content.
                </p>
            </div>

            {{-- Version bump --}}
            <div style="border-top:1px solid rgba(255,255,255,0.07)" class="pt-4">
                <label class="block text-xs font-medium text-white mb-1.5">
                    New Version Number
                    <span class="ml-1 font-normal" style="color:#555">— must be higher than current ({{ $versions['privacy_version'] }})</span>
                </label>
                <input type="text" name="version"
                       class="w-full rounded-xl px-4 py-2.5 text-sm mb-1 focus:outline-none focus:ring-1"
                       style="background:#252525;border:1px solid rgba(255,255,255,0.1);color:#ccc"
                       placeholder="e.g. {{ number_format((float)$versions['privacy_version'] + 0.1, 1) }}"
                       pattern="\d+\.\d+" required>
                <p class="text-xs mb-4" style="color:#555">
                    <i class="bi bi-exclamation-triangle mr-1" style="color:#fbbf24"></i>
                    POPIA requires all users to explicitly re-consent when the Privacy Policy changes.
                </p>

                <button type="submit"
                        class="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-semibold transition-colors"
                        style="background:#6EDCD3;color:#0D0D0D"
                        onmouseover="this.style.background='#5ac8bf'"
                        onmouseout="this.style.background='#6EDCD3'"
                        onclick="return confirm('Save content and bump Privacy Policy to the new version?\n\nAll users must re-consent under POPIA.')">
                    <i class="bi bi-cloud-upload"></i>
                    Save &amp; Publish New Version
                </button>
            </div>
        </form>
    </div>
</div>

{{-- Firestore structure info --}}
<div class="rounded-2xl p-5" style="background:rgba(110,220,211,0.04);border:1px solid rgba(110,220,211,0.1)">
    <div class="flex gap-4">
        <i class="bi bi-database flex-shrink-0 mt-0.5" style="color:#6EDCD3"></i>
        <div>
            <p class="text-sm font-medium text-white mb-2">Firestore document: <code style="color:#6EDCD3">app_config/policies</code></p>
            <pre class="text-xs rounded-lg p-3 overflow-x-auto" style="background:#111;color:#888;border:1px solid rgba(255,255,255,0.06)">{
  "terms_name":        "Terms &amp; Conditions",
  "terms_version":     "{{ $versions['terms_version'] }}",
  "terms_content":     "&lt;h2&gt;...&lt;/h2&gt;&lt;p&gt;...&lt;/p&gt;",
  "terms_updated_at":  "{{ $versions['terms_updated_at'] ?? now()->toISOString() }}",

  "privacy_name":      "Privacy Policy",
  "privacy_version":   "{{ $versions['privacy_version'] }}",
  "privacy_content":   "&lt;h2&gt;...&lt;/h2&gt;&lt;p&gt;...&lt;/p&gt;",
  "privacy_updated_at":"{{ $versions['privacy_updated_at'] ?? now()->toISOString() }}"
}</pre>
            <p class="text-xs mt-2" style="color:#666">
                The Android app and web portal read <code style="color:#6EDCD3">terms_version</code> / <code style="color:#6EDCD3">privacy_version</code>
                to decide whether to prompt re-acceptance.
                The <code style="color:#6EDCD3">*_content</code> fields are served via
                <code style="color:#6EDCD3">GET /api/v1/policies/current</code> and rendered on the public legal pages.
            </p>
        </div>
    </div>
</div>

@endsection
