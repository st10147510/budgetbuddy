import XCTest
@testable import BudgetBuddy

final class BadgeTypeTests: XCTestCase {

    // MARK: - Raw values

    func test_raw_values_are_screaming_snake_case() {
        XCTAssertEqual(BadgeType.firstStep.rawValue,    "FIRST_STEP")
        XCTAssertEqual(BadgeType.dailyTracker.rawValue, "DAILY_TRACKER")
        XCTAssertEqual(BadgeType.perfectMonth.rawValue, "PERFECT_MONTH")
        XCTAssertEqual(BadgeType.budgetMaster.rawValue, "BUDGET_MASTER")
        XCTAssertEqual(BadgeType.debtSlayer.rawValue,   "DEBT_SLAYER")
        XCTAssertEqual(BadgeType.goalGetter.rawValue,   "GOAL_GETTER")
        XCTAssertEqual(BadgeType.thriftyChamp.rawValue, "THRIFTY_CHAMP")
    }

    func test_all_cases_count_is_seven() {
        XCTAssertEqual(BadgeType.allCases.count, 7)
    }

    // MARK: - displayName

    func test_display_names_are_non_empty() {
        for badge in BadgeType.allCases {
            XCTAssertFalse(badge.displayName.isEmpty, "\(badge) has empty displayName")
        }
    }

    func test_display_names_match_expected_strings() {
        XCTAssertEqual(BadgeType.firstStep.displayName,    "First Step")
        XCTAssertEqual(BadgeType.dailyTracker.displayName, "Daily Tracker")
        XCTAssertEqual(BadgeType.perfectMonth.displayName, "Perfect Month")
        XCTAssertEqual(BadgeType.budgetMaster.displayName, "Budget Master")
        XCTAssertEqual(BadgeType.debtSlayer.displayName,   "Debt Slayer")
        XCTAssertEqual(BadgeType.goalGetter.displayName,   "Goal Getter")
        XCTAssertEqual(BadgeType.thriftyChamp.displayName, "Thrifty Champ")
    }

    // MARK: - icon (emoji)

    func test_icons_are_non_empty() {
        for badge in BadgeType.allCases {
            XCTAssertFalse(badge.icon.isEmpty, "\(badge) has empty icon")
        }
    }

    func test_icons_match_expected_emoji() {
        XCTAssertEqual(BadgeType.firstStep.icon,    "🎯")
        XCTAssertEqual(BadgeType.dailyTracker.icon, "🔥")
        XCTAssertEqual(BadgeType.perfectMonth.icon, "📅")
        XCTAssertEqual(BadgeType.budgetMaster.icon, "💰")
        XCTAssertEqual(BadgeType.debtSlayer.icon,   "⚔️")
        XCTAssertEqual(BadgeType.goalGetter.icon,   "🏆")
        XCTAssertEqual(BadgeType.thriftyChamp.icon, "💎")
    }

    func test_all_icons_are_unique() {
        let icons = BadgeType.allCases.map(\.icon)
        XCTAssertEqual(icons.count, Set(icons).count, "Duplicate badge icons found")
    }

    // MARK: - description

    func test_descriptions_are_non_empty() {
        for badge in BadgeType.allCases {
            XCTAssertFalse(badge.description.isEmpty, "\(badge) has empty description")
        }
    }

    func test_descriptions_end_with_period() {
        for badge in BadgeType.allCases {
            XCTAssertTrue(badge.description.hasSuffix("."),
                          "\(badge.displayName) description does not end with '.'")
        }
    }

    func test_descriptions_match_expected_strings() {
        XCTAssertEqual(BadgeType.firstStep.description,    "Log your first transaction.")
        XCTAssertEqual(BadgeType.dailyTracker.description, "Log transactions 7 days in a row.")
        XCTAssertEqual(BadgeType.perfectMonth.description, "Earn more than you spend in a month.")
        XCTAssertEqual(BadgeType.budgetMaster.description, "Stay under every budget limit for a month.")
        XCTAssertEqual(BadgeType.debtSlayer.description,   "Pay off a debt in full.")
        XCTAssertEqual(BadgeType.goalGetter.description,   "Complete a savings goal.")
        XCTAssertEqual(BadgeType.thriftyChamp.description, "Save at least 33% of your income in a month.")
    }

    // MARK: - Codability

    func test_codable_round_trip() throws {
        for badge in BadgeType.allCases {
            let encoded = try JSONEncoder().encode(badge)
            let decoded = try JSONDecoder().decode(BadgeType.self, from: encoded)
            XCTAssertEqual(decoded, badge)
        }
    }

    func test_init_from_raw_value_succeeds() {
        XCTAssertNotNil(BadgeType(rawValue: "FIRST_STEP"))
        XCTAssertNotNil(BadgeType(rawValue: "THRIFTY_CHAMP"))
    }

    func test_init_from_invalid_raw_value_returns_nil() {
        XCTAssertNil(BadgeType(rawValue: "INVALID"))
        XCTAssertNil(BadgeType(rawValue: "first_step"))   // lowercase
    }
}
