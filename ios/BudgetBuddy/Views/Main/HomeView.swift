import SwiftUI

struct HomeView: View {
    let userId: String
    @ObservedObject var vm: HomeViewModel
    @State private var showAddTransaction = false
    @State private var showReports = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // ── Balance Card ──────────────────────────────────────
                    BalanceCard(state: vm.uiState)

                    // ── Quick Goals Preview ───────────────────────────────
                    if !vm.uiState.activeGoals.isEmpty {
                        VStack(alignment: .leading, spacing: 10) {
                            SectionHeader("Active Goals")
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(vm.uiState.activeGoals.prefix(5)) { goal in
                                        GoalMiniCard(goal: goal)
                                    }
                                }
                                .padding(.horizontal, 16)
                            }
                        }
                    }

                    // ── Recent Transactions ───────────────────────────────
                    VStack(alignment: .leading, spacing: 10) {
                        SectionHeader("Recent Transactions")
                        if vm.uiState.recentTransactions.isEmpty {
                            EmptyStateView(icon: "creditcard", message: "No transactions yet.\nTap + to add one.")
                        } else {
                            ForEach(vm.uiState.recentTransactions) { tx in
                                TransactionRowView(transaction: tx,
                                                   category: vm.uiState.categories.first { $0.id == tx.categoryId })
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
                .padding(.vertical, 16)
            }
            .navigationTitle("Overview")
            .refreshable { vm.load(userId: userId) }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button { showAddTransaction = true } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title3)
                            .foregroundColor(.teal)
                    }
                }
            }
            .sheet(isPresented: $showAddTransaction, onDismiss: { vm.load(userId: userId) }) {
                AddTransactionView(userId: userId, vm: AppContainer.shared.makeExpenseVM())
            }
        }
    }
}

// MARK: - Sub-components

private struct BalanceCard: View {
    let state: HomeUiState

    var body: some View {
        VStack(spacing: 20) {
            Text("Monthly Balance")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Text(state.balance.currencyFormatted)
                .font(.system(size: 40, weight: .bold, design: .rounded))
                .foregroundColor(state.balance >= 0 ? .primary : .red)

            HStack(spacing: 0) {
                StatItem(label: "Income", value: state.totalIncomeThisMonth, color: .green)
                Divider().frame(height: 36)
                StatItem(label: "Spent",  value: state.totalSpendThisMonth,  color: .red)
            }
            .padding(.horizontal)
            .background(Color(.tertiarySystemBackground), in: RoundedRectangle(cornerRadius: 12))
        }
        .padding(20)
        .cardStyle()
        .padding(.horizontal, 16)
    }
}

private struct StatItem: View {
    let label: String
    let value: Double
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            Text(value.currencyFormatted).font(.subheadline.bold()).foregroundColor(color)
        }
        .frame(maxWidth: .infinity)
    }
}

private struct GoalMiniCard: View {
    let goal: BBGoal

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(goal.name).font(.caption.bold()).lineLimit(1)
            ProgressView(value: Double(goal.progressPercent) / 100)
                .tint(.teal)
            Text("\(goal.progressPercent)%")
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .padding(12)
        .frame(width: 130)
        .cardStyle()
    }
}

private struct SectionHeader: View {
    let title: String
    init(_ title: String) { self.title = title }
    var body: some View {
        Text(title)
            .font(.headline)
            .padding(.horizontal, 16)
    }
}
