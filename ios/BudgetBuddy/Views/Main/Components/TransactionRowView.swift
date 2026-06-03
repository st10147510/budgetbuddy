import SwiftUI

struct TransactionRowView: View {
    let transaction: BBTransaction
    let category: BBCategory?

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill((category.map { Color(hex: $0.colorHex) } ?? Color.gray).opacity(0.15))
                    .frame(width: 42, height: 42)
                Text(category?.icon ?? "📦")
                    .font(.title3)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(category?.name ?? "Uncategorised")
                    .font(.subheadline.weight(.medium))
                if let notes = transaction.notes, !notes.isEmpty {
                    Text(notes)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
                Text(transaction.date.formatted(style: .medium))
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }

            Spacer()

            Text("\(transaction.type == .income ? "+" : "-")\(transaction.amount.currencyFormatted)")
                .font(.subheadline.bold())
                .foregroundColor(transaction.type == .income ? .green : .primary)
        }
        .padding(.vertical, 4)
    }
}
