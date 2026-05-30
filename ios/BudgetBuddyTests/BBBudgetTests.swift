import XCTest
import SwiftData
@testable import BudgetBuddy

final class BBBudgetTests: XCTestCase {

    private var container: ModelContainer!
    private var context: ModelContext!

    override func setUpWithError() throws {
        let config = ModelConfiguration(isStoredInMemoryOnly: true)
        container  = try ModelContainer(for: BBBudget.self, configurations: config)
        context    = ModelContext(container)
    }

    override func tearDown() {
        container = nil
        context   = nil
    }

    private func makeBudget(limit: Double = 1000, min: Double = 0,
                            month: Int = 6, year: Int = 2024) -> BBBudget {
        BBBudget(userId: "uid", categoryId: UUID(), limitAmount: limit,
                 minAmount: min, month: month, year: year)
    }

    // MARK: - Init

    func test_limit_amount_stored() {
        let b = makeBudget(limit: 2500)
        XCTAssertEqual(b.limitAmount, 2500)
    }

    func test_min_amount_defaults_to_zero() {
        let b = makeBudget()
        XCTAssertEqual(b.minAmount, 0)
    }

    func test_min_amount_stored_when_provided() {
        let b = makeBudget(min: 500)
        XCTAssertEqual(b.minAmount, 500)
    }

    func test_month_stored_as_integer_1_to_12() {
        for m in 1...12 {
            let b = makeBudget(month: m)
            XCTAssertEqual(b.month, m)
        }
    }

    func test_year_stored() {
        let b = makeBudget(year: 2025)
        XCTAssertEqual(b.year, 2025)
    }

    func test_user_id_stored() {
        let b = BBBudget(userId: "abc-xyz", categoryId: UUID(), limitAmount: 100,
                         month: 1, year: 2024)
        XCTAssertEqual(b.userId, "abc-xyz")
    }

    func test_category_id_stored() {
        let catId = UUID()
        let b     = BBBudget(userId: "uid", categoryId: catId, limitAmount: 500,
                             month: 3, year: 2024)
        XCTAssertEqual(b.categoryId, catId)
    }

    func test_id_is_unique() {
        let a = makeBudget()
        let b = makeBudget()
        XCTAssertNotEqual(a.id, b.id)
    }

    func test_created_at_is_recent() {
        let before = Date()
        let b      = makeBudget()
        let after  = Date()
        XCTAssertGreaterThanOrEqual(b.createdAt, before)
        XCTAssertLessThanOrEqual(b.createdAt, after)
    }

    // MARK: - Persistence

    func test_budget_inserts_and_fetches() throws {
        let b = makeBudget(limit: 1500, month: 5, year: 2024)
        context.insert(b)
        try context.save()

        let fetched = try context.fetch(FetchDescriptor<BBBudget>())
        XCTAssertEqual(fetched.count, 1)
        XCTAssertEqual(fetched.first?.limitAmount, 1500)
        XCTAssertEqual(fetched.first?.month, 5)
    }

    func test_multiple_budgets_different_months() throws {
        for m in 1...6 {
            context.insert(makeBudget(month: m))
        }
        try context.save()

        let fetched = try context.fetch(FetchDescriptor<BBBudget>())
        XCTAssertEqual(fetched.count, 6)
    }

    // MARK: - BudgetStatus boundary values (mirrors BudgetStatusTests logic)

    func test_status_boundary_80_percent_is_warning() {
        let limit: Double = 1000
        let spent: Double = 800
        let pct = min(150, Int((spent / limit) * 100))
        XCTAssertEqual(pct, 80)
        // pct >= 80 → warning
        let status: BudgetStatus = pct >= 100 ? .exceeded : pct >= 80 ? .warning : .ok
        XCTAssertEqual(status, .warning)
    }

    func test_status_limit_equal_zero_gives_zero_percent() {
        let limit: Double = 0
        let pct = limit > 0 ? min(150, Int((500 / limit) * 100)) : 0
        XCTAssertEqual(pct, 0)
    }
}
