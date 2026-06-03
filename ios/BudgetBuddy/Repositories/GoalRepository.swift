import Foundation
import SwiftData

final class GoalRepository {
    private let context: ModelContext
    private let firestore: FirestoreRepository

    init(context: ModelContext, firestore: FirestoreRepository) {
        self.context   = context
        self.firestore = firestore
    }

    func all(userId: String) throws -> [BBGoal] {
        let pred = #Predicate<BBGoal> { $0.userId == userId }
        return try context.fetch(FetchDescriptor<BBGoal>(predicate: pred,
                                                         sortBy: [SortDescriptor(\.createdAt, order: .reverse)]))
    }

    func active(userId: String) throws -> [BBGoal] {
        let pred = #Predicate<BBGoal> { $0.userId == userId && !$0.isCompleted }
        return try context.fetch(FetchDescriptor<BBGoal>(predicate: pred))
    }

    func byId(_ id: UUID) throws -> BBGoal? {
        let pred = #Predicate<BBGoal> { $0.id == id }
        return try context.fetch(FetchDescriptor<BBGoal>(predicate: pred)).first
    }

    func completedCount(userId: String) throws -> Int {
        let pred = #Predicate<BBGoal> { $0.userId == userId && $0.isCompleted }
        return try context.fetchCount(FetchDescriptor<BBGoal>(predicate: pred))
    }

    @discardableResult
    func insert(_ goal: BBGoal) async throws -> BBGoal {
        context.insert(goal)
        try context.save()
        Task { await firestore.saveGoal(userId: goal.userId, goal: goal) }
        return goal
    }

    func update(_ goal: BBGoal) async throws {
        try context.save()
        Task { await firestore.saveGoal(userId: goal.userId, goal: goal) }
    }

    func delete(_ goal: BBGoal) async throws {
        let userId = goal.userId
        let id     = goal.id
        context.delete(goal)
        try context.save()
        Task { await firestore.deleteGoal(userId: userId, id: id) }
    }
}
