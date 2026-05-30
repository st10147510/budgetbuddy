import Foundation
import SwiftData

enum PayoffStrategy: String, Codable, CaseIterable {
    case snowball  = "SNOWBALL"
    case avalanche = "AVALANCHE"

    var label: String {
        switch self {
        case .snowball:  return "Snowball (smallest balance first)"
        case .avalanche: return "Avalanche (highest interest first)"
        }
    }
}

@Model
final class BBDebt {
    @Attribute(.unique) var id: UUID
    var userId: String
    var name: String
    var originalBalance: Double
    var balance: Double
    var interestRate: Double    // annual %
    var minimumPayment: Double
    var isPaidOff: Bool
    var createdAt: Date

    init(userId: String, name: String, balance: Double,
         interestRate: Double, minimumPayment: Double) {
        self.id              = UUID()
        self.userId          = userId
        self.name            = name
        self.originalBalance = balance
        self.balance         = balance
        self.interestRate    = interestRate
        self.minimumPayment  = minimumPayment
        self.isPaidOff       = false
        self.createdAt       = .now
    }
}

struct DebtPayoffMonth: Identifiable {
    var id = UUID()
    let month: Int
    let debtName: String
    let payment: Double
    let remainingBalance: Double
}
