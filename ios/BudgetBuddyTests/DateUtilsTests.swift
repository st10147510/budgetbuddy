import XCTest
@testable import BudgetBuddy

final class DateUtilsTests: XCTestCase {

    private let cal = Calendar.current

    // MARK: - startOfMonth

    func test_startOfMonth_is_first_day() {
        let date  = makeDate(year: 2024, month: 6, day: 15)
        let start = DateUtils.startOfMonth(date)
        let comps = cal.dateComponents([.year, .month, .day, .hour, .minute, .second], from: start)
        XCTAssertEqual(comps.year,  2024)
        XCTAssertEqual(comps.month, 6)
        XCTAssertEqual(comps.day,   1)
        XCTAssertEqual(comps.hour,  0)
        XCTAssertEqual(comps.minute,0)
        XCTAssertEqual(comps.second,0)
    }

    func test_startOfMonth_january() {
        let date  = makeDate(year: 2024, month: 1, day: 31)
        let start = DateUtils.startOfMonth(date)
        XCTAssertEqual(cal.component(.day, from: start), 1)
        XCTAssertEqual(cal.component(.month, from: start), 1)
    }

    func test_startOfMonth_december() {
        let date  = makeDate(year: 2023, month: 12, day: 25)
        let start = DateUtils.startOfMonth(date)
        XCTAssertEqual(cal.component(.day, from: start), 1)
        XCTAssertEqual(cal.component(.month, from: start), 12)
    }

    // MARK: - endOfMonth

    func test_endOfMonth_june_is_30th() {
        let date = makeDate(year: 2024, month: 6, day: 1)
        let end  = DateUtils.endOfMonth(date)
        XCTAssertEqual(cal.component(.day, from: end), 30)
        XCTAssertEqual(cal.component(.month, from: end), 6)
    }

    func test_endOfMonth_january_is_31st() {
        let date = makeDate(year: 2024, month: 1, day: 1)
        let end  = DateUtils.endOfMonth(date)
        XCTAssertEqual(cal.component(.day, from: end), 31)
    }

    func test_endOfMonth_february_leap_year_is_29th() {
        let date = makeDate(year: 2024, month: 2, day: 1)   // 2024 is a leap year
        let end  = DateUtils.endOfMonth(date)
        XCTAssertEqual(cal.component(.day, from: end), 29)
    }

    func test_endOfMonth_february_non_leap_is_28th() {
        let date = makeDate(year: 2023, month: 2, day: 1)
        let end  = DateUtils.endOfMonth(date)
        XCTAssertEqual(cal.component(.day, from: end), 28)
    }

    func test_endOfMonth_time_is_235959() {
        let date = makeDate(year: 2024, month: 3, day: 10)
        let end  = DateUtils.endOfMonth(date)
        let comps = cal.dateComponents([.hour, .minute, .second], from: end)
        XCTAssertEqual(comps.hour,   23)
        XCTAssertEqual(comps.minute, 59)
        XCTAssertEqual(comps.second, 59)
    }

    func test_start_before_end_for_same_month() {
        let date  = makeDate(year: 2024, month: 8, day: 14)
        let start = DateUtils.startOfMonth(date)
        let end   = DateUtils.endOfMonth(date)
        XCTAssertLessThan(start, end)
    }

    // MARK: - startOfDay / endOfDay

    func test_startOfDay_is_midnight() {
        let date  = makeDate(year: 2024, month: 5, day: 20, hour: 14, minute: 30)
        let start = DateUtils.startOfDay(date)
        let comps = cal.dateComponents([.hour, .minute, .second], from: start)
        XCTAssertEqual(comps.hour,   0)
        XCTAssertEqual(comps.minute, 0)
        XCTAssertEqual(comps.second, 0)
    }

    func test_endOfDay_is_235959() {
        let date = makeDate(year: 2024, month: 5, day: 20, hour: 9, minute: 0)
        let end  = DateUtils.endOfDay(date)
        let comps = cal.dateComponents([.hour, .minute, .second], from: end)
        XCTAssertEqual(comps.hour,   23)
        XCTAssertEqual(comps.minute, 59)
        XCTAssertEqual(comps.second, 59)
    }

    func test_startOfDay_before_endOfDay() {
        let date  = makeDate(year: 2024, month: 7, day: 4)
        let start = DateUtils.startOfDay(date)
        let end   = DateUtils.endOfDay(date)
        XCTAssertLessThan(start, end)
    }

    func test_startOfDay_same_date_as_input() {
        let date  = makeDate(year: 2024, month: 11, day: 11)
        let start = DateUtils.startOfDay(date)
        XCTAssertEqual(cal.component(.day,   from: start), 11)
        XCTAssertEqual(cal.component(.month, from: start), 11)
        XCTAssertEqual(cal.component(.year,  from: start), 2024)
    }

    // MARK: - monthLabel

    func test_monthLabel_contains_year() {
        let label = DateUtils.monthLabel
        XCTAssertTrue(label.contains("202"), "Expected year in '\(label)'")
    }

    // MARK: - Helpers

    private func makeDate(year: Int, month: Int, day: Int,
                          hour: Int = 0, minute: Int = 0) -> Date {
        var comps       = DateComponents()
        comps.year      = year
        comps.month     = month
        comps.day       = day
        comps.hour      = hour
        comps.minute    = minute
        return cal.date(from: comps)!
    }
}
