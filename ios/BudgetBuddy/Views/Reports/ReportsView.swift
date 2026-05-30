import SwiftUI
import Charts

struct ReportsView: View {
    let userId: String
    @ObservedObject var vm: ReportsViewModel

    private let monthLabels = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {

                    // Month picker
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(0..<12, id: \.self) { m in
                                let y = Calendar.current.component(.year, from: .now)
                                Button(monthLabels[m]) {
                                    vm.selectMonth(m, year: y)
                                }
                                .font(.caption.bold())
                                .padding(.horizontal, 12).padding(.vertical, 6)
                                .background(vm.selectedMonth == m ? Color.teal : Color(.secondarySystemBackground))
                                .foregroundColor(vm.selectedMonth == m ? .white : .primary)
                                .cornerRadius(20)
                            }
                        }
                        .padding(.horizontal, 16)
                    }

                    // Summary cards
                    HStack(spacing: 12) {
                        StatCard(label: "Income",  value: vm.uiState.totalIncome,  color: .green)
                        StatCard(label: "Expense", value: vm.uiState.totalExpense, color: .red)
                        StatCard(label: "Balance", value: vm.uiState.balance,      color: vm.uiState.balance >= 0 ? .teal : .red)
                    }
                    .padding(.horizontal, 16)

                    // Category spending pie / bar
                    if !vm.uiState.categorySpends.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Spending by Category").font(.headline).padding(.horizontal, 16)
                            Chart(vm.uiState.categorySpends) { item in
                                SectorMark(angle: .value("Amount", item.amount), innerRadius: .ratio(0.55))
                                    .foregroundStyle(Color(hex: item.colorHex))
                                    .annotation(position: .overlay) {
                                        Text(item.icon).font(.caption2)
                                    }
                            }
                            .frame(height: 200)
                            .padding(.horizontal, 16)

                            ForEach(vm.uiState.categorySpends) { item in
                                HStack {
                                    Circle().fill(Color(hex: item.colorHex)).frame(width: 10, height: 10)
                                    Text("\(item.icon) \(item.name)").font(.caption)
                                    Spacer()
                                    Text(item.amount.currencyFormatted).font(.caption.bold())
                                }
                                .padding(.horizontal, 16)
                            }
                        }
                        .padding(.vertical, 12)
                        .cardStyle()
                        .padding(.horizontal, 16)
                    }

                    // Budget vs spend bars
                    if !vm.uiState.categoryBudgetBars.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Budget vs. Spend").font(.headline)
                            ForEach(vm.uiState.categoryBudgetBars) { bar in
                                VStack(alignment: .leading, spacing: 4) {
                                    HStack {
                                        Text("\(bar.icon) \(bar.categoryName)").font(.caption.bold())
                                        Spacer()
                                        Text("\(bar.spent.currencyFormatted) / \(bar.limitAmount.currencyFormatted)")
                                            .font(.caption).foregroundStyle(.secondary)
                                    }
                                    ProgressView(value: bar.limitAmount > 0 ? min(1, bar.spent / bar.limitAmount) : 0)
                                        .tint(bar.spent >= bar.limitAmount ? .red : bar.spent >= bar.limitAmount * 0.8 ? .orange : Color(hex: bar.colorHex))
                                }
                            }
                        }
                        .padding()
                        .cardStyle()
                        .padding(.horizontal, 16)
                    }

                    // 6-month balance chart
                    if !vm.uiState.monthlyTotals.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("6-Month Balance").font(.headline)
                            Chart(vm.uiState.monthlyTotals) { item in
                                BarMark(x: .value("Month", item.label), y: .value("Balance", item.total))
                                    .foregroundStyle(item.total >= 0 ? Color.teal : Color.red)
                            }
                            .frame(height: 180)
                        }
                        .padding()
                        .cardStyle()
                        .padding(.horizontal, 16)
                    }
                }
                .padding(.vertical, 16)
            }
            .navigationTitle("Reports")
            .refreshable { vm.load(userId: userId) }
            .overlay { if vm.uiState.isLoading { ProgressView() } }
        }
    }
}

private struct StatCard: View {
    let label: String
    let value: Double
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            Text(value.currencyFormatted).font(.caption.bold()).foregroundColor(color).lineLimit(1).minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .cardStyle()
    }
}
