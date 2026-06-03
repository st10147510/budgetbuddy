import SwiftUI

struct DebtView: View {
    let userId: String
    @ObservedObject var vm: DebtViewModel
    @State private var showAdd  = false
    @State private var showPlan = false

    var body: some View {
        NavigationStack {
            Group {
                if vm.debts.isEmpty && !vm.isLoading {
                    EmptyStateView(icon: "creditcard.trianglebadge.exclamationmark",
                                   message: "No debts recorded.\nTap + to add one.")
                } else {
                    List {
                        if !vm.activeDebts.isEmpty {
                            Section(header: HStack {
                                Text("Active — \(vm.totalOwed.currencyFormatted) total")
                                Spacer()
                                Text("Min \(vm.totalMinPayment.currencyFormatted)/mo")
                                    .font(.caption).foregroundStyle(.secondary)
                            }) {
                                ForEach(vm.activeDebts) { debt in
                                    NavigationLink {
                                        DebtDetailView(debt: debt, vm: vm, userId: userId)
                                    } label: {
                                        DebtRowView(debt: debt)
                                    }
                                }
                                .onDelete { idxs in idxs.forEach { vm.delete(vm.activeDebts[$0]) } }
                            }
                        }

                        if !vm.paidDebts.isEmpty {
                            Section("Paid Off 🎉") {
                                ForEach(vm.paidDebts) { debt in
                                    DebtRowView(debt: debt)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Debts")
            .refreshable { vm.load(userId: userId) }
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    if !vm.activeDebts.isEmpty {
                        Button("Payment Plan") { showPlan = true }
                            .foregroundColor(.teal)
                    }
                }
                ToolbarItem(placement: .primaryAction) {
                    Button { showAdd = true } label: { Image(systemName: "plus") }
                }
            }
            .sheet(isPresented: $showAdd, onDismiss: { vm.load(userId: userId) }) {
                AddDebtView(userId: userId, vm: vm)
            }
            .sheet(isPresented: $showPlan) {
                PaymentPlanView(vm: vm)
            }
        }
    }
}

private struct DebtRowView: View {
    let debt: BBDebt

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(debt.name).font(.subheadline.bold())
                Spacer()
                Text(debt.balance.currencyFormatted).font(.subheadline.bold())
                    .foregroundColor(debt.isPaidOff ? .green : .primary)
            }
            HStack {
                Text("\(debt.interestRate, specifier: "%.1f")% APR")
                    .font(.caption).foregroundStyle(.secondary)
                Spacer()
                Text("Min \(debt.minimumPayment.currencyFormatted)/mo")
                    .font(.caption).foregroundStyle(.secondary)
            }
            if !debt.isPaidOff {
                let progress = debt.originalBalance > 0 ? 1 - (debt.balance / debt.originalBalance) : 0
                ProgressView(value: max(0, min(1, progress))).tint(.teal)
            }
        }
        .padding(.vertical, 2)
    }
}
