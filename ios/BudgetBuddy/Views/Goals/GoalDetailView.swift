import SwiftUI

struct GoalDetailView: View {
    let goal: BBGoal
    @ObservedObject var vm: GoalsViewModel
    let userId: String
    @Environment(\.dismiss) var dismiss

    @State private var depositStr = ""
    @State private var showDeposit = false

    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 12) {
                    Text(goal.name).font(.title2.bold())
                    ProgressView(value: Double(goal.progressPercent) / 100).tint(.teal)
                    HStack {
                        Text("\(goal.savedAmount.currencyFormatted) saved")
                        Spacer()
                        Text("\(goal.targetAmount.currencyFormatted) target")
                    }
                    .font(.subheadline)
                }
                .padding(.vertical, 4)
            }

            if let date = goal.targetDate {
                Section("Target Date") {
                    Text(date.formatted(style: .long))
                }
            }

            if !goal.isCompleted {
                Section {
                    Button("Add Savings") { showDeposit = true }
                        .foregroundColor(.teal)
                }
            }

            Section {
                Button("Delete Goal", role: .destructive) {
                    vm.delete(goal); dismiss()
                }
            }
        }
        .navigationTitle("Goal")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showDeposit) {
            DepositSheet(goal: goal, vm: vm)
        }
    }
}

private struct DepositSheet: View {
    let goal: BBGoal
    @ObservedObject var vm: GoalsViewModel
    @Environment(\.dismiss) var dismiss
    @State private var amountStr = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Add savings to \(goal.name)") {
                    HStack {
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $amountStr).keyboardType(.decimalPad)
                    }
                }
            }
            .navigationTitle("Add Savings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        if let amt = Double(amountStr), amt > 0 {
                            vm.addSavings(goal: goal, amount: amt)
                            dismiss()
                        }
                    }.bold()
                }
            }
        }
    }
}
