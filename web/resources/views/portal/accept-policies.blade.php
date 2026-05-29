@extends('portal.layout')

@section('title', 'Accept Policies — BudgetBuddy')

@section('content')
<div class="max-w-2xl mx-auto">

    <div class="rounded-2xl p-8 mb-6" style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.06)">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center mb-6"
             style="background:rgba(110,220,211,0.12)">
            <i class="bi bi-shield-check text-xl" style="color:#6EDCD3"></i>
        </div>
        <h1 class="text-2xl font-bold text-white mb-2">Please review our policies</h1>
        <p class="text-sm mb-8" style="color:#999">
            We've updated our Terms &amp; Conditions and Privacy Policy. Please read and accept them to continue using BudgetBuddy.
        </p>

        {{-- Policy version badges --}}
        <div class="flex flex-wrap gap-3 mb-8">
            <a href="{{ route('legal.terms') }}" target="_blank"
               class="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors"
               style="background:#2A2A2A;color:#ccc;border:1px solid rgba(255,255,255,0.08)"
               onmouseover="this.style.borderColor='rgba(110,220,211,0.4)';this.style.color='#6EDCD3'"
               onmouseout="this.style.borderColor='rgba(255,255,255,0.08)';this.style.color='#ccc'">
                <i class="bi bi-file-earmark-text"></i>
                Terms &amp; Conditions
                <span class="text-xs px-1.5 py-0.5 rounded" style="background:rgba(110,220,211,0.12);color:#6EDCD3">
                    v{{ $versions['terms_version'] }}
                </span>
                <i class="bi bi-arrow-up-right text-xs"></i>
            </a>
            <a href="{{ route('legal.privacy') }}" target="_blank"
               class="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors"
               style="background:#2A2A2A;color:#ccc;border:1px solid rgba(255,255,255,0.08)"
               onmouseover="this.style.borderColor='rgba(110,220,211,0.4)';this.style.color='#6EDCD3'"
               onmouseout="this.style.borderColor='rgba(255,255,255,0.08)';this.style.color='#ccc'">
                <i class="bi bi-shield-lock"></i>
                Privacy Policy
                <span class="text-xs px-1.5 py-0.5 rounded" style="background:rgba(110,220,211,0.12);color:#6EDCD3">
                    v{{ $versions['privacy_version'] }}
                </span>
                <i class="bi bi-arrow-up-right text-xs"></i>
            </a>
        </div>

        <form method="POST" action="{{ route('portal.policies.accept') }}">
            @csrf

            <div class="space-y-4 mb-8">
                <label class="flex items-start gap-3 cursor-pointer group">
                    <div class="relative mt-0.5">
                        <input type="checkbox" name="accept_terms" value="1" required
                               class="sr-only peer">
                        <div class="w-5 h-5 rounded border transition-all peer-checked:border-bb-teal"
                             style="border-color:rgba(255,255,255,0.2);background:#2A2A2A"
                             id="terms-box">
                            <i class="bi bi-check hidden text-xs absolute top-0 left-0.5" style="color:#6EDCD3" id="terms-check"></i>
                        </div>
                    </div>
                    <span class="text-sm" style="color:#ccc">
                        I have read and agree to the
                        <a href="{{ route('legal.terms') }}" target="_blank" style="color:#6EDCD3">Terms &amp; Conditions</a>
                        (v{{ $versions['terms_version'] }})
                    </span>
                </label>

                <label class="flex items-start gap-3 cursor-pointer group">
                    <div class="relative mt-0.5">
                        <input type="checkbox" name="accept_privacy" value="1" required
                               class="sr-only peer">
                        <div class="w-5 h-5 rounded border transition-all"
                             style="border-color:rgba(255,255,255,0.2);background:#2A2A2A">
                        </div>
                    </div>
                    <span class="text-sm" style="color:#ccc">
                        I have read and agree to the
                        <a href="{{ route('legal.privacy') }}" target="_blank" style="color:#6EDCD3">Privacy Policy</a>
                        (v{{ $versions['privacy_version'] }}) and consent to the processing of my personal information
                        in accordance with POPIA.
                    </span>
                </label>
            </div>

            <button type="submit" class="bb-btn w-full">
                <i class="bi bi-check-circle mr-1"></i>
                I Accept — Continue to Dashboard
            </button>
        </form>
    </div>

    <p class="text-center text-xs" style="color:#555">
        By accepting, you confirm you are 18 years or older and have the legal capacity to enter into these agreements.
    </p>
</div>
@endsection
