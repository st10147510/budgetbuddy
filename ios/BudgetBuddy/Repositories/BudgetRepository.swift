import Foundation
import SwiftData

final class BudgetRepository {
    private let context: ModelContext
    private let firestore: FirestoreRepository

    init(context: ModelContext, firestore: FirestoreRepository) {
        self.context   = context
        self.firestore = firestore
    }

    func forMonth(userId: String, month: Int, year: Int) throws -> [BBBudget] {
        let pred = #Predicate<BBBudget> { $0.userId == userId && $0.month == month && $0.year == year }
        return try context.fetch(FetchDescriptor<BBBudget>(predicate: pred))
    }

    func forCategory(userId: String, categoryId: UUID, month: Int, year: Int) throws -> BBBudget? {
        let pred = #Predicate<BBBudget> {
            $0.userId == userId && $0.categoryId == categoryId && $0.month == month && $0.year == year
        }
        return try context.fetch(FetchDescriptor<BBBudget>(predicate: pred)).first
    }

    func byId(_ id: UUID) throws -> BBBudget? {
        let pred = #Predicate<BBBudget> { $0.id == id }
        return try context.fetch(FetchDescriptor<BBBudget>(predicate: pred)).first
    }

    @discardableResult
    func insertOrUpdate(userId: String, categoryId: UUID, limitAmount: Double,
                        minAmount: Double, month: Int, year: Int) async throws -> BBBudget {
        if let existing = try forCategory(userId: userId, categoryId: categoryId, month: month, year: year) {
            existing.limitAmount = limitAmount
            existing.minAmount   = minAmount
            try context.save()
            Task { await firestore.saveBudget(userId: userId, budget: existing) }
            return existing
        }
        let budget = BBBudget(userId: userId, categoryId: categoryId,
                              limitAmount: limitAmount, minAmount: minAmount, month: month, year: year)
        context.insert(budget)
        try context.save()
        Task { await firestore.saveBudget(userId: userId, budget: budget) }
        return budget
    }

    func delete(_ budget: BBBudget) async throws {
        let userId = budget.userId
        let id     = budget.id
        context.delete(budget)
        try context.save()
        Task { await firestore.deleteBudget(userId: userId, id: id) }
    }
}
