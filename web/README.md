# BudgetBuddy — Web Admin, User Portal & REST API

> **AI Assistance** — Security hardening (CSP, HSTS, rate limiting, Firebase token middleware), Firebase security rules, and the OpenAPI documentation were generated with AI assistance (Claude by Anthropic).

Laravel 13 / PHP 8.3 admin panel and REST API for the BudgetBuddy personal finance platform. Uses the Firebase Admin SDK (kreait) to manage users and data in Firestore/Storage without being subject to client-side security rules.

---

## Quick Start

```bash
cp .env.example .env
php artisan key:generate

# Place Firebase service account JSON at web/firebase-service-account.json
# Then set in .env:
#   FIREBASE_CREDENTIALS=firebase-service-account.json
#   FIREBASE_STORAGE_DEFAULT_BUCKET=your-bucket.appspot.com
#   ADMIN_PASSWORD=your-secret-password

php artisan migrate
php artisan serve    # http://localhost:8000/admin/login
```

---

## Key Commands

```bash
php artisan route:list           # All routes
php artisan config:clear         # Clear config cache
php artisan cache:clear
php artisan queue:work --tries=3 # Process bank statement jobs
php artisan test                 # PHPUnit test suite
```

---

## Architecture

**Single-password admin auth** — `ADMIN_PASSWORD` env var; session flag `admin_authenticated` set on login. No database user table — all data comes from Firebase.

**Firebase Admin SDK** — `kreait/laravel-firebase`. Service account JSON bypasses Firestore/Storage security rules. Injected via constructor DI.

**REST API** — Firebase ID token verification via `FirebaseApiAuth` middleware. Rate-limited per Firebase UID (120 req/min) with `throttle:10,1` on uploads.

---

## Routes

### Admin Panel

```
GET  /admin/login                  → login form
POST /admin/login                  → authenticate (throttle: 3/15 min)
POST /admin/logout                 → clear session
GET  /admin/dashboard              → stats (total/active/disabled/new users)
GET  /admin/users                  → paginated user list with search
GET  /admin/users/{uid}            → user detail + Firestore data
POST /admin/users/{uid}/disable
POST /admin/users/{uid}/enable
POST /admin/users/{uid}/reset-password
DELETE /admin/users/{uid}
GET  /admin/policies               → view/manage policy versions
POST /admin/policies/{type}        → bump policy version
```

### Portal (end-user)

```
GET  /portal/login                 → portal login form (throttle: 5/15 min)
POST /portal/login                 → authenticate
GET  /portal/policies/accept       → accept T&C + Privacy Policy
POST /portal/policies/accept       → record acceptance
```

### Legal (public, no auth)

```
GET  /terms                        → Terms & Conditions (v1.0, South African law)
GET  /privacy                      → Privacy Policy (POPIA-compliant, v1.0)
```

### REST API

```
GET  /api/v1/policies/current      → current policy versions (no auth)
POST /api/v1/policies/accept       → record acceptance  [Firebase token]
GET  /api/v1/policies/status       → per-user acceptance status  [Firebase token]
GET  /api/v1/statements            → list upload jobs  [Firebase token]
GET  /api/v1/statements/{id}       → single job status  [Firebase token]
POST /api/v1/statements            → upload PDF (max 10 MB)  [Firebase token + throttle:10/min]
```

### API Documentation

```
GET  /docs                         → Swagger UI (interactive, no auth)
GET  /api-docs/openapi.yaml        → Raw OpenAPI 3.0 spec (served from public/)
```

The spec lives at `public/api-docs/openapi.yaml`. The Swagger UI page at `/docs` loads it from the CDN version of Swagger UI (no build step required). Controllers carry `@OA\*` annotations compatible with `darkaonline/l5-swagger` for annotation-based generation if preferred.

---

## Key Files

| File | Purpose |
|---|---|
| `app/Http/Controllers/Admin/AuthController.php` | Login / logout |
| `app/Http/Controllers/Admin/DashboardController.php` | Stats from Firebase Auth |
| `app/Http/Controllers/Admin/UserController.php` | List, view, disable, enable, reset, delete users |
| `app/Http/Controllers/Admin/PolicyController.php` | View and bump policy versions |
| `app/Http/Controllers/Portal/PolicyController.php` | End-user policy acceptance |
| `app/Http/Controllers/Api/StatementController.php` | Upload and list bank statement jobs |
| `app/Http/Controllers/Api/PolicyController.php` | Policy versions and acceptance via API |
| `app/Http/Middleware/AdminAuth.php` | Session guard for admin routes |
| `app/Http/Middleware/FirebaseApiAuth.php` | ID token verification for API routes |
| `app/Http/Middleware/SecurityHeaders.php` | CSP, HSTS, X-Frame-Options, etc. |
| `app/Http/Middleware/ApiCors.php` | CORS — restricted origin allowlist |
| `app/Http/Middleware/PortalPolicyCheck.php` | Redirects users who haven't accepted current policies |
| `app/Services/PolicyService.php` | Reads/writes policy versions and acceptances in Firestore |
| `app/Services/FirestoreService.php` | Thin wrapper over kreait Firestore client |
| `config/admin.php` | Reads `ADMIN_PASSWORD` from env |
| `resources/views/admin/` | Blade templates (Bootstrap 5) |
| `resources/views/legal/` | Public T&C and Privacy Policy pages |
| `routes/web.php` | All web route definitions |
| `routes/api.php` | All API route definitions |

---

## Security

- **Security headers** on every web response: CSP, HSTS, `X-Frame-Options: SAMEORIGIN`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, `Permissions-Policy`, strips `X-Powered-By` / `Server`
- **Rate limiting**: `throttle:3,15` admin login; `throttle:5,15` portal login; 120 req/min per Firebase UID on API; `throttle:10,1` on statement upload
- **CORS**: origin allowlist (`https://thebudgetbuddy.co.za`, localhost, emulator); no wildcard
- **Session**: `SESSION_ENCRYPT=true`, `SESSION_SECURE_COOKIE=true`, `SESSION_SAME_SITE=strict`
- **bcrypt cost 12**: ~250 ms/hash

---

## Docker

```bash
docker build -t budgetbuddy-web .

docker run -d -p 80:80 \
  -e APP_KEY=base64:... \
  -e ADMIN_PASSWORD=... \
  -e FIREBASE_CREDENTIALS_JSON='{"type":"service_account",...}' \
  -e FIREBASE_STORAGE_DEFAULT_BUCKET=... \
  budgetbuddy-web
```

Multi-stage build: base (php:8.3-fpm-alpine + nginx + supervisor) → deps → builder (Vite assets) → production.

---

## CI/CD

| Job | Trigger | Action |
|---|---|---|
| `test` | Every push / PR | PHPUnit on PHP 8.3 + SQLite |
| `docker` | Push to `main` | Build + push image to GHCR |
| `release` | Tag `v*` | GitHub Release with Docker pull snippet |
