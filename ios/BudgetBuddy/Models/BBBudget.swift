import Foundation
import SwiftData

@Model
final class BBBudget {
    @Attribute(.unique) var id: UUID
    var userId: String
    var categoryId: UUID
    var minAmount: Double   // minimum spend goal
    var limitAmount: Double // maximum spend cap
    var month: Int          // 1–12
    var year: Int
    var createdAt: Date

    init(userId: String, categoryId: UUID, limitAmount: Double,
         minAmount: Double = 0, month: Int, year: Int) {
        self.id          = UUID()
        self.userId      = userId
        self.categoryId  = categoryId
        self.minAmount   = minAmount
        self.limitAmount = limitAmount
        self.month       = month
        self.year        = year
        self.createdAt   = .now
    }
}
