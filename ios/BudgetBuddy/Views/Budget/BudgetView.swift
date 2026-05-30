import SwiftUI

struct BudgetView: View {
    let userId: String
    @ObservedObject var vm: BudgetViewModel
    @State private var showAdd = false

    var body: some View {
        NavigationStack {
            Group {
                if vm.budgetsWithSpend.isEmpty && !vm.isLoading {
                    EmptyStateView(icon: "chart.bar", message: "No budgets set.\nTap + to create one.")
                } else {
                    List {
                        ForEach(vm.budgetsWithSpend) { item in
                            NavigationLink {
                                BudgetDetailView(item: item, userId: userId, vm: vm)
                            } label: {
                                BudgetRowView(item: item)
                            }
                        }
                        .onDelete { idxs in
                            idxs.forEach { vm.delete(vm.budgetsWithSpend[$0].budget, userId: userId) }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Budgets — \(DateUtils.monthLabel)")
            .refreshable { vm.load(userId: userId) }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button { showAdd = true } label: { Image(systemName: "plus") }
                }
            }
            .sheet(isPresented: $showAdd, onDismiss: { vm.load(userId: userId) }) {
                AddBudgetView(userId: userId, vm: vm)
            }
            .overlay { if vm.isLoading { ProgressView() } }
        }
    }
}

private struct BudgetRowView: View {
    let item: BudgetWithSpend

    var statusColor: Color {
        switch item.status {
        case .exceeded: return .red
        case .warning:  return .orange
        case .underMin: return .yellow
        case .ok:       return .green
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(item.category.icon).font(.title3)
                Text(item.category.name).font(.subheadline.bold())
                Spacer()
                Text(item.spent.currencyFormatted).font(.subheadline)
                Text("/ \(item.budget.limitAmount.currencyFormatted)").font(.caption).foregroundStyle(.secondary)
            }
            ProgressView(value: Double(item.progressPercent) / 100)
                .tint(statusColor)
            HStack {
                Text("\(item.progressPercent)% used").font(.caption).foregroundStyle(.secondary)
                Spacer()
                if case .exceeded = item.status {
                    Label("Over budget", systemImage: "exclamationmark.triangle.fill")
                        .font(.caption).foregroundColor(.red)
                }
            }
        }
        .padding(.vertical, 4)
    }
}
