import SwiftUI

struct TransactionDetailView: View {
    let transaction: BBTransaction
    @ObservedObject var vm: ExpenseViewModel
    let userId: String
    @Environment(\.dismiss) var dismiss

    private var category: BBCategory? { vm.category(for: transaction) }

    var body: some View {
        List {
            Section {
                HStack {
                    Text(category?.icon ?? "📦").font(.largeTitle)
                    VStack(alignment: .leading) {
                        Text(category?.name ?? "Uncategorised").font(.headline)
                        Text(transaction.type == .income ? "Income" : "Expense")
                            .font(.caption).foregroundColor(transaction.type == .income ? .green : .red)
                    }
                    Spacer()
                    Text("\(transaction.type == .income ? "+" : "-")\(transaction.amount.currencyFormatted)")
                        .font(.title2.bold())
                        .foregroundColor(transaction.type == .income ? .green : .primary)
                }
                .padding(.vertical, 4)
            }

            Section("Details") {
                LabeledContent("Date", value: transaction.date.formatted(style: .long))
                if let notes = transaction.notes, !notes.isEmpty {
                    LabeledContent("Notes") { Text(notes).multilineTextAlignment(.trailing) }
                }
            }

            if let path = transaction.receiptImagePath, !path.isEmpty {
                Section("Receipt") {
                    AsyncImage(url: URL(string: path)) { img in
                        img.resizable().scaledToFit().cornerRadius(10)
                    } placeholder: { ProgressView() }
                }
            }

            Section {
                Button("Delete Transaction", role: .destructive) {
                    vm.delete(transaction)
                    dismiss()
                }
            }
        }
        .navigationTitle("Transaction")
        .navigationBarTitleDisplayMode(.inline)
    }
}
