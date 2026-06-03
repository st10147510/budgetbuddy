import Foundation

enum DateUtils {
    static func startOfMonth(_ date: Date = .now) -> Date {
        let cal   = Calendar.current
        let comps = cal.dateComponents([.year, .month], from: date)
        return cal.date(from: comps) ?? date
    }

    static func endOfMonth(_ date: Date = .now) -> Date {
        let cal   = Calendar.current
        var comps = cal.dateComponents([.year, .month], from: date)
        comps.month! += 1
        let nextMonth = cal.date(from: comps) ?? date
        return cal.date(byAdding: .second, value: -1, to: nextMonth) ?? date
    }

    static func startOfDay(_ date: Date = .now) -> Date {
        Calendar.current.startOfDay(for: date)
    }

    static func endOfDay(_ date: Date = .now) -> Date {
        Calendar.current.date(byAdding: .second, value: 86399, to: startOfDay(date)) ?? date
    }

    static var monthLabel: String {
        let fmt = DateFormatter()
        fmt.dateFormat = "MMMM yyyy"
        return fmt.string(from: .now)
    }
}
