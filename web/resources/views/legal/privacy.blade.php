<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Privacy Policy — BudgetBuddy</title>
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
        <h1 class="text-3xl font-bold text-white mb-2">Privacy Policy</h1>
        <p class="text-sm" style="color:#555">
            Version {{ $version ?? '1.0' }}
            @if(!empty($updatedAt)) · Updated {{ \Carbon\Carbon::parse($updatedAt)->format('d M Y') }} @endif
        </p>
    </div>

    <div class="prose">
    @if(!empty($firestoreContent))
        {!! $firestoreContent !!}
    @else
        <h2>1. Information We Collect</h2>
        <ul>
            <li><strong style="color:#ccc">Account information:</strong> email address and display name when you register.</li>
            <li><strong style="color:#ccc">Financial data:</strong> transaction records, budgets, goals, and debts that you enter or upload.</li>
            <li><strong style="color:#ccc">Bank statement files:</strong> PDF files you choose to upload for automatic transaction extraction.</li>
            <li><strong style="color:#ccc">Usage data:</strong> anonymous analytics to improve the App (device type, app version, feature usage).</li>
        </ul>

        <h2>2. How We Use Your Information</h2>
        <ul>
            <li>To provide and improve the BudgetBuddy service.</li>
            <li>To extract and categorise transactions from uploaded bank statements.</li>
            <li>To send important service notifications (e.g., bill reminders you configure).</li>
            <li>To comply with legal obligations.</li>
        </ul>

        <h2>3. Data Storage and Security</h2>
        <p>Your data is stored using Google Firebase (Firestore and Firebase Storage), hosted on Google Cloud Platform infrastructure with South African regional preferences where available. We implement industry-standard security measures including encryption in transit and at rest.</p>

        <h2>4. Data Sharing</h2>
        <p>We do not sell, trade, or rent your personal information. We may share data with:</p>
        <ul>
            <li><strong style="color:#ccc">Google Firebase:</strong> for data storage and authentication.</li>
            <li><strong style="color:#ccc">Legal authorities:</strong> only when required by applicable law.</li>
        </ul>

        <h2>5. Your Rights (POPIA)</h2>
        <p>Under the Protection of Personal Information Act (POPIA), you have the right to:</p>
        <ul>
            <li>Access the personal information we hold about you.</li>
            <li>Request correction of inaccurate information.</li>
            <li>Request deletion of your data.</li>
            <li>Object to processing of your information.</li>
        </ul>
        <p>To exercise these rights, contact us at <span style="color:#6EDCD3">privacy@thebudgetbuddy.co.za</span>.</p>

        <h2>6. Data Retention</h2>
        <p>We retain your data for as long as your account is active. When you delete your account, your data is permanently removed within 30 days, except where retention is required by law.</p>

        <h2>7. Cookies and Analytics</h2>
        <p>The BudgetBuddy web portal uses session cookies for authentication. We use Firebase Analytics to collect anonymised usage statistics. No third-party advertising cookies are used.</p>

        <h2>8. Children's Privacy</h2>
        <p>BudgetBuddy is not intended for users under 18 years of age. We do not knowingly collect personal information from children.</p>

        <h2>9. Changes to This Policy</h2>
        <p>We may update this Privacy Policy periodically. We will notify you of significant changes via the App. Continued use of the Service after the effective date constitutes your acceptance.</p>

        <h2>10. Contact</h2>
        <p>For privacy-related enquiries, contact our Information Officer at <span style="color:#6EDCD3">privacy@thebudgetbuddy.co.za</span>.</p>
    @endif
    </div>
</div>
</body>
</html>
