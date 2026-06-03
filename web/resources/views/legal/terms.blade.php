<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Terms &amp; Conditions — BudgetBuddy</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    @vite(['resources/css/app.css'])
    <style>
        body { background:#0D0D0D; color:#e0e0e0; font-family:'Inter',sans-serif; }
        .prose h2 { color:#fff; font-size:1.125rem; font-weight:600; margin:2rem 0 .75rem; }
        .prose p,
        .prose li { color:#999; line-height:1.75; font-size:.9375rem; }
        .prose ul { padding-left:1.25rem; list-style:disc; }
    </style>
</head>
<body>
<div class="max-w-3xl mx-auto px-6 py-16">
    <a href="{{ url('/') }}" class="inline-flex items-center gap-2 text-sm mb-10" style="color:#6EDCD3">
        ← Back to home
    </a>

    <div class="mb-8">
        <h1 class="text-3xl font-bold text-white mb-2">Terms &amp; Conditions</h1>
        <p class="text-sm" style="color:#555">
            Version {{ $version ?? '1.0' }}
            @if(!empty($updatedAt)) · Updated {{ \Carbon\Carbon::parse($updatedAt)->format('d M Y') }} @endif
        </p>
    </div>

    <div class="prose">
    @if(!empty($firestoreContent))
        {!! $firestoreContent !!}
    @else
        <h2>1. Acceptance of Terms</h2>
        <p>By downloading, installing, or using BudgetBuddy ("the App", "the Service"), you agree to be bound by these Terms and Conditions. If you do not agree, please uninstall the App and stop using the Service immediately.</p>

        <h2>2. Description of Service</h2>
        <p>BudgetBuddy is a personal finance management application that allows you to track income and expenses, set budgets and savings goals, manage debts, and upload bank statements for automatic transaction extraction.</p>

        <h2>3. User Accounts</h2>
        <p>You must create an account using a valid email address. You are responsible for maintaining the confidentiality of your login credentials. You agree to notify us immediately of any unauthorised use of your account.</p>

        <h2>4. User Data and Bank Statements</h2>
        <ul>
            <li>Bank statements you upload are stored securely and processed solely for transaction extraction.</li>
            <li>We do not share your financial data with third parties without your explicit consent.</li>
            <li>You may delete your data at any time by contacting support or deleting your account in the App.</li>
        </ul>

        <h2>5. Acceptable Use</h2>
        <p>You agree not to misuse the Service, including attempting to reverse-engineer, scrape, or otherwise access the Service in an unauthorised manner. You agree not to use the Service for any unlawful purpose.</p>

        <h2>6. Disclaimers</h2>
        <p>BudgetBuddy is a personal finance tracking tool and does not constitute financial advice. Transaction categorisation is automated and may not always be accurate. Always verify important financial decisions with a qualified professional.</p>

        <h2>7. Limitation of Liability</h2>
        <p>To the maximum extent permitted by applicable law, BudgetBuddy and its developers shall not be liable for any indirect, incidental, or consequential damages arising from your use of the Service.</p>

        <h2>8. Changes to Terms</h2>
        <p>We may update these Terms from time to time. We will notify you of material changes via the App. Your continued use of the Service after changes take effect constitutes your acceptance of the revised Terms.</p>

        <h2>9. Governing Law</h2>
        <p>These Terms are governed by the laws of the Republic of South Africa. Any disputes shall be subject to the exclusive jurisdiction of the courts of South Africa.</p>

        <h2>10. Contact</h2>
        <p>For questions about these Terms, please contact us at <span style="color:#6EDCD3">support@thebudgetbuddy.co.za</span>.</p>
    @endif
    </div>
</div>
</body>
</html>
