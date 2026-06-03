import SwiftUI

struct DebtDetailView: View {
    let debt: BBDebt
    @ObservedObject var vm: DebtViewModel
    let userId: String
    @Environment(\.dismiss) var dismiss

    @State private var paymentStr = ""
    @State private var showPayment = false

    var payoffProgress: Double {
        guard debt.originalBalance > 0 else { return 0 }
        return max(0, min(1, 1 - (debt.balance / debt.originalBalance)))
    }

    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 12) {
                    Text(debt.name).font(.title2.bold())
                    ProgressView(value: payoffProgress).tint(.teal)
                    HStack {
                        Text("\((payoffProgress * 100), specifier: "%.0f")% paid off")
                            .font(.caption).foregroundStyle(.secondary)
                        Spacer()
                        Text("\(debt.balance.currencyFormatted) remaining")
                            .font(.caption.bold())
                    }
                }
                .padding(.vertical, 4)
            }

            Section("Details") {
                LabeledContent("Original Balance", value: debt.originalBalance.currencyFormatted)
                LabeledContent("Current Balance",  value: debt.balance.currencyFormatted)
                LabeledContent("Interest Rate",    value: "\(debt.interestRate, specifier: "%.2f")% APR")
                LabeledContent("Minimum Payment",  value: "\(debt.minimumPayment.currencyFormatted) / month")
            }

            if !debt.isPaidOff {
                Section {
                    Button("Record Payment") { showPayment = true }
                        .foregroundColor(.teal)
                    Button("Mark as Paid Off") {
                        Task {
                            try? await AppContainer.shared.debtRepo.markPaidOff(debt)
                            vm.load(userId: userId)
                            dismiss()
                        }
                    }
                    .foregroundColor(.green)
                }
            }

            Section {
                Button("Delete Debt", role: .destructive) {
                    vm.delete(debt); dismiss()
                }
            }
        }
        .navigationTitle("Debt")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showPayment) {
            PaymentSheet(debt: debt, vm: vm, userId: userId)
        }
    }
}

private struct PaymentSheet: View {
    let debt: BBDebt
    @ObservedObject var vm: DebtViewModel
    let userId: String
    @Environment(\.dismiss) var dismiss
    @State private var amountStr = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Payment amount for \(debt.name)") {
                    HStack {
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $amountStr).keyboardType(.decimalPad)
                    }
                }
                Section {
                    Text("Minimum payment: \(debt.minimumPayment.currencyFormatted)")
                        .font(.caption).foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Record Payment")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        if let amt = Double(amountStr), amt > 0 {
                            vm.makePayment(debt: debt, amount: amt, userId: userId)
                            dismiss()
                        }
                    }.bold()
                }
            }
        }
    }
}
