import XCTest
import SwiftUI
@testable import BudgetBuddy

final class ExtensionsTests: XCTestCase {

    // MARK: - Color(hex:)

    func test_6_digit_hex_red() {
        // #FF0000 → red (r=1, g=0, b=0)
        let color = Color(hex: "FF0000")
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        UIColor(color).getRed(&r, green: &g, blue: &b, alpha: &a)
        XCTAssertEqual(Double(r), 1.0, accuracy: 0.01)
        XCTAssertEqual(Double(g), 0.0, accuracy: 0.01)
        XCTAssertEqual(Double(b), 0.0, accuracy: 0.01)
    }

    func test_6_digit_hex_green() {
        let color = Color(hex: "00FF00")
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        UIColor(color).getRed(&r, green: &g, blue: &b, alpha: &a)
        XCTAssertEqual(Double(g), 1.0, accuracy: 0.01)
        XCTAssertEqual(Double(r), 0.0, accuracy: 0.01)
    }

    func test_6_digit_hex_blue() {
        let color = Color(hex: "0000FF")
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        UIColor(color).getRed(&r, green: &g, blue: &b, alpha: &a)
        XCTAssertEqual(Double(b), 1.0, accuracy: 0.01)
        XCTAssertEqual(Double(r), 0.0, accuracy: 0.01)
    }

    func test_3_digit_hex_white() {
        // #FFF → white
        let color = Color(hex: "FFF")
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        UIColor(color).getRed(&r, green: &g, blue: &b, alpha: &a)
        XCTAssertEqual(Double(r), 1.0, accuracy: 0.01)
        XCTAssertEqual(Double(g), 1.0, accuracy: 0.01)
        XCTAssertEqual(Double(b), 1.0, accuracy: 0.01)
    }

    func test_3_digit_hex_black() {
        let color = Color(hex: "000")
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        UIColor(color).getRed(&r, green: &g, blue: &b, alpha: &a)
        XCTAssertEqual(Double(r), 0.0, accuracy: 0.01)
        XCTAssertEqual(Double(g), 0.0, accuracy: 0.01)
        XCTAssertEqual(Double(b), 0.0, accuracy: 0.01)
    }

    func test_8_digit_hex_with_alpha() {
        // #80FF0000 → red with 50% opacity
        let color = Color(hex: "80FF0000")
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        UIColor(color).getRed(&r, green: &g, blue: &b, alpha: &a)
        XCTAssertEqual(Double(r), 1.0, accuracy: 0.01)
        XCTAssertEqual(Double(a), Double(0x80) / 255.0, accuracy: 0.01)
    }

    func test_hex_with_hash_prefix_stripped() {
        // The init trims non-alphanumeric chars, so #FF0000 == FF0000
        let withHash    = Color(hex: "#FF0000")
        let withoutHash = Color(hex: "FF0000")
        var r1: CGFloat = 0, g1: CGFloat = 0, b1: CGFloat = 0, a1: CGFloat = 0
        var r2: CGFloat = 0, g2: CGFloat = 0, b2: CGFloat = 0, a2: CGFloat = 0
        UIColor(withHash).getRed(&r1, green: &g1, blue: &b1, alpha: &a1)
        UIColor(withoutHash).getRed(&r2, green: &g2, blue: &b2, alpha: &a2)
        XCTAssertEqual(Double(r1), Double(r2), accuracy: 0.01)
        XCTAssertEqual(Double(g1), Double(g2), accuracy: 0.01)
        XCTAssertEqual(Double(b1), Double(b2), accuracy: 0.01)
    }

    func test_invalid_hex_defaults_to_black() {
        let color = Color(hex: "ZZZZZZ")
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        UIColor(color).getRed(&r, green: &g, blue: &b, alpha: &a)
        XCTAssertEqual(Double(r), 0.0, accuracy: 0.01)
        XCTAssertEqual(Double(g), 0.0, accuracy: 0.01)
        XCTAssertEqual(Double(b), 0.0, accuracy: 0.01)
    }

    // MARK: - Double.currencyFormatted

    func test_currency_formatted_contains_rand_symbol() {
        XCTAssertTrue((500.0).currencyFormatted.contains("R"))
    }

    func test_currency_formatted_matches_formatter() {
        let value: Double = 12345.67
        XCTAssertEqual(value.currencyFormatted, CurrencyFormatter.format(value))
    }

    // MARK: - Date.formatted(style:)

    func test_date_formatted_returns_non_empty_string() {
        let date = Date(timeIntervalSince1970: 0)  // 1970-01-01
        XCTAssertFalse(date.formatted(style: .medium).isEmpty)
    }

    func test_date_monthYearLabel_contains_year() {
        let date = Date(timeIntervalSince1970: 0)
        XCTAssertTrue(date.monthYearLabel.contains("1970"))
    }
}
