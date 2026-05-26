# BudgetBuddy

A personal finance Android app for tracking expenses, managing budgets, monitoring debt, and reaching savings goals with offline-first local storage.

## Video Demo

[PART 2 - Watch the demo on YouTube](https://youtu.be/lt0lGFa9MV8)
[PART 3 - Watch the demo on YouTube](https://youtu.be/XXX)

> The video walks through all app features with a voice-over explaining the implementation.

## APK Download

The APK is available in the [Releases](https://github.com/st10147510/budgetbuddy/releases) section of this repository.

To build it yourself:

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

## Features

- **Expense Tracking** — Log income and expenses with categories, notes, and dates
- **Budgets** — Set per-category spending limits and minimum spending goals; real-time compliance status (On Track / Near Limit / Over Limit / Below Goal)
- **Savings Goals** — Track progress toward financial goals
- **Debt Management** — Monitor debts with payoff schedules and strategy selection (Snowball / Avalanche)
- **Reports** — Monthly line chart of net balance, grouped bar chart of budget vs actual spend per category, and a full transaction list — all filterable by month
- **Gamification** — Earn badges for milestones (first transaction, 7-day streak)
- **Notifications** — Budget alert workers and daily reminders via WorkManager
- **Offline-first** — Room database with write-through sync to Firestore on every save/update/delete
- **Cloud Auth** — Firebase Authentication (email/password) with friendly error messages and password reset

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin 1.9.23, JVM 17 |
| UI | Fragments, ViewBinding, Navigation Component |
| Architecture | Single-Activity MVVM, StateFlow, Hilt DI |
| Local DB | Room 2.6.1 (`budget_buddy.db`) |
| Auth | Firebase Authentication (email/password) |
| Cloud DB | Firebase Firestore (write-through sync) |
| Charts | MPAndroidChart 3.1.0 |
| Background | WorkManager 2.9.1 |
| Preferences | DataStore 1.1.1 |
| Testing | JUnit 4, Mockito-Kotlin, Turbine, Hilt Test |
| Min SDK | 26 (Android 8.0) — Target SDK 35 |

## Project Structure

```text
app/src/main/java/com/budgetbuddy/
├── data/
│   ├── local/
│   │   ├── BudgetBuddyDatabase.kt   # Room DB, seeds 10 default categories
│   │   ├── dao/                     # TransactionDao, BudgetDao, GoalDao, DebtDao, BadgeDao, ...
│   │   ├── entities/                # Room entities (Transaction, Budget, Goal, Debt, Badge, ...)
│   │   └── SessionManager.kt
│   └── repository/                  # AuthRepository, TransactionRepository, BudgetRepository, ...
├── di/
│   ├── AppModule.kt                 # FirebaseAuth, DataStore
│   └── DatabaseModule.kt            # Room DB + all DAOs
├── ui/
│   ├── MainActivity.kt
│   ├── auth/                        # WelcomeFragment, SignInFragment, SignUpFragment, ForgotPasswordFragment
│   ├── home/                        # HomeFragment, HomeViewModel
│   ├── expense/                     # AddExpenseFragment, TransactionListFragment, ExpenseViewModel
│   ├── budget/                      # BudgetFragment, BudgetViewModel
│   ├── goals/                       # GoalsFragment, GoalsViewModel
│   ├── debt/                        # DebtFragment, DebtViewModel
│   ├── reports/                     # ReportsFragment, ReportsViewModel
│   ├── category/                    # CategoriesFragment, CategoryViewModel
│   ├── gamification/                # BadgesFragment, BadgesViewModel
│   ├── profile/                     # ProfileFragment
│   └── notifications/               # BudgetAlertWorker, DailyReminderWorker
└── util/
    ├── Converters.kt                # Room type converters for enums
    ├── DateUtils.kt
    └── PasswordUtils.kt
```

## Architecture

```text
Fragment → ViewModel (StateFlow) → Repository → Room DAO (Flow)
```

- Fragments observe `StateFlow` with `repeatOnLifecycle(STARTED)`
- ViewModels are `@HiltViewModel` injected — never reference DAOs directly
- Aggregate queries (monthly totals, category spend) live in `TransactionDao` — never computed in-memory
- `BadgeRepository.checkAndAwardBadges()` is called after every transaction save (idempotent)

## Custom Features

### 1. Budget Minimum Spending Goals

Beyond a standard spending *limit*, each budget can also carry a *minimum spending goal* — useful for categories like Savings or Education where underspending is just as problematic as overspending.

**How it works:**

When creating a budget (bottom sheet on the Budget screen), two amount fields are shown:

| Field | Purpose |
|---|---|
| Minimum spending goal *(optional)* | Alert when monthly spend falls below this amount |
| Budget limit | Alert when monthly spend approaches or exceeds this amount |

Each budget card in the list shows a compliance chip whose colour and label reflect the current status:

| Status | Colour | Condition |
|---|---|---|
| On Track | Green | spend ≥ min goal and < 80 % of limit |
| Near Limit | Amber | spend ≥ 80 % of limit |
| Over Limit | Red | spend ≥ limit |
| Below Goal | Blue | spend < min goal (and not already Near Limit / Over Limit) |

When `minAmount > 0`, the card also shows a "Min goal: R X.XX" label beneath the category name.

**Key files:**

- `BudgetEntity.kt` — `minAmount` field (default 0.0)
- `BudgetViewModel.kt` — `BudgetStatus` enum with `UNDER_MIN`; status computed in `loadBudgets()`
- `dialog_add_budget.xml` — min amount `TextInputLayout` with blue stroke and helper text
- `item_budget.xml` + `BudgetAdapter.kt` — compliance chip and min goal label
- `FirestoreRepository.kt` — `minAmount` included in Firestore document on every save

---

### 2. Category Budget vs Spend Bar Chart (Reports)

The Reports screen includes a grouped bar chart that visually compares actual spending against the budget limit for every category that has a budget set in the selected month.

**What the chart shows:**

- **Teal bar (Limit)** — the budget ceiling for the category; extends to at least the spent amount so the chart never clips
- **Coloured bar (Spent)** — actual spend, coloured by the same compliance system as the budget list (green / amber / red / blue)
- **X-axis labels** — category icon + name; if a minimum spending goal is set, the label also shows `≥R<amount>` as a reminder
- **Y-axis** — formatted as `R<amount>`; up to 6 categories displayed (sorted by spend descending)

The chart is reactive: it updates whenever transactions or budgets change, and collapses to an empty-state message ("Set budgets to see spending comparison") when no budgets exist for the selected month.

**Key files:**

- `ReportsViewModel.kt` — `CategoryBudgetBar` data class; `BudgetRepository` injected; `loadSelectedMonth()` uses `combine()` on transaction + budget flows to build `categoryBudgetBars`
- `fragment_reports.xml` — `BarChart` view (200 dp) with legend row and empty state
- `ReportsFragment.kt` — `setupBudgetBarChart()` / `updateBudgetBarChart()` using MPAndroidChart grouped `BarData` with two `BarDataSet`s

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- A Firebase project with Email/Password authentication enabled
- `google-services.json` placed in `app/`

### Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Clean build
./gradlew clean assembleDebug

# Install to connected device
./gradlew installDebug
```

### Running on an Emulator

**Via Android Studio:**

1. Open the project in Android Studio.
2. Open **Device Manager** (View > Tool Windows > Device Manager).
3. Click **Create Device**, choose a phone (e.g. Pixel 6), select a system image (API 26+), and finish.
4. Click the green **Run** button (or `Shift+F10`) with the emulator selected as the target.

**Via command line (AVD Manager):**

```bash
# List available emulators
emulator -list-avds

# Start a specific emulator (replace Pixel_6_API_35 with your AVD name)
emulator -avd Pixel_6_API_35

# Build and install once the emulator is booted
./gradlew installDebug
```

The app requires API 26+. Make sure your AVD system image matches that minimum.

### Running Tests

```bash
# All unit tests
./gradlew test

# Debug variant only
./gradlew testDebugUnitTest

# Single test class
./gradlew test --tests "com.budgetbuddy.ui.auth.AuthViewModelTest"

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

Test coverage reports output to `app/build/reports/coverage/`.

### Code Quality

```bash
./gradlew lint
```

## Navigation

Two navigation regions in `nav_graph.xml`:

1. **Auth flow** — `welcomeFragment` start; popped entirely from back stack on successful login
2. **Main flow** — `homeFragment` start destination; bottom nav drives `budgetFragment`, `reportsFragment`, `goalsFragment`, `profileFragment`

`MainActivity` hides the bottom nav bar for auth screens and detail screens (add expense, badges, categories, debt, transaction list).

## Database

Room database `budget_buddy.db` (version 4).

Enums (`TransactionType`, `BadgeType`, `NotificationType`, `PayoffStrategy`) stored as strings via `Converters.kt`.

Ten default categories are seeded in `BudgetBuddyDatabase.Callback.onCreate`.

The database is configured with `fallbackToDestructiveMigration()` — schema changes during development wipe and reseed the local DB. Cloud data in Firestore is unaffected.

## License

This project is for personal/educational use.
