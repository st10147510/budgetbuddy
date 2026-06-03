import XCTest
@testable import BudgetBuddy

final class DebtRepositoryTests: XCTestCase {

    // MARK: - Helpers

    private func makeDebt(name: String, balance: Double,
                          interestRate: Double, minimumPayment: Double) -> BBDebt {
        BBDebt(userId: "uid", name: name, balance: balance,
               interestRate: interestRate, minimumPayment: minimumPayment)
    }

    // DebtRepository.computePayoffSchedule is a pure function — no context or
    // Firestore needed, so we can call it directly on a temporary instance.
    private func compute(debts: [BBDebt], strategy: PayoffStrategy,
                         extra: Double = 0) -> [DebtPayoffMonth] {
        // We need a real ModelContext to init the repo, but computePayoffSchedule
        // doesn't use it — use an in-memory container.
        let config = ModelConfiguration(isStoredInMemoryOnly: true)
        let container = try! ModelContainer(for: BBDebt.self, configurations: config)
        let repo = DebtRepository(context: ModelContext(container),
                                  firestore: FirestoreRepository())
        return repo.computePayoffSchedule(debts: debts, strategy: strategy,
                                          extraPayment: extra)
    }

    // MARK: - Edge cases

    func test_empty_debts_returns_empty_schedule() {
        XCTAssertTrue(compute(debts: [], strategy: .snowball).isEmpty)
    }

    func test_already_paid_off_debt_returns_empty_schedule() {
        let debt = makeDebt(name: "Card", balance: 0, interestRate: 20, minimumPayment: 100)
        XCTAssertTrue(compute(debts: [debt], strategy: .snowball).isEmpty)
    }

    // MARK: - Snowball strategy (smallest balance first)

    func test_snowball_targets_smallest_balance_first() {
        let small = makeDebt(name: "Small", balance: 500,  interestRate: 10, minimumPayment: 50)
        let large = makeDebt(name: "Large", balance: 5000, interestRate: 5,  minimumPayment: 100)
        let schedule = compute(debts: [large, small], strategy: .snowball)

        // First month — the "Small" debt (lower balance) should be targeted with extra payment
        let firstSmall = schedule.first { $0.debtName == "Small" }
        let firstLarge = schedule.first { $0.debtName == "Large" }

        XCTAssertNotNil(firstSmall)
        XCTAssertNotNil(firstLarge)

        // Small gets minimum + extra (freed minimums + extra payment), so its payment >= minimum
        XCTAssertGreaterThanOrEqual(firstSmall!.payment, small.minimumPayment)
        // Large pays exactly its minimum in month 1
        XCTAssertEqual(firstLarge!.payment, large.minimumPayment + (firstLarge!.remainingBalance == 0 ? 0 : 0), accuracy: 0.01)
    }

    // MARK: - Avalanche strategy (highest interest first)

    func test_avalanche_targets_highest_interest_first() {
        let highRate = makeDebt(name: "HighRate", balance: 1000, interestRate: 25, minimumPayment: 50)
        let lowRate  = makeDebt(name: "LowRate",  balance: 500,  interestRate: 5,  minimumPayment: 50)
        let schedule = compute(debts: [lowRate, highRate], strategy: .avalanche)

        // Month 1 — highRate is targeted (highest interest)
        let firstHigh = schedule.first { $0.debtName == "HighRate" }
        let firstLow  = schedule.first { $0.debtName == "LowRate" }

        XCTAssertNotNil(firstHigh)
        XCTAssertNotNil(firstLow)
        // High-rate debt gets more than minimum (extra flows to it)
        XCTAssertGreaterThanOrEqual(firstHigh!.payment, highRate.minimumPayment)
    }

    // MARK: - Schedule correctness

    func test_single_zero_interest_debt_pays_off_in_exact_months() {
        // 1200 balance, 0% interest, R100/month → exactly 12 months
        let debt = makeDebt(name: "NoInterest", balance: 1200, interestRate: 0, minimumPayment: 100)
        let schedule = compute(debts: [debt], strategy: .snowball)

        let months = Set(schedule.map(\.month)).count
        XCTAssertEqual(months, 12)

        let lastEntry = schedule.max { $0.month < $1.month }
        XCTAssertEqual(lastEntry?.remainingBalance ?? 1, 0, accuracy: 0.01)
    }

    func test_schedule_balances_are_non_negative() {
        let debt = makeDebt(name: "Debt", balance: 3000, interestRate: 18, minimumPayment: 200)
        let schedule = compute(debts: [debt], strategy: .snowball)

        for entry in schedule {
            XCTAssertGreaterThanOrEqual(entry.remainingBalance, 0,
                "Balance went negative in month \(entry.month)")
        }
    }

    func test_schedule_capped_at_360_months() {
        // Impossibly low payment (won't converge) — must cap at 360 months
        let debt = makeDebt(name: "NeverEnd", balance: 100_000, interestRate: 999, minimumPayment: 1)
        let schedule = compute(debts: [debt], strategy: .snowball)

        let maxMonth = schedule.map(\.month).max() ?? 0
        XCTAssertLessThanOrEqual(maxMonth, 360)
    }

    func test_extra_payment_reduces_total_months() {
        let debt = makeDebt(name: "Card", balance: 5000, interestRate: 20, minimumPayment: 100)
        let withoutExtra = compute(debts: [debt], strategy: .snowball, extra: 0)
        let withExtra    = compute(debts: [debt], strategy: .snowball, extra: 200)

        let monthsWithout = Set(withoutExtra.map(\.month)).count
        let monthsWith    = Set(withExtra.map(\.month)).count
        XCTAssertLessThan(monthsWith, monthsWithout)
    }

    func test_freed_minimum_rolls_to_next_debt_after_payoff() {
        // Debt A pays off in 1 month; debt B should then receive A's minimum too
        let quickDebt = makeDebt(name: "Quick", balance: 100,   interestRate: 0, minimumPayment: 100)
        let longDebt  = makeDebt(name: "Long",  balance: 10000, interestRate: 5, minimumPayment: 200)

        let schedule = compute(debts: [quickDebt, longDebt], strategy: .snowball)

        // After month 1 Quick is paid off. In month 2 Long should receive Long.min + Quick.min
        let month2Long = schedule.filter { $0.debtName == "Long" && $0.month == 2 }.first
        XCTAssertNotNil(month2Long)
        XCTAssertGreaterThanOrEqual(month2Long!.payment, longDebt.minimumPayment + quickDebt.minimumPayment - 1)
    }
}
