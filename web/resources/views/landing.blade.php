<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BudgetBuddy — Take Control of Your Finances</title>
    <meta name="description" content="BudgetBuddy helps South Africans track spending, set goals, manage debt and understand their money — on mobile and web.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        * { box-sizing: border-box; }
        body { background: #0D0D0D; color: #fff; font-family: 'Inter', sans-serif; margin: 0; }

        /* Nav */
        .landing-nav {
            position: sticky; top: 0; z-index: 50;
            background: rgba(13,13,13,0.85);
            backdrop-filter: blur(12px);
            border-bottom: 1px solid rgba(255,255,255,0.06);
            display: flex; align-items: center; justify-content: space-between;
            padding: 0 2rem; height: 64px;
        }

        /* Glow blobs */
        .glow-teal {
            position: absolute; border-radius: 50%; filter: blur(80px); pointer-events: none;
            background: radial-gradient(circle, rgba(110,220,211,0.18) 0%, transparent 70%);
        }
        .glow-coral {
            position: absolute; border-radius: 50%; filter: blur(80px); pointer-events: none;
            background: radial-gradient(circle, rgba(232,132,122,0.12) 0%, transparent 70%);
        }

        /* Pill badge */
        .pill {
            display: inline-flex; align-items: center; gap: 0.5rem;
            padding: 0.25rem 0.875rem; border-radius: 999px;
            background: rgba(110,220,211,0.1); color: #6EDCD3;
            border: 1px solid rgba(110,220,211,0.2);
            font-size: 0.75rem; font-weight: 600;
        }

        /* Feature card */
        .feat-card {
            background: #1A1A1A;
            border: 1px solid rgba(255,255,255,0.06);
            border-radius: 1.25rem; padding: 1.75rem;
            transition: border-color 0.2s, transform 0.2s;
        }
        .feat-card:hover { border-color: rgba(110,220,211,0.25); transform: translateY(-2px); }

        /* Stat card */
        .stat-card {
            background: #1A1A1A;
            border: 1px solid rgba(255,255,255,0.06);
            border-radius: 1rem; padding: 1.5rem; text-align: center;
        }

        /* Step */
        .step-number {
            width: 2rem; height: 2rem; border-radius: 50%;
            background: #6EDCD3; color: #0D0D0D;
            font-weight: 700; font-size: 0.875rem;
            display: flex; align-items: center; justify-content: center;
            flex-shrink: 0;
        }

        /* CTA section */
        .cta-section {
            background: linear-gradient(135deg, #1A1A1A 0%, #111 100%);
            border: 1px solid rgba(110,220,211,0.15);
            border-radius: 1.5rem; padding: 4rem 2rem; text-align: center;
            position: relative; overflow: hidden;
        }

        /* Footer */
        footer {
            border-top: 1px solid rgba(255,255,255,0.06);
            color: #555; font-size: 0.8125rem; text-align: center;
            padding: 2rem 1rem;
        }

        /* Responsive max-width container */
        .container { max-width: 1100px; margin: 0 auto; padding: 0 1.5rem; }

        /* Nav links */
        .nav-link { color: #999; font-size: 0.875rem; font-weight: 500; text-decoration: none; padding: 0.375rem 0.75rem; border-radius: 0.5rem; transition: color 0.15s; }
        .nav-link:hover { color: #fff; }
    </style>
</head>
<body>

{{-- ── Nav ── --}}
<nav class="landing-nav">
    <div class="flex items-center gap-2">
        <div style="width:32px;height:32px;border-radius:10px;background:#6EDCD3;display:flex;align-items:center;justify-content:center">
            <i class="bi bi-piggy-bank" style="color:#0D0D0D;font-size:1rem"></i>
        </div>
        <span style="font-weight:700;font-size:1.1rem">Budget<span style="color:#6EDCD3">Buddy</span></span>
    </div>

    <div class="hidden sm:flex items-center gap-1">
        <a href="#features" class="nav-link">Features</a>
        <a href="#how-it-works" class="nav-link">How it works</a>
        <a href="#stats" class="nav-link">Impact</a>
    </div>

    <div style="display:flex;align-items:center;gap:0.75rem">
        <a href="{{ route('portal.login') }}" class="nav-link">Sign in</a>
        <a href="{{ route('portal.login') }}" class="bb-btn" style="padding:0.5rem 1.25rem;font-size:0.875rem">
            Get started
        </a>
    </div>
</nav>

{{-- ── Hero ── --}}
<section style="position:relative;overflow:hidden;padding:7rem 1.5rem 6rem">
    <div class="glow-teal" style="width:600px;height:600px;top:-200px;left:50%;transform:translateX(-50%)"></div>
    <div class="glow-coral" style="width:400px;height:400px;bottom:-100px;right:-100px"></div>

    <div class="container" style="text-align:center;position:relative;z-index:1">
        <div class="pill" style="margin-bottom:1.5rem">
            <span style="width:6px;height:6px;border-radius:50%;background:#6EDCD3;display:inline-block"></span>
            Built for South Africa
        </div>

        <h1 style="font-size:clamp(2.5rem,6vw,4rem);font-weight:800;line-height:1.1;margin:0 auto 1.5rem;max-width:780px">
            Your finances,<br><span style="color:#6EDCD3">finally under control</span>
        </h1>

        <p style="font-size:1.125rem;color:#999;max-width:560px;margin:0 auto 2.5rem;line-height:1.7">
            BudgetBuddy tracks your income, expenses, goals, and debt automatically. Upload a bank statement and see exactly where your money goes.
        </p>

        <div style="display:flex;gap:1rem;justify-content:center;flex-wrap:wrap">
            <a href="{{ route('portal.login') }}" class="bb-btn" style="font-size:1rem;padding:0.875rem 2rem">
                <i class="bi bi-arrow-right-circle"></i> Start for free
            </a>
            <a href="#how-it-works"
               style="display:inline-flex;align-items:center;gap:0.5rem;padding:0.875rem 2rem;border-radius:0.75rem;border:1px solid rgba(255,255,255,0.12);color:#ccc;font-size:1rem;font-weight:600;text-decoration:none;transition:border-color 0.15s"
               onmouseover="this.style.borderColor='rgba(110,220,211,0.4)'"
               onmouseout="this.style.borderColor='rgba(255,255,255,0.12)'">
                <i class="bi bi-play-circle"></i> See how it works
            </a>
        </div>

        {{-- Mock phone card --}}
        <div style="margin-top:4rem;max-width:320px;margin-left:auto;margin-right:auto">
            <div style="background:#1A1A1A;border:1px solid rgba(255,255,255,0.08);border-radius:1.5rem;padding:1.5rem;text-align:left">
                <div style="font-size:0.75rem;color:#666;margin-bottom:0.5rem">This month's net</div>
                <div style="font-size:2.25rem;font-weight:800;color:#6EDCD3;margin-bottom:0.25rem">R12,340.00</div>
                <div style="font-size:0.75rem;color:#555;margin-bottom:1.25rem">↑ R55,000 · ↓ R42,660</div>
                <div style="height:1px;background:rgba(255,255,255,0.06);margin-bottom:1rem"></div>
                @foreach([
                    ['🛒', 'Groceries', '-R4,200', '#F44336'],
                    ['🚗', 'Transport', '-R1,850', '#F44336'],
                    ['💰', 'Savings',   '+R8,000', '#4CAF50'],
                ] as [$ico, $label, $amt, $col])
                <div style="display:flex;align-items:center;gap:0.75rem;margin-bottom:0.75rem">
                    <div style="width:32px;height:32px;border-radius:50%;background:#2A2A2A;display:flex;align-items:center;justify-content:center;font-size:0.875rem">{{ $ico }}</div>
                    <div style="flex:1;font-size:0.875rem;color:#ccc">{{ $label }}</div>
                    <div style="font-size:0.875rem;font-weight:600;color:{{ $col }}">{{ $amt }}</div>
                </div>
                @endforeach
            </div>
        </div>
    </div>
</section>

{{-- ── Stats ── --}}
<section id="stats" style="padding:4rem 1.5rem;border-top:1px solid rgba(255,255,255,0.06)">
    <div class="container">
        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:1.5rem">
            @foreach([
                ['R0 → R∞', 'Money tracked for every rand you earn and spend'],
                ['10 categories', 'Auto-classified expense categories out of the box'],
                ['PDF → data', 'Instant transaction extraction from any SA bank statement'],
                ['100% private', 'Your data syncs to your personal cloud account only'],
            ] as [$stat, $desc])
            <div class="stat-card">
                <div style="font-size:1.75rem;font-weight:800;color:#6EDCD3;margin-bottom:0.5rem">{{ $stat }}</div>
                <div style="font-size:0.8125rem;color:#666;line-height:1.5">{{ $desc }}</div>
            </div>
            @endforeach
        </div>
    </div>
</section>

{{-- ── Features ── --}}
<section id="features" style="padding:5rem 1.5rem">
    <div class="container">
        <div style="text-align:center;margin-bottom:3.5rem">
            <div class="pill" style="margin-bottom:1rem">Everything you need</div>
            <h2 style="font-size:clamp(1.75rem,4vw,2.5rem);font-weight:800;margin:0">
                Built around your real financial life
            </h2>
            <p style="color:#666;margin-top:1rem;font-size:1rem">No spreadsheets. No manual entry. Just your finances, made clear.</p>
        </div>

        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:1.5rem">
            @foreach([
                ['bi-upload',           '#6EDCD3', 'Bank Statement Upload',   'Drag-and-drop your PDF statement and BudgetBuddy extracts every transaction automatically — dates, amounts, descriptions and categories.'],
                ['bi-tag',              '#E8847A', 'Smart Categorisation',    'Transactions are automatically mapped to 10 categories: groceries, transport, utilities, healthcare and more, using SA-specific merchant recognition.'],
                ['bi-graph-up-arrow',   '#F5D5A8', 'Income vs Expense',       'See this month and all-time income, expenses and net balance at a glance — on mobile and on the web portal, always in sync.'],
                ['bi-bullseye',         '#6EDCD3', 'Savings Goals',           'Set savings goals with target amounts and track progress. BudgetBuddy shows exactly how far you are and adjusts as you save.'],
                ['bi-pie-chart',        '#E8847A', 'Budget Tracking',         'Create monthly budgets per category. Get warned when you\'re approaching the limit and see which areas are overspent.'],
                ['bi-credit-card',      '#F5D5A8', 'Debt Management',         'Track your debts with Snowball or Avalanche payoff strategies. See exactly when you\'ll be debt-free based on your payment schedule.'],
            ] as [$icon, $col, $title, $desc])
            <div class="feat-card">
                <div style="width:44px;height:44px;border-radius:12px;background:rgba(255,255,255,0.05);display:flex;align-items:center;justify-content:center;margin-bottom:1.25rem">
                    <i class="bi {{ $icon }}" style="color:{{ $col }};font-size:1.25rem"></i>
                </div>
                <h3 style="font-size:1rem;font-weight:600;margin:0 0 0.625rem;color:#fff">{{ $title }}</h3>
                <p style="font-size:0.875rem;color:#666;line-height:1.6;margin:0">{{ $desc }}</p>
            </div>
            @endforeach
        </div>
    </div>
</section>

{{-- ── How it works ── --}}
<section id="how-it-works" style="padding:5rem 1.5rem;border-top:1px solid rgba(255,255,255,0.06)">
    <div class="container">
        <div style="text-align:center;margin-bottom:3.5rem">
            <div class="pill" style="margin-bottom:1rem">Simple setup</div>
            <h2 style="font-size:clamp(1.75rem,4vw,2.5rem);font-weight:800;margin:0">Up and running in minutes</h2>
        </div>

        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:2rem;max-width:960px;margin:0 auto">
            @foreach([
                ['Download the app',          'Get BudgetBuddy from the Play Store and create your free account with just an email address.'],
                ['Sign into the web portal',  'Log in at budgetbuddy.app with the same account — no second registration needed.'],
                ['Upload your bank statement','Drag and drop any SA bank statement PDF. Every transaction is extracted and dated in seconds.'],
                ['Set goals and budgets',     'Create monthly spending budgets per category and savings goals with target amounts to keep you on track.'],
                ['Track debt payoff',         'Add your debts and choose a Snowball or Avalanche strategy — BudgetBuddy calculates your payoff date automatically.'],
                ['Earn badges as you grow',   'Hit milestones like your first transaction, a 7-day streak, or becoming debt-free to unlock achievement badges.'],
            ] as $i => [$title, $desc])
            <div style="display:flex;flex-direction:column;gap:0.875rem">
                <div style="display:flex;align-items:center;gap:1rem">
                    <div class="step-number" style="{{ $i === 5 ? 'background:#E8847A' : '' }}">{{ $i + 1 }}</div>
                    <h3 style="font-size:0.9375rem;font-weight:600;margin:0;color:#fff">{{ $title }}</h3>
                </div>
                <p style="font-size:0.875rem;color:#666;margin:0;padding-left:3rem;line-height:1.6">{{ $desc }}</p>
            </div>
            @endforeach
        </div>
    </div>
</section>

{{-- ── CTA ── --}}
<section style="padding:4rem 1.5rem 5rem">
    <div class="container">
        <div class="cta-section">
            <div class="glow-teal" style="width:500px;height:500px;top:-150px;left:50%;transform:translateX(-50%)"></div>
            <div style="position:relative;z-index:1">
                <div class="pill" style="margin-bottom:1.5rem">Free to use</div>
                <h2 style="font-size:clamp(1.75rem,4vw,2.5rem);font-weight:800;margin:0 auto 1rem;max-width:600px">
                    Start understanding your money today
                </h2>
                <p style="color:#666;margin-bottom:2rem;font-size:1rem">
                    Sign in with the same account you use on the BudgetBuddy Android app.
                </p>
                <div style="display:flex;gap:1rem;justify-content:center;flex-wrap:wrap">
                    <a href="{{ route('portal.login') }}" class="bb-btn" style="font-size:1rem;padding:0.875rem 2rem">
                        <i class="bi bi-arrow-right-circle"></i> Open the portal
                    </a>
                </div>
            </div>
        </div>
    </div>
</section>

<footer>
    <div class="container">
        <div style="display:flex;align-items:center;justify-content:center;gap:0.5rem;margin-bottom:0.75rem">
            <div style="width:24px;height:24px;border-radius:8px;background:#6EDCD3;display:flex;align-items:center;justify-content:center">
                <i class="bi bi-piggy-bank" style="color:#0D0D0D;font-size:0.75rem"></i>
            </div>
            <span style="color:#fff;font-weight:600">Budget<span style="color:#6EDCD3">Buddy</span></span>
        </div>
        <p style="margin:0">Built for South Africa &mdash; &copy; {{ date('Y') }} BudgetBuddy. All rights reserved.</p>
    </div>
</footer>

</body>
</html>
