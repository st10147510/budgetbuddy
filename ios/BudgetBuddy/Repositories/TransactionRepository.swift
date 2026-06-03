import Foundation
import SwiftData

final class TransactionRepository {
    private let context: ModelContext
    private let firestore: FirestoreRepository

    init(context: ModelContext, firestore: FirestoreRepository) {
        self.context   = context
        self.firestore = firestore
    }

    // MARK: - Queries

    func all(userId: String) throws -> [BBTransaction] {
        let pred = #Predicate<BBTransaction> { $0.userId == userId }
        let desc = FetchDescriptor<BBTransaction>(predicate: pred,
                                                  sortBy: [SortDescriptor(\.date, order: .reverse)])
        return try context.fetch(desc)
    }

    func forDateRange(userId: String, start: Date, end: Date) throws -> [BBTransaction] {
        let pred = #Predicate<BBTransaction> { $0.userId == userId && $0.date >= start && $0.date <= end }
        let desc = FetchDescriptor<BBTransaction>(predicate: pred,
                                                  sortBy: [SortDescriptor(\.date, order: .reverse)])
        return try context.fetch(desc)
    }

    func recent(userId: String, limit: Int = 5) throws -> [BBTransaction] {
        let pred = #Predicate<BBTransaction> { $0.userId == userId }
        var desc = FetchDescriptor<BBTransaction>(predicate: pred,
                                                  sortBy: [SortDescriptor(\.date, order: .reverse)])
        desc.fetchLimit = limit
        return try context.fetch(desc)
    }

    func byId(_ id: UUID) throws -> BBTransaction? {
        let pred = #Predicate<BBTransaction> { $0.id == id }
        return try context.fetch(FetchDescriptor<BBTransaction>(predicate: pred)).first
    }

    func totalExpense(userId: String, start: Date, end: Date) throws -> Double {
        try forDateRange(userId: userId, start: start, end: end)
            .filter { $0.type == .expense }.reduce(0) { $0 + $1.amount }
    }

    func totalIncome(userId: String, start: Date, end: Date) throws -> Double {
        try forDateRange(userId: userId, start: start, end: end)
            .filter { $0.type == .income }.reduce(0) { $0 + $1.amount }
    }

    func totalExpenseByCategory(userId: String, categoryId: UUID, start: Date, end: Date) throws -> Double {
        try forDateRange(userId: userId, start: start, end: end)
            .filter { $0.type == .expense && $0.categoryId == categoryId }
            .reduce(0) { $0 + $1.amount }
    }

    func countInRange(userId: String, start: Date, end: Date) throws -> Int {
        let pred = #Predicate<BBTransaction> { $0.userId == userId && $0.date >= start && $0.date <= end }
        return try context.fetchCount(FetchDescriptor<BBTransaction>(predicate: pred))
    }

    // MARK: - Mutations

    @discardableResult
    func insert(_ tx: BBTransaction) async throws -> BBTransaction {
        context.insert(tx)
        try context.save()
        Task { await firestore.saveTransaction(userId: tx.userId, tx: tx) }
        return tx
    }

    func update(_ tx: BBTransaction) async throws {
        try context.save()
        Task { await firestore.saveTransaction(userId: tx.userId, tx: tx) }
    }

    func delete(_ tx: BBTransaction) async throws {
        let userId = tx.userId
        let id     = tx.id
        context.delete(tx)
        try context.save()
        Task { await firestore.deleteTransaction(userId: userId, id: id) }
    }
}
