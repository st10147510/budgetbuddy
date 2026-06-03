import SwiftUI

struct BudgetDetailView: View {
    let item: BudgetWithSpend
    let userId: String
    @ObservedObject var vm: BudgetViewModel
    @Environment(\.dismiss) var dismiss

    var remaining: Double { item.budget.limitAmount - item.spent }

    var body: some View {
        List {
            Section {
                HStack {
                    Text(item.category.icon).font(.largeTitle)
                    Text(item.category.name).font(.title2.bold())
                }
            }

            Section("This Month") {
                LabeledContent("Spent",     value: item.spent.currencyFormatted)
                LabeledContent("Limit",     value: item.budget.limitAmount.currencyFormatted)
                LabeledContent("Remaining", value: remaining.currencyFormatted)
                    .foregroundColor(remaining < 0 ? .red : .primary)
                if item.budget.minAmount > 0 {
                    LabeledContent("Minimum goal", value: item.budget.minAmount.currencyFormatted)
                }
            }

            Section("Progress") {
                ProgressView(value: Double(min(item.progressPercent, 100)) / 100)
                    .tint(item.progressPercent >= 100 ? .red : item.progressPercent >= 80 ? .orange : .green)
                Text("\(item.progressPercent)% of limit used")
                    .font(.caption).foregroundStyle(.secondary)
            }

            Section {
                Button("Delete Budget", role: .destructive) {
                    vm.delete(item.budget, userId: userId)
                    dismiss()
                }
            }
        }
        .navigationTitle("Budget Detail")
        .navigationBarTitleDisplayMode(.inline)
    }
}
