import XCTest
import SwiftData
@testable import BudgetBuddy

final class BBCategoryTests: XCTestCase {

    private var container: ModelContainer!
    private var context: ModelContext!

    override func setUpWithError() throws {
        let config = ModelConfiguration(isStoredInMemoryOnly: true)
        container  = try ModelContainer(for: BBCategory.self, configurations: config)
        context    = ModelContext(container)
    }

    override func tearDown() {
        container = nil
        context   = nil
    }

    // MARK: - DefaultCategorySeeder.defaults

    func test_defaults_count_is_ten() {
        XCTAssertEqual(DefaultCategorySeeder.defaults.count, 10)
    }

    func test_defaults_first_is_food_and_dining() {
        XCTAssertEqual(DefaultCategorySeeder.defaults[0].name, "Food & Dining")
    }

    func test_defaults_last_is_other() {
        XCTAssertEqual(DefaultCategorySeeder.defaults[9].name, "Other")
    }

    func test_all_defaults_have_non_empty_icon() {
        for d in DefaultCategorySeeder.defaults {
            XCTAssertFalse(d.icon.isEmpty, "\(d.name) has empty icon")
        }
    }

    func test_all_defaults_have_hex_color_starting_with_hash() {
        for d in DefaultCategorySeeder.defaults {
            XCTAssertTrue(d.color.hasPrefix("#"), "\(d.name) color '\(d.color)' doesn't start with #")
        }
    }

    func test_all_defaults_have_six_digit_hex_color() {
        for d in DefaultCategorySeeder.defaults {
            // "#RRGGBB" → 7 chars total
            XCTAssertEqual(d.color.count, 7, "\(d.name) color '\(d.color)' is not 7 chars (#RRGGBB)")
        }
    }

    func test_expected_category_names_present() {
        let names = DefaultCategorySeeder.defaults.map(\.name)
        XCTAssertTrue(names.contains("Transport"))
        XCTAssertTrue(names.contains("Health"))
        XCTAssertTrue(names.contains("Utilities"))
        XCTAssertTrue(names.contains("Housing"))
        XCTAssertTrue(names.contains("Education"))
        XCTAssertTrue(names.contains("Savings"))
        XCTAssertTrue(names.contains("Entertainment"))
        XCTAssertTrue(names.contains("Shopping"))
    }

    func test_default_names_are_unique() {
        let names = DefaultCategorySeeder.defaults.map(\.name)
        XCTAssertEqual(names.count, Set(names).count, "Duplicate default category names")
    }

    // MARK: - seedIfNeeded

    func test_seed_inserts_ten_categories_into_empty_context() throws {
        DefaultCategorySeeder.seedIfNeeded(context: context)
        let fetched = try context.fetch(FetchDescriptor<BBCategory>())
        XCTAssertEqual(fetched.count, 10)
    }

    func test_seed_does_not_duplicate_if_called_twice() throws {
        DefaultCategorySeeder.seedIfNeeded(context: context)
        DefaultCategorySeeder.seedIfNeeded(context: context)
        let fetched = try context.fetch(FetchDescriptor<BBCategory>())
        XCTAssertEqual(fetched.count, 10)
    }

    func test_seed_marks_all_as_default() throws {
        DefaultCategorySeeder.seedIfNeeded(context: context)
        let fetched = try context.fetch(FetchDescriptor<BBCategory>())
        XCTAssertTrue(fetched.allSatisfy(\.isDefault), "Some seeded categories are not marked isDefault")
    }

    func test_seed_skips_when_categories_already_exist() throws {
        // Insert a single custom category first
        context.insert(BBCategory(name: "Custom", icon: "⭐", colorHex: "#000000"))
        try context.save()

        DefaultCategorySeeder.seedIfNeeded(context: context)

        let fetched = try context.fetch(FetchDescriptor<BBCategory>())
        // Should NOT add the 10 defaults — only the 1 custom one
        XCTAssertEqual(fetched.count, 1)
    }

    // MARK: - BBCategory init

    func test_category_id_is_unique() {
        let a = BBCategory(name: "A", icon: "🍔", colorHex: "#FF0000")
        let b = BBCategory(name: "B", icon: "🚗", colorHex: "#0000FF")
        XCTAssertNotEqual(a.id, b.id)
    }

    func test_is_default_defaults_to_false() {
        let cat = BBCategory(name: "Custom", icon: "⭐", colorHex: "#FFFFFF")
        XCTAssertFalse(cat.isDefault)
    }

    func test_is_default_can_be_set_true() {
        let cat = BBCategory(name: "Food", icon: "🍔", colorHex: "#FF5722", isDefault: true)
        XCTAssertTrue(cat.isDefault)
    }

    func test_color_computed_property_does_not_crash() {
        let cat = BBCategory(name: "Food", icon: "🍔", colorHex: "#FF5722")
        // Just accessing .color should not throw/crash
        _ = cat.color
    }
}
