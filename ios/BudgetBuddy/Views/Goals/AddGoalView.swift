import SwiftUI

struct AddGoalView: View {
    let userId: String
    @ObservedObject var vm: GoalsViewModel
    @Environment(\.dismiss) var dismiss

    @State private var name       = ""
    @State private var targetStr  = ""
    @State private var hasDate    = false
    @State private var targetDate = Date()

    private var canSave: Bool { !name.isEmpty && (Double(targetStr) ?? 0) > 0 }

    var body: some View {
        NavigationStack {
            Form {
                Section("Goal") {
                    TextField("Name (e.g. Emergency Fund)", text: $name)
                    HStack {
                        Text("Target amount").frame(maxWidth: .infinity, alignment: .leading)
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $targetStr).keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing).frame(width: 100)
                    }
                }
                Section {
                    Toggle("Set target date", isOn: $hasDate)
                    if hasDate {
                        DatePicker("Target date", selection: $targetDate, displayedComponents: .date)
                    }
                }
            }
            .navigationTitle("New Goal")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        vm.create(userId: userId, name: name,
                                  targetAmount: Double(targetStr) ?? 0,
                                  targetDate: hasDate ? targetDate : nil) { dismiss() }
                    }
                    .disabled(!canSave).bold()
                }
            }
        }
    }
}
