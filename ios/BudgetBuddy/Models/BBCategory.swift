import Foundation
import SwiftData
import SwiftUI

@Model
final class BBCategory {
    @Attribute(.unique) var id: UUID
    var name: String
    var icon: String        // emoji
    var colorHex: String    // "#FF5722"
    var isDefault: Bool

    init(name: String, icon: String, colorHex: String, isDefault: Bool = false) {
        self.id        = UUID()
        self.name      = name
        self.icon      = icon
        self.colorHex  = colorHex
        self.isDefault = isDefault
    }

    var color: Color { Color(hex: colorHex) }
}

// MARK: - Default seed data (mirrors Android BudgetBuddyDatabase.Callback.onCreate)

enum DefaultCategorySeeder {
    static let defaults: [(name: String, icon: String, color: String)] = [
        ("Food & Dining",   "🍔", "#FF5722"),
        ("Transport",       "🚗", "#2196F3"),
        ("Shopping",        "🛍️", "#9C27B0"),
        ("Entertainment",   "🎬", "#FF9800"),
        ("Health",          "💊", "#4CAF50"),
        ("Utilities",       "💡", "#607D8B"),
        ("Housing",         "🏠", "#795548"),
        ("Education",       "📚", "#3F51B5"),
        ("Savings",         "💰", "#009688"),
        ("Other",           "📦", "#9E9E9E"),
    ]

    static func seedIfNeeded(context: ModelContext) {
        let existing = (try? context.fetch(FetchDescriptor<BBCategory>())) ?? []
        guard existing.isEmpty else { return }
        for d in defaults {
            context.insert(BBCategory(name: d.name, icon: d.icon, colorHex: d.color, isDefault: true))
        }
        try? context.save()
    }
}
