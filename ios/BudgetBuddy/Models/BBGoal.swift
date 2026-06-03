import Foundation
import SwiftData

@Model
final class BBGoal {
    @Attribute(.unique) var id: UUID
    var userId: String
    var name: String
    var targetAmount: Double
    var savedAmount: Double
    var targetDate: Date?
    var isCompleted: Bool
    var createdAt: Date

    var progressPercent: Int {
        guard targetAmount > 0 else { return 0 }
        return min(100, Int((savedAmount / targetAmount) * 100))
    }

    init(userId: String, name: String, targetAmount: Double,
         savedAmount: Double = 0, targetDate: Date? = nil) {
        self.id            = UUID()
        self.userId        = userId
        self.name          = name
        self.targetAmount  = targetAmount
        self.savedAmount   = savedAmount
        self.targetDate    = targetDate
        self.isCompleted   = false
        self.createdAt     = .now
    }
}
