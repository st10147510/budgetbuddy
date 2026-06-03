import XCTest
import SwiftData
@testable import BudgetBuddy

// BBGoal.progressPercent is a computed property on a @Model class.
// We need a ModelContext to instantiate it even though the property
// is purely arithmetic.

final class BBGoalTests: XCTestCase {

    private var container: ModelContainer!
    private var context: ModelContext!

    override func setUpWithError() throws {
        let config = ModelConfiguration(isStoredInMemoryOnly: true)
        container  = try ModelContainer(for: BBGoal.self, configurations: config)
        context    = ModelContext(container)
    }

    override func tearDown() {
        container = nil
        context   = nil
    }

    private func makeGoal(target: Double, saved: Double) -> BBGoal {
        let goal = BBGoal(userId: "uid", name: "Test", targetAmount: target,
                          savedAmount: saved)
        context.insert(goal)
        return goal
    }

    // MARK: - progressPercent

    func test_zero_saved_is_0_percent() {
        XCTAssertEqual(makeGoal(target: 1000, saved: 0).progressPercent, 0)
    }

    func test_half_saved_is_50_percent() {
        XCTAssertEqual(makeGoal(target: 1000, saved: 500).progressPercent, 50)
    }

    func test_full_amount_saved_is_100_percent() {
        XCTAssertEqual(makeGoal(target: 1000, saved: 1000).progressPercent, 100)
    }

    func test_over_target_is_capped_at_100() {
        XCTAssertEqual(makeGoal(target: 1000, saved: 2000).progressPercent, 100)
    }

    func test_zero_target_returns_0() {
        XCTAssertEqual(makeGoal(target: 0, saved: 500).progressPercent, 0)
    }

    func test_1_percent_boundary() {
        // saved = 10, target = 1000 → 1%
        XCTAssertEqual(makeGoal(target: 1000, saved: 10).progressPercent, 1)
    }

    func test_99_percent_boundary() {
        XCTAssertEqual(makeGoal(target: 1000, saved: 999).progressPercent, 99)
    }

    // MARK: - isCompleted default

    func test_is_completed_defaults_to_false() {
        XCTAssertFalse(makeGoal(target: 500, saved: 0).isCompleted)
    }

    // MARK: - PayoffStrategy labels

    func test_snowball_label_mentions_smallest_balance() {
        XCTAssertTrue(PayoffStrategy.snowball.label.lowercased().contains("smallest"))
    }

    func test_avalanche_label_mentions_highest_interest() {
        XCTAssertTrue(PayoffStrategy.avalanche.label.lowercased().contains("highest"))
    }

    func test_payoff_strategy_all_cases() {
        XCTAssertEqual(PayoffStrategy.allCases.count, 2)
    }
}
