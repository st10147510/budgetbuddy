# BudgetBuddy

A personal finance Android app for tracking expenses, managing budgets, monitoring debt, and reaching savings goals with offline-first local storage.

## Video Demo

[Watch the demo on YouTube](https://youtu.be/lt0lGFa9MV8)

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
- **Budgets** — Set spending limits per category with real-time status (OK / WARNING / EXCEEDED)
- **Savings Goals** — Track progress toward financial goals
- **Debt Management** — Monitor debts with payoff schedules and strategy selection
- **Reports** — Monthly spending charts and category breakdowns via MPAndroidChart
- **Gamification** — Earn badges for milestones (first transaction, 7-day streak)
- **Notifications** — Budget alert workers and daily reminders via WorkManager
- **Offline-first** — All data persisted locally with Room

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin 1.9.23, JVM 17 |
| UI | Fragments, ViewBinding, Navigation Component |
| Architecture | Single-Activity MVVM, StateFlow, Hilt DI |
| Local DB | Room 2.6.1 (`budget_buddy.db`) |
| Auth | Authentication (email/password) |
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

Room database `budget_buddy.db` (version 1).

Enums (`TransactionType`, `BadgeType`, `NotificationType`, `PayoffStrategy`) stored as strings via `Converters.kt`.

Ten default categories are seeded in `BudgetBuddyDatabase.Callback.onCreate`.

## License

This project is for personal/educational use.
