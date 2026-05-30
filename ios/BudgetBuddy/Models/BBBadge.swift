import Foundation
import SwiftData

enum BadgeType: String, Codable, CaseIterable {
    case firstStep    = "FIRST_STEP"
    case dailyTracker = "DAILY_TRACKER"
    case perfectMonth = "PERFECT_MONTH"
    case budgetMaster = "BUDGET_MASTER"
    case debtSlayer   = "DEBT_SLAYER"
    case goalGetter   = "GOAL_GETTER"
    case thriftyChamp = "THRIFTY_CHAMP"

    var displayName: String {
        switch self {
        case .firstStep:    return "First Step"
        case .dailyTracker: return "Daily Tracker"
        case .perfectMonth: return "Perfect Month"
        case .budgetMaster: return "Budget Master"
        case .debtSlayer:   return "Debt Slayer"
        case .goalGetter:   return "Goal Getter"
        case .thriftyChamp: return "Thrifty Champ"
        }
    }

    var icon: String {
        switch self {
        case .firstStep:    return "🎯"
        case .dailyTracker: return "🔥"
        case .perfectMonth: return "📅"
        case .budgetMaster: return "💰"
        case .debtSlayer:   return "⚔️"
        case .goalGetter:   return "🏆"
        case .thriftyChamp: return "💎"
        }
    }

    var description: String {
        switch self {
        case .firstStep:    return "Log your first transaction."
        case .dailyTracker: return "Log transactions 7 days in a row."
        case .perfectMonth: return "Earn more than you spend in a month."
        case .budgetMaster: return "Stay under every budget limit for a month."
        case .debtSlayer:   return "Pay off a debt in full."
        case .goalGetter:   return "Complete a savings goal."
        case .thriftyChamp: return "Save at least 33% of your income in a month."
        }
    }
}

@Model
final class BBBadge {
    @Attribute(.unique) var id: UUID
    var userId: String
    var badgeType: BadgeType
    var earnedAt: Date

    init(userId: String, badgeType: BadgeType) {
        self.id        = UUID()
        self.userId    = userId
        self.badgeType = badgeType
        self.earnedAt  = .now
    }
}
