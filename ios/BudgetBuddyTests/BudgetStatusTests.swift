import XCTest
@testable import BudgetBuddy

// BudgetWithSpend.status is computed inline in BudgetViewModel.load().
// These tests replicate that logic in isolation so we can verify the
// threshold boundaries without needing a live ModelContext.

final class BudgetStatusTests: XCTestCase {

    private func status(spent: Double, limit: Double, min: Double = 0) -> BudgetStatus {
        let pct: Int = limit > 0 ? Swift.min(150, Int((spent / limit) * 100)) : 0
        if pct >= 100 { return .exceeded }
        if pct >= 80  { return .warning  }
        if min > 0 && spent < min { return .underMin }
        return .ok
    }

    // MARK: - Exceeded (≥ 100 %)

    func test_100_percent_spend_is_exceeded() {
        XCTAssertEqual(status(spent: 1000, limit: 1000), .exceeded)
    }

    func test_over_100_percent_spend_is_exceeded() {
        XCTAssertEqual(status(spent: 1500, limit: 1000), .exceeded)
    }

    func test_capped_at_150_percent_is_exceeded() {
        // Even if spent is 10× the limit, status is exceeded (not some other state)
        XCTAssertEqual(status(spent: 10_000, limit: 1000), .exceeded)
    }

    // MARK: - Warning (80 – 99 %)

    func test_80_percent_spend_is_warning() {
        XCTAssertEqual(status(spent: 800, limit: 1000), .warning)
    }

    func test_99_percent_spend_is_warning() {
        XCTAssertEqual(status(spent: 999, limit: 1000), .warning)
    }

    func test_79_percent_spend_is_ok_not_warning() {
        XCTAssertEqual(status(spent: 790, limit: 1000), .ok)
    }

    // MARK: - Under minimum

    func test_under_min_returns_underMin_status() {
        // spent < min, within limit → underMin
        XCTAssertEqual(status(spent: 50, limit: 1000, min: 200), .underMin)
    }

    func test_no_min_set_does_not_return_underMin() {
        XCTAssertEqual(status(spent: 0, limit: 1000, min: 0), .ok)
    }

    func test_exactly_at_min_is_ok_not_underMin() {
        XCTAssertEqual(status(spent: 200, limit: 1000, min: 200), .ok)
    }

    func test_underMin_does_not_trigger_when_warning_threshold_reached() {
        // spent ≥ 80% limit: warning takes priority over underMin
        XCTAssertEqual(status(spent: 800, limit: 1000, min: 900), .warning)
    }

    // MARK: - OK

    func test_zero_spend_is_ok() {
        XCTAssertEqual(status(spent: 0, limit: 1000), .ok)
    }

    func test_50_percent_spend_is_ok() {
        XCTAssertEqual(status(spent: 500, limit: 1000), .ok)
    }

    func test_zero_limit_gives_ok_status() {
        // When limit == 0 pct is forced to 0, so status is ok
        XCTAssertEqual(status(spent: 9999, limit: 0), .ok)
    }

    // MARK: - progressPercent capping

    func test_progress_percent_capped_at_150() {
        let limit: Double = 1000
        let spent: Double = 5000
        let pct = Swift.min(150, Int((spent / limit) * 100))
        XCTAssertEqual(pct, 150)
    }
}
