# BudgetBuddy

A personal finance platform for tracking expenses, managing budgets, monitoring debt, and reaching savings goals — with an Android app, a web admin, user portal, and a REST API connecting them.

## AI Assistance

Several parts of this project were built with AI assistance (Claude by Anthropic):

| Area | What AI helped with |
|---|---|
| **Localization** | Translated all UI strings in `strings.xml` into Sesotho (`st`), Swahili (`sw`), and Zulu (`zu`) — three South African languages with limited machine-translation tooling |
| **Security hardening** | Android: `FLAG_SECURE`, root detection, `EncryptedSharedPreferences`, `network_security_config.xml`, ProGuard R8 rules, session expiry countdown. Web/API: CSP headers, HSTS, rate limiting, Firebase token middleware, CORS allowlist |
| **Firebase rules** | Authored `firestore.rules` (UID-scoped user data, `app_config` read-only for auth users) and `storage.rules` (MIME-type enforcement, per-path size limits, bank statement write-protection) |
| **API documentation** | OpenAPI 3.0 spec (`web/public/api-docs/openapi.yaml`) and Swagger UI integration |

---

## Video Demo

[PART 2 - Watch the demo on YouTube](https://youtu.be/lt0lGFa9MV8)
[PART 3 - Watch the demo on YouTube](https://youtu.be/XXX)

> The video walks through all app features with a voice-over explaining the implementation.

## APK Download

The APK is available in the [Releases](https://github.com/st10147510/budgetbuddy/releases) section of this repository.

---

## Repository Structure

```
budgetbuddy/
├── android/          # Kotlin Android app (MVVM, Room, Firebase)
├── web/              # Laravel PHP admin panel + REST API
├── ios/              # iOS (placeholder — not yet implemented)
├── firebase.json
├── firestore.rules   # Security rules for Cloud Firestore
├── storage.rules     # Security rules for Firebase Storage
└── firestore.indexes.json
```

---

## Android App

### Features

| Feature | Description |
|---|---|
| Expense Tracking | Log income and expenses with categories, notes, dates, and optional receipt photos |
| Budgets | Per-category spending limits and minimum spending goals; compliance chip (On Track / Near Limit / Over Limit / Below Goal) |
| Savings Goals | Track progress toward financial goals; add contributions at any time |
| Debt Management | Monitor debts with payoff schedules |
| Payment Plans | Month-by-month payoff schedule using Snowball or Avalanche strategy |
| Reports | Monthly net-balance chart, budget vs actual bar chart, full transaction list — filterable by month |
| Detail & Edit Screens | Full detail and edit views for transactions, budgets, debts, and goals |
| Bank Statement Upload | Upload a PDF bank statement from the device; tracks processing status via the web API |
| Policy Acceptance | Versioned Terms & Conditions and Privacy Policy gate; users must accept before accessing the app |
| Gamification | Earn badges for milestones (first transaction, 7-day streak) |
| Notifications | Budget alert workers and daily reminders via WorkManager |
| Offline-first | Room database with write-through sync to Firestore on every save/update/delete |
| Cloud Auth | Firebase Authentication (email/password) with friendly error messages and password reset |
| 1-Hour Session | Session expires after 60 minutes; a 30-second countdown dialog lets users extend or sign out |
| Localization | 12 languages: English, Sesotho, Swahili, Zulu, Afrikaans, Arabic, German, Spanish, French, Italian, Dutch, Portuguese |

### Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9.23, JVM 17 |
| UI | Fragments, ViewBinding, Navigation Component |
| Architecture | Single-Activity MVVM, StateFlow, Hilt DI |
| Local DB | Room 2.6.1 (`budget_buddy.db`) |
| Auth | Firebase Authentication (email/password) |
| Cloud DB | Firebase Firestore (write-through sync) |
| Storage | Firebase Storage (receipt + profile photos) |
| Networking | Retrofit 2.11 + OkHttp 4.12 (REST API calls) |
| Secure Storage | EncryptedSharedPreferences (AES-256-GCM) |
| Charts | MPAndroidChart 3.1.0 |
| Background | WorkManager 2.9.1 |
| Preferences | DataStore 1.1.1 |
| Testing | JUnit 4, Mockito-Kotlin, Turbine, Hilt Test |
| Min SDK | 26 (Android 8.0) — Target SDK 35 |

### Android Security Hardening

- **`FLAG_SECURE`** — prevents screenshots and screen recording of financial data
- **Root detection** — warns users on rooted/compromised devices (non-blocking, release builds only)
- **EncryptedSharedPreferences** — session tokens stored with AES-256-GCM via Android Keystore
- **Firebase ID Token auth** — all API calls carry a short-lived Firebase bearer token
- **Network security config** — cleartext traffic blocked everywhere except `10.0.2.2`/`localhost` (emulator only)
- **ProGuard R8** — `Log.d/v/i` stripped from release builds; all sensitive DTOs kept
- **Session expiry** — 1-hour hard limit with a 30-second countdown warning dialog

### Project Structure

```
android/app/src/main/java/com/budgetbuddy/
├── data/
│   ├── local/
│   │   ├── BudgetBuddyDatabase.kt
│   │   ├── dao/
│   │   ├── entities/
│   │   └── SessionManager.kt          # EncryptedSharedPreferences, session expiry
│   ├── remote/
│   │   ├── BudgetBuddyApiService.kt   # Retrofit interface
│   │   └── dto/                       # Request/response DTOs
│   └── repository/
│       ├── AuthRepository.kt
│       ├── PolicyRepository.kt        # Reads/writes policy acceptance in Firestore
│       ├── StatementUploadRepository.kt
│       └── ...
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── NetworkModule.kt               # OkHttp + Retrofit + FirebaseTokenInterceptor
├── ui/
│   ├── MainActivity.kt                # Session countdown, root detection, FLAG_SECURE
│   ├── auth/
│   │   ├── PolicyAcceptanceFragment.kt
│   │   ├── PolicyAcceptanceViewModel.kt
│   │   └── ...
│   ├── statement/
│   │   ├── UploadStatementFragment.kt
│   │   └── UploadStatementViewModel.kt
│   └── ...
└── util/
    ├── LocaleHelper.kt
    ├── SecurityUtils.kt               # Root detection, debug build check
    └── ...
```

### Build & Run

```bash
cd android

./gradlew assembleDebug          # Debug APK
./gradlew clean assembleDebug    # Clean build
./gradlew installDebug           # Install to connected device
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (requires device/emulator)
./gradlew lint
```

Place `google-services.json` in `android/app/` before building.

---

## Web Admin Panel & REST API

### Features

| Feature | Description |
|---|---|
| Admin Dashboard | Total / active / disabled / new users from Firebase Auth |
| User Management | List, search, view, disable, enable, reset password, delete users |
| Policy Management | View and bump version numbers for Terms & Conditions and Privacy Policy |
| Bank Statement Jobs | View upload queue; shows status of each processing job |
| Public Legal Pages | `/terms` and `/privacy` — standalone pages, no login required |

### REST API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/policies/current` | None | Current policy versions |
| `POST` | `/api/v1/policies/accept` | Firebase token | Record policy acceptance |
| `GET` | `/api/v1/policies/status` | Firebase token | Per-user acceptance status |
| `GET` | `/api/v1/statements` | Firebase token | List statement upload jobs |
| `GET` | `/api/v1/statements/{id}` | Firebase token | Single job status |
| `POST` | `/api/v1/statements` | Firebase token | Upload PDF bank statement |

All authenticated endpoints require `Authorization: Bearer <Firebase ID token>`.

**Interactive API docs:** `GET /docs` — Swagger UI backed by `public/api-docs/openapi.yaml`.

### Web & API Security Hardening

- **Security headers** — CSP, HSTS (HTTPS only), `X-Frame-Options: SAMEORIGIN`, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`, strips `X-Powered-By` and `Server`
- **Rate limiting** — `throttle:3,15` on admin login; `throttle:5,15` on portal login; 120 req/min per Firebase UID on API (falls back to IP)
- **Firebase ID token verification** — `FirebaseApiAuth` middleware verifies every API token with the kreait SDK
- **CORS** — restricted to `https://thebudgetbuddy.co.za`, `http://localhost:8000`, `http://10.0.2.2:8000`; no wildcard origins
- **Input validation** — PDF mime-type and size (max 10 MB) enforced on upload; policy version pattern `^\d+\.\d+$`
- **Session hardening** — `SESSION_ENCRYPT=true`, `SESSION_SECURE_COOKIE=true`, `SESSION_SAME_SITE=strict`
- **bcrypt cost 12** — ~250 ms/hash, good security/speed balance

### Web Local Setup

```bash
cd web
cp .env.example .env
php artisan key:generate

# Place Firebase service account JSON at web/firebase-service-account.json
# Set in .env:
#   FIREBASE_CREDENTIALS=firebase-service-account.json
#   FIREBASE_STORAGE_DEFAULT_BUCKET=your-bucket.appspot.com
#   ADMIN_PASSWORD=your-secret-password

php artisan migrate
php artisan serve          # http://localhost:8000/admin/login
```

### Docker (Production)

```bash
cd web
docker build -t budgetbuddy-web .

docker run -d \
  -p 80:80 \
  -e APP_KEY=base64:... \
  -e ADMIN_PASSWORD=... \
  -e FIREBASE_CREDENTIALS_JSON='{"type":"service_account",...}' \
  -e FIREBASE_STORAGE_DEFAULT_BUCKET=... \
  budgetbuddy-web
```

The multi-stage Dockerfile (base → deps → builder → production) produces an Alpine image with PHP-FPM, nginx, and supervisor.

---

## Firebase Rules

Security rules live at the repo root and are deployed with:

```bash
firebase deploy --only firestore:rules,storage
```

**Firestore** — all user data is isolated by UID (`users/{userId}/...`). `app_config/policies` is readable by any authenticated user (write blocked — service account only).

**Storage** — profile photos: 5 MB max, images only, keyed to UID. Receipts: 10 MB max, images only, owner-only access. Bank statements: client write blocked; service account writes via Admin SDK. Everything else denied.

---

## CI/CD

| Job | Trigger | What it does |
|---|---|---|
| `test` | Every push / PR | PHP 8.3, SQLite, PHPUnit — runs all tests |
| `docker` | Push to `main` | Builds Docker image, pushes to GHCR |
| `release` | Tag `v*` | Creates GitHub Release with Docker pull snippet |

Workflow file: `.github/workflows/web-build.yml`

---

## Production Readiness Checklist

### Immediate (before going live)

- [ ] Set `APP_DEBUG=false` and `LOG_LEVEL=error` in production `.env`
- [ ] Generate a strong `APP_KEY` and rotate `ADMIN_PASSWORD`
- [ ] Switch from SQLite to PostgreSQL (`DB_CONNECTION=pgsql`)
- [ ] Configure a real SMTP provider in `MAIL_*` variables
- [ ] Deploy Firestore and Storage rules: `firebase deploy --only firestore:rules,storage`
- [ ] Verify `SESSION_ENCRYPT=true`, `SESSION_SECURE_COOKIE=true` are set
- [ ] Add certificate pinning in Android `NetworkModule` for the production API host
- [ ] Enable Firebase App Check on the Firebase console and in the Android app

### Short-term (within first month)

- [ ] Replace `CACHE_STORE=database` with Redis (`CACHE_STORE=redis`)
- [ ] Replace `QUEUE_CONNECTION=database` with Redis (`QUEUE_CONNECTION=redis`) and deploy a queue worker
- [ ] Integrate structured logging / error tracking (Sentry or Datadog)
- [ ] Add email verification flow after sign-up
- [ ] Set up automated database backups (pg_dump → object storage)
- [ ] Put the Laravel app behind a CDN (Cloudflare) for DDoS protection and TLS termination
- [ ] Integrate Google Play Integrity API to block sideloaded / tampered builds

### Nice to have

- [ ] Biometric unlock (Android BiometricPrompt) as a PIN-alternative
- [ ] FCM push notifications for budget alerts instead of local-only WorkManager
- [ ] CSV / PDF export from the mobile app
- [ ] Multi-currency support with live exchange rates
- [ ] Plaid / bank open-banking integration for automatic transaction import
- [ ] Penetration test by a third-party security firm before public launch

---

## Architecture Overview

```
Android App
  └── Hilt DI
       ├── Room (local-first, offline)
       ├── Firebase Auth / Firestore / Storage (sync layer)
       └── Retrofit → Web REST API
              ├── FirebaseTokenInterceptor (Bearer auth)
              └── OkHttp (network security config)

Web Admin (Laravel)
  └── kreait Firebase Admin SDK
       ├── Firebase Auth (user management)
       ├── Firestore (policy versions, acceptance records)
       └── Firebase Storage (bank statement files)
  └── Middleware stack
       ├── SecurityHeaders (CSP, HSTS, ...)
       ├── AdminAuth (session guard)
       ├── FirebaseApiAuth (ID token verification)
       └── ApiCors + rate limiting
```

---

## License

This project is for personal/educational use.
