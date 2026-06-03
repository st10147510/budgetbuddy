import SwiftUI

struct AddDebtView: View {
    let userId: String
    @ObservedObject var vm: DebtViewModel
    @Environment(\.dismiss) var dismiss

    @State private var name        = ""
    @State private var balanceStr  = ""
    @State private var rateStr     = ""
    @State private var minPayStr   = ""

    private var canSave: Bool {
        !name.isEmpty && (Double(balanceStr) ?? 0) > 0 && (Double(minPayStr) ?? 0) > 0
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Debt") {
                    TextField("Name (e.g. Credit Card)", text: $name)
                }
                Section("Amounts") {
                    HStack {
                        Text("Balance").frame(maxWidth: .infinity, alignment: .leading)
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $balanceStr).keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing).frame(width: 100)
                    }
                    HStack {
                        Text("Interest rate").frame(maxWidth: .infinity, alignment: .leading)
                        TextField("e.g. 18.5", text: $rateStr).keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing).frame(width: 80)
                        Text("%").foregroundStyle(.secondary)
                    }
                    HStack {
                        Text("Minimum payment").frame(maxWidth: .infinity, alignment: .leading)
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $minPayStr).keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing).frame(width: 100)
                    }
                }
            }
            .navigationTitle("Add Debt")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        vm.create(userId: userId, name: name,
                                  balance: Double(balanceStr) ?? 0,
                                  interestRate: Double(rateStr) ?? 0,
                                  minimumPayment: Double(minPayStr) ?? 0) { dismiss() }
                    }
                    .disabled(!canSave).bold()
                }
            }
        }
    }
}
