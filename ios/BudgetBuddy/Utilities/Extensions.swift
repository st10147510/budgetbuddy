import SwiftUI

// MARK: - Color from hex string

extension Color {
    init(hex: String) {
        let h = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: h).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch h.count {
        case 3: (a,r,g,b) = (255,(int>>8)*17,(int>>4&0xF)*17,(int&0xF)*17)
        case 6: (a,r,g,b) = (255, int>>16, int>>8&0xFF, int&0xFF)
        case 8: (a,r,g,b) = (int>>24, int>>16&0xFF, int>>8&0xFF, int&0xFF)
        default:(a,r,g,b) = (255,0,0,0)
        }
        self.init(.sRGB, red: Double(r)/255, green: Double(g)/255, blue: Double(b)/255, opacity: Double(a)/255)
    }
}

// MARK: - View modifiers

extension View {
    func cardStyle() -> some View {
        self
            .background(Color(.secondarySystemBackground))
            .cornerRadius(16)
    }

    func tealButton() -> some View {
        self
            .font(.subheadline.weight(.semibold))
            .foregroundColor(Color(.systemBackground))
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color.teal)
            .cornerRadius(14)
    }
}

// MARK: - Date formatting helpers

extension Date {
    func formatted(style: DateFormatter.Style = .medium) -> String {
        let f = DateFormatter()
        f.dateStyle = style
        return f.string(from: self)
    }

    var monthYearLabel: String {
        let f = DateFormatter()
        f.dateFormat = "MMMM yyyy"
        return f.string(from: self)
    }
}

// MARK: - Double

extension Double {
    var currencyFormatted: String { CurrencyFormatter.format(self) }
}
