import XCTest
import SwiftData
@testable import BudgetBuddy

final class BBTransactionTests: XCTestCase {

    private var container: ModelContainer!
    private var context: ModelContext!

    override func setUpWithError() throws {
        let config = ModelConfiguration(isStoredInMemoryOnly: true)
        container  = try ModelContainer(for: BBTransaction.self, configurations: config)
        context    = ModelContext(container)
    }

    override func tearDown() {
        container = nil
        context   = nil
    }

    // MARK: - TransactionType enum

    func test_expense_raw_value() {
        XCTAssertEqual(TransactionType.expense.rawValue, "EXPENSE")
    }

    func test_income_raw_value() {
        XCTAssertEqual(TransactionType.income.rawValue, "INCOME")
    }

    func test_transaction_type_codable_expense() throws {
        let encoded = try JSONEncoder().encode(TransactionType.expense)
        let decoded = try JSONDecoder().decode(TransactionType.self, from: encoded)
        XCTAssertEqual(decoded, .expense)
    }

    func test_transaction_type_codable_income() throws {
        let encoded = try JSONEncoder().encode(TransactionType.income)
        let decoded = try JSONDecoder().decode(TransactionType.self, from: encoded)
        XCTAssertEqual(decoded, .income)
    }

    func test_transaction_type_from_valid_raw_value() {
        XCTAssertEqual(TransactionType(rawValue: "EXPENSE"), .expense)
        XCTAssertEqual(TransactionType(rawValue: "INCOME"),  .income)
    }

    func test_transaction_type_from_invalid_raw_value_is_nil() {
        XCTAssertNil(TransactionType(rawValue: "TRANSFER"))
        XCTAssertNil(TransactionType(rawValue: "expense"))   // lowercase
    }

    // MARK: - BBTransaction init defaults

    func test_default_type_is_expense() {
        let tx = BBTransaction(userId: "uid", amount: 100)
        XCTAssertEqual(tx.type, .expense)
    }

    func test_default_category_id_is_nil() {
        let tx = BBTransaction(userId: "uid", amount: 100)
        XCTAssertNil(tx.categoryId)
    }

    func test_default_notes_is_nil() {
        let tx = BBTransaction(userId: "uid", amount: 100)
        XCTAssertNil(tx.notes)
    }

    func test_default_receipt_image_path_is_nil() {
        let tx = BBTransaction(userId: "uid", amount: 100)
        XCTAssertNil(tx.receiptImagePath)
    }

    func test_id_is_unique_per_instance() {
        let a = BBTransaction(userId: "uid", amount: 50)
        let b = BBTransaction(userId: "uid", amount: 50)
        XCTAssertNotEqual(a.id, b.id)
    }

    func test_amount_stored_correctly() {
        let tx = BBTransaction(userId: "uid", amount: 1234.56)
        XCTAssertEqual(tx.amount, 1234.56)
    }

    func test_income_type_stored() {
        let tx = BBTransaction(userId: "uid", amount: 5000, type: .income)
        XCTAssertEqual(tx.type, .income)
    }

    func test_notes_stored() {
        let tx = BBTransaction(userId: "uid", amount: 100, notes: "Lunch")
        XCTAssertEqual(tx.notes, "Lunch")
    }

    func test_category_id_stored() {
        let catId = UUID()
        let tx    = BBTransaction(userId: "uid", amount: 100, categoryId: catId)
        XCTAssertEqual(tx.categoryId, catId)
    }

    func test_user_id_stored() {
        let tx = BBTransaction(userId: "user-abc", amount: 100)
        XCTAssertEqual(tx.userId, "user-abc")
    }

    func test_created_at_is_recent() {
        let before = Date()
        let tx     = BBTransaction(userId: "uid", amount: 100)
        let after  = Date()
        XCTAssertGreaterThanOrEqual(tx.createdAt, before)
        XCTAssertLessThanOrEqual(tx.createdAt, after)
    }

    // MARK: - Model context persistence

    func test_insert_and_fetch() throws {
        let tx = BBTransaction(userId: "uid", amount: 42.00, type: .expense)
        context.insert(tx)
        try context.save()

        let fetched = try context.fetch(FetchDescriptor<BBTransaction>())
        XCTAssertEqual(fetched.count, 1)
        XCTAssertEqual(fetched.first?.amount, 42.00)
    }

    func test_multiple_transactions_inserted() throws {
        for i in 1...5 {
            context.insert(BBTransaction(userId: "uid", amount: Double(i) * 100))
        }
        try context.save()

        let fetched = try context.fetch(FetchDescriptor<BBTransaction>())
        XCTAssertEqual(fetched.count, 5)
    }
}
