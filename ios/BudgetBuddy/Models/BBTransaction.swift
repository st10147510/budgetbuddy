import Foundation
import SwiftData

enum TransactionType: String, Codable {
    case expense = "EXPENSE"
    case income  = "INCOME"
}

@Model
final class BBTransaction {
    @Attribute(.unique) var id: UUID
    var userId: String
    var amount: Double
    var categoryId: UUID?
    var date: Date
    var notes: String?
    var receiptImagePath: String?
    var type: TransactionType
    var createdAt: Date

    init(userId: String, amount: Double, categoryId: UUID? = nil,
         date: Date = .now, notes: String? = nil,
         receiptImagePath: String? = nil, type: TransactionType = .expense) {
        self.id             = UUID()
        self.userId         = userId
        self.amount         = amount
        self.categoryId     = categoryId
        self.date           = date
        self.notes          = notes
        self.receiptImagePath = receiptImagePath
        self.type           = type
        self.createdAt      = .now
    }
}
