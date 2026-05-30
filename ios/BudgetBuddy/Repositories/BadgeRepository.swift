import Foundation
import SwiftData
import UserNotifications

final class BadgeRepository {
    private let context: ModelContext
    private let firestore: FirestoreRepository
    private let transactionRepo: TransactionRepository
    private let budgetRepo: BudgetRepository
    private let goalRepo: GoalRepository
    private let debtRepo: DebtRepository

    init(context: ModelContext, firestore: FirestoreRepository,
         transactionRepo: TransactionRepository, budgetRepo: BudgetRepository,
         goalRepo: GoalRepository, debtRepo: DebtRepository) {
        self.context         = context
        self.firestore       = firestore
        self.transactionRepo = transactionRepo
        self.budgetRepo      = budgetRepo
        self.goalRepo        = goalRepo
        self.debtRepo        = debtRepo
    }

    func all(userId: String) throws -> [BBBadge] {
        let pred = #Predicate<BBBadge> { $0.userId == userId }
        return try context.fetch(FetchDescriptor<BBBadge>(predicate: pred,
                                                          sortBy: [SortDescriptor(\.earnedAt, order: .reverse)]))
    }

    func checkAndAwardBadges(userId: String) async {
        await checkFirstStep(userId: userId)
        await checkLoggingStreak(userId: userId)
        await checkPerfectMonth(userId: userId)
        await checkBudgetMaster(userId: userId)
        await checkDebtSlayer(userId: userId)
        await checkGoalGetter(userId: userId)
        await checkThriftyChamp(userId: userId)
    }

    // MARK: - Private checks

    private func awardBadge(userId: String, type: BadgeType) async {
        let pred = #Predicate<BBBadge> { $0.userId == userId && $0.badgeType == type }
        guard (try? context.fetchCount(FetchDescriptor<BBBadge>(predicate: pred))) == 0 else { return }
        let badge = BBBadge(userId: userId, badgeType: type)
        context.insert(badge)
        try? context.save()
        Task { await firestore.saveBadge(userId: userId, badge: badge) }
        postNotification(type: type)
    }

    private func checkFirstStep(userId: String) async {
        let count = (try? transactionRepo.countInRange(userId: userId,
                                                       start: .distantPast, end: .now)) ?? 0
        if count >= 1 { await awardBadge(userId: userId, type: .firstStep) }
    }

    private func checkLoggingStreak(userId: String) async {
        var streak = 0
        for i in 0..<7 {
            let day   = Calendar.current.date(byAdding: .day, value: -i, to: Date())!
            let start = DateUtils.startOfDay(day)
            let end   = DateUtils.endOfDay(day)
            let count = (try? transactionRepo.countInRange(userId: userId, start: start, end: end)) ?? 0
            if count > 0 { streak += 1 } else { break }
        }
        if streak >= 7 { await awardBadge(userId: userId, type: .dailyTracker) }
    }

    private func checkPerfectMonth(userId: String) async {
        let start  = DateUtils.startOfMonth()
        let end    = DateUtils.endOfMonth()
        let income = (try? transactionRepo.totalIncome(userId: userId, start: start, end: end)) ?? 0
        let spend  = (try? transactionRepo.totalExpense(userId: userId, start: start, end: end)) ?? 0
        if income > 0 && income > spend { await awardBadge(userId: userId, type: .perfectMonth) }
    }

    private func checkBudgetMaster(userId: String) async {
        let cal   = Calendar.current
        let month = cal.component(.month, from: Date())
        let year  = cal.component(.year, from: Date())
        let start = DateUtils.startOfMonth()
        let end   = DateUtils.endOfMonth()
        guard let budgets = try? budgetRepo.forMonth(userId: userId, month: month, year: year),
              !budgets.isEmpty else { return }
        let allUnder = budgets.allSatisfy { budget in
            let spent = (try? transactionRepo.totalExpenseByCategory(
                userId: userId, categoryId: budget.categoryId, start: start, end: end)) ?? 0
            return spent < budget.limitAmount
        }
        if allUnder { await awardBadge(userId: userId, type: .budgetMaster) }
    }

    private func checkDebtSlayer(userId: String) async {
        let count = (try? debtRepo.paidOffCount(userId: userId)) ?? 0
        if count >= 1 { await awardBadge(userId: userId, type: .debtSlayer) }
    }

    private func checkGoalGetter(userId: String) async {
        let count = (try? goalRepo.completedCount(userId: userId)) ?? 0
        if count >= 1 { await awardBadge(userId: userId, type: .goalGetter) }
    }

    private func checkThriftyChamp(userId: String) async {
        let start  = DateUtils.startOfMonth()
        let end    = DateUtils.endOfMonth()
        let income = (try? transactionRepo.totalIncome(userId: userId, start: start, end: end)) ?? 0
        let spend  = (try? transactionRepo.totalExpense(userId: userId, start: start, end: end)) ?? 0
        if income > 0 && income >= spend * 1.5 { await awardBadge(userId: userId, type: .thriftyChamp) }
    }

    private func postNotification(type: BadgeType) {
        let content = UNMutableNotificationContent()
        content.title   = "\(type.icon)  Badge Unlocked — \(type.displayName)"
        content.body    = type.description
        content.sound   = .default
        let req = UNNotificationRequest(identifier: "badge_\(type.rawValue)",
                                        content: content, trigger: nil)
        UNUserNotificationCenter.current().add(req)
    }
}
