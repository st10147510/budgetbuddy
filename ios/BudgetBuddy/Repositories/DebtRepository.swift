import Foundation
import SwiftData

final class DebtRepository {
    private let context: ModelContext
    private let firestore: FirestoreRepository

    init(context: ModelContext, firestore: FirestoreRepository) {
        self.context   = context
        self.firestore = firestore
    }

    func all(userId: String) throws -> [BBDebt] {
        let pred = #Predicate<BBDebt> { $0.userId == userId }
        return try context.fetch(FetchDescriptor<BBDebt>(predicate: pred,
                                                         sortBy: [SortDescriptor(\.createdAt, order: .reverse)]))
    }

    func active(userId: String) throws -> [BBDebt] {
        let pred = #Predicate<BBDebt> { $0.userId == userId && !$0.isPaidOff }
        return try context.fetch(FetchDescriptor<BBDebt>(predicate: pred))
    }

    func byId(_ id: UUID) throws -> BBDebt? {
        let pred = #Predicate<BBDebt> { $0.id == id }
        return try context.fetch(FetchDescriptor<BBDebt>(predicate: pred)).first
    }

    func paidOffCount(userId: String) throws -> Int {
        let pred = #Predicate<BBDebt> { $0.userId == userId && $0.isPaidOff }
        return try context.fetchCount(FetchDescriptor<BBDebt>(predicate: pred))
    }

    @discardableResult
    func insert(_ debt: BBDebt) async throws -> BBDebt {
        context.insert(debt)
        try context.save()
        Task { await firestore.saveDebt(userId: debt.userId, debt: debt) }
        return debt
    }

    func update(_ debt: BBDebt) async throws {
        try context.save()
        Task { await firestore.saveDebt(userId: debt.userId, debt: debt) }
    }

    func markPaidOff(_ debt: BBDebt) async throws {
        debt.isPaidOff = true
        debt.balance   = 0
        try context.save()
        Task { await firestore.saveDebt(userId: debt.userId, debt: debt) }
    }

    func delete(_ debt: BBDebt) async throws {
        let userId = debt.userId
        let id     = debt.id
        context.delete(debt)
        try context.save()
        Task { await firestore.deleteDebt(userId: userId, id: id) }
    }

    // MARK: - Payoff schedule (mirrors Android DebtRepository.computePayoffSchedule)

    func computePayoffSchedule(debts: [BBDebt], strategy: PayoffStrategy,
                                extraPayment: Double = 0) -> [DebtPayoffMonth] {
        guard !debts.isEmpty else { return [] }

        let sorted = strategy == .snowball
            ? debts.sorted { $0.balance < $1.balance }
            : debts.sorted { $0.interestRate > $1.interestRate }

        var balances = sorted.map { $0.balance }
        var schedule = [DebtPayoffMonth]()
        var month    = 1

        while balances.contains(where: { $0 > 0 }), month <= 360 {
            let freedMinimums   = sorted.indices.filter { balances[$0] <= 0 }.reduce(0.0) { $0 + sorted[$1].minimumPayment }
            let targetIndex     = balances.firstIndex(where: { $0 > 0 }) ?? 0
            let targetBonus     = extraPayment + freedMinimums

            for (i, debt) in sorted.enumerated() {
                guard balances[i] > 0 else { continue }
                let monthlyRate   = debt.interestRate / 100.0 / 12.0
                let interest      = balances[i] * monthlyRate
                let payment       = i == targetIndex ? debt.minimumPayment + targetBonus : debt.minimumPayment
                let actual        = min(payment, balances[i] + interest)
                balances[i]       = max(0, balances[i] + interest - actual)
                schedule.append(DebtPayoffMonth(month: month, debtName: debt.name,
                                                payment: actual, remainingBalance: balances[i]))
            }
            month += 1
        }
        return schedule
    }
}
