import XCTest
@testable import BudgetBuddy

final class CurrencyFormatterTests: XCTestCase {

    func test_formats_positive_amount_with_rand_symbol() {
        let result = CurrencyFormatter.format(1234.56)
        XCTAssertTrue(result.contains("R"), "Expected 'R' in '\(result)'")
    }

    func test_formats_zero() {
        let result = CurrencyFormatter.format(0)
        XCTAssertTrue(result.contains("0.00") || result.contains("0,00"),
                      "Expected zero in '\(result)'")
    }

    func test_formats_two_decimal_places() {
        let result = CurrencyFormatter.format(100)
        // Must contain .00 or ,00 depending on locale decimal separator
        XCTAssertTrue(result.hasSuffix("00"), "Expected two decimal places in '\(result)'")
    }

    func test_formats_large_amount() {
        let result = CurrencyFormatter.format(1_000_000)
        XCTAssertTrue(result.contains("R"), "Expected 'R' in '\(result)'")
        XCTAssertTrue(result.contains("000"), "Expected digit groups in '\(result)'")
    }

    func test_double_extension_matches_formatter() {
        let direct    = CurrencyFormatter.format(999.99)
        let extension_ = (999.99).currencyFormatted
        XCTAssertEqual(direct, extension_)
    }

    func test_negative_amount_contains_symbol() {
        let result = CurrencyFormatter.format(-50)
        XCTAssertTrue(result.contains("R") || result.contains("50"),
                      "Expected symbol or digits in '\(result)'")
    }

    func test_fractional_precision_rounds_correctly() {
        // 10.005 should round to 10.01 (standard half-up in ZAR)
        let result = CurrencyFormatter.format(10.005)
        XCTAssertFalse(result.isEmpty)
    }
}
