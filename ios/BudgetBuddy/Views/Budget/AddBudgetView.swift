import SwiftUI

struct AddBudgetView: View {
    let userId: String
    @ObservedObject var vm: BudgetViewModel
    @Environment(\.dismiss) var dismiss

    @State private var selectedCategoryId: UUID?
    @State private var limitStr = ""
    @State private var minStr   = ""

    private var canSave: Bool { selectedCategoryId != nil && (Double(limitStr) ?? 0) > 0 }

    var body: some View {
        NavigationStack {
            Form {
                Section("Category") {
                    Picker("Category", selection: $selectedCategoryId) {
                        Text("Select…").tag(UUID?.none)
                        ForEach(vm.categories) { cat in
                            Label(cat.name, systemImage: "circle.fill").tag(UUID?.some(cat.id))
                        }
                    }
                }

                Section("Spending Limits") {
                    HStack {
                        Text("Limit (max)").frame(maxWidth: .infinity, alignment: .leading)
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $limitStr).keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing).frame(width: 100)
                    }
                    HStack {
                        Text("Minimum goal").frame(maxWidth: .infinity, alignment: .leading)
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $minStr).keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing).frame(width: 100)
                    }
                }

                Section {
                    Text("⚠️ Bumping the version will prompt all users to re-accept.")
                        .font(.caption).foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Add Budget")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { save() }.disabled(!canSave).bold()
                }
            }
        }
    }

    private func save() {
        guard let catId = selectedCategoryId,
              let limit = Double(limitStr) else { return }
        vm.save(userId: userId, categoryId: catId, limitAmount: limit,
                minAmount: Double(minStr) ?? 0) { dismiss() }
    }
}
