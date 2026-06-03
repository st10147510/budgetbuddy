import Foundation
import SwiftData

final class SyncRepository {
    private let context: ModelContext
    private let firestore: FirestoreRepository
    private let transactionRepo: TransactionRepository
    private let budgetRepo: BudgetRepository
    private let goalRepo: GoalRepository
    private let debtRepo: DebtRepository
    private let categoryRepo: CategoryRepository
    private let badgeRepo: BadgeRepository

    init(context: ModelContext, firestore: FirestoreRepository,
         transactionRepo: TransactionRepository, budgetRepo: BudgetRepository,
         goalRepo: GoalRepository, debtRepo: DebtRepository,
         categoryRepo: CategoryRepository, badgeRepo: BadgeRepository) {
        self.context         = context
        self.firestore       = firestore
        self.transactionRepo = transactionRepo
        self.budgetRepo      = budgetRepo
        self.goalRepo        = goalRepo
        self.debtRepo        = debtRepo
        self.categoryRepo    = categoryRepo
        self.badgeRepo       = badgeRepo
    }

    func syncFromFirestore(userId: String) async {
        await syncTransactions(userId: userId)
        await syncBudgets(userId: userId)
        await syncGoals(userId: userId)
        await syncDebts(userId: userId)
        await syncBadges(userId: userId)
    }

    func syncToFirestore(userId: String) async {
        if let txs = try? transactionRepo.all(userId: userId) {
            for tx in txs { await firestore.saveTransaction(userId: userId, tx: tx) }
        }
        let cal = Calendar.current
        let month = cal.component(.month, from: .now)
        let year  = cal.component(.year,  from: .now)
        if let budgets = try? budgetRepo.forMonth(userId: userId, month: month, year: year) {
            for b in budgets { await firestore.saveBudget(userId: userId, budget: b) }
        }
        if let goals = try? goalRepo.all(userId: userId) {
            for g in goals { await firestore.saveGoal(userId: userId, goal: g) }
        }
        if let debts = try? debtRepo.all(userId: userId) {
            for d in debts { await firestore.saveDebt(userId: userId, debt: d) }
        }
        if let badges = try? badgeRepo.all(userId: userId) {
            for b in badges { await firestore.saveBadge(userId: userId, badge: b) }
        }
    }

    // MARK: - Pull from Firestore into local DB

    private func syncTransactions(userId: String) async {
        let remote = await firestore.fetchAllTransactions(userId: userId)
        for data in remote {
            guard let idStr = data["id"] as? String,
                  let id = UUID(uuidString: idStr),
                  (try? transactionRepo.byId(id)) == nil else { continue }
            let tx = BBTransaction(
                userId:   userId,
                amount:   data["amount"] as? Double ?? 0,
                categoryId: (data["categoryId"] as? String).flatMap(UUID.init),
                date:     (data["date"] as? Timestamp)?.dateValue() ?? .now,
                notes:    data["notes"] as? String,
                type:     TransactionType(rawValue: data["type"] as? String ?? "") ?? .expense
            )
            tx.id = id
            context.insert(tx)
        }
        try? context.save()
    }

    private func syncBudgets(userId: String) async {
        let remote = await firestore.fetchAllBudgets(userId: userId)
        for data in remote {
            guard let idStr = data["id"] as? String,
                  let id = UUID(uuidString: idStr),
                  let catStr = data["categoryId"] as? String,
                  let catId = UUID(uuidString: catStr),
                  (try? budgetRepo.byId(id)) == nil else { continue }
            let budget = BBBudget(
                userId:      userId,
                categoryId:  catId,
                limitAmount: data["limitAmount"] as? Double ?? 0,
                minAmount:   data["minAmount"]   as? Double ?? 0,
                month:       data["month"]        as? Int ?? 1,
                year:        data["year"]         as? Int ?? 2025
            )
            budget.id = id
            context.insert(budget)
        }
        try? context.save()
    }

    private func syncGoals(userId: String) async {
        let remote = await firestore.fetchAllGoals(userId: userId)
        for data in remote {
            guard let idStr = data["id"] as? String,
                  let id = UUID(uuidString: idStr),
                  (try? goalRepo.byId(id)) == nil else { continue }
            let goal = BBGoal(
                userId:       userId,
                name:         data["name"] as? String ?? "",
                targetAmount: data["targetAmount"] as? Double ?? 0,
                savedAmount:  data["savedAmount"]  as? Double ?? 0,
                targetDate:   (data["targetDate"] as? Timestamp)?.dateValue()
            )
            goal.id          = id
            goal.isCompleted = data["isCompleted"] as? Bool ?? false
            context.insert(goal)
        }
        try? context.save()
    }

    private func syncDebts(userId: String) async {
        let remote = await firestore.fetchAllDebts(userId: userId)
        for data in remote {
            guard let idStr = data["id"] as? String,
                  let id = UUID(uuidString: idStr),
                  (try? debtRepo.byId(id)) == nil else { continue }
            let debt = BBDebt(
                userId:         userId,
                name:           data["name"]           as? String ?? "",
                balance:        data["balance"]         as? Double ?? 0,
                interestRate:   data["interestRate"]    as? Double ?? 0,
                minimumPayment: data["minimumPayment"]  as? Double ?? 0
            )
            debt.id              = id
            debt.originalBalance = data["originalBalance"] as? Double ?? debt.balance
            debt.isPaidOff       = data["isPaidOff"]       as? Bool   ?? false
            context.insert(debt)
        }
        try? context.save()
    }

    private func syncBadges(userId: String) async {
        let remote = await firestore.fetchAllBadges(userId: userId)
        for data in remote {
            guard let typeStr = data["badgeType"] as? String,
                  let type = BadgeType(rawValue: typeStr) else { continue }
            let pred = #Predicate<BBBadge> { $0.userId == userId && $0.badgeType == type }
            guard (try? context.fetchCount(FetchDescriptor<BBBadge>(predicate: pred))) == 0 else { continue }
            let badge = BBBadge(userId: userId, badgeType: type)
            if let ts = data["earnedAt"] as? Timestamp { badge.earnedAt = ts.dateValue() }
            context.insert(badge)
        }
        try? context.save()
    }
}
