import SwiftUI
import PhotosUI

struct AddTransactionView: View {
    let userId: String
    @ObservedObject var vm: ExpenseViewModel
    @Environment(\.dismiss) var dismiss

    @State private var type: TransactionType = .expense
    @State private var amountStr = ""
    @State private var selectedCategoryId: UUID?
    @State private var date = Date()
    @State private var notes = ""
    @State private var photosItem: PhotosPickerItem?
    @State private var receiptImage: UIImage?

    private var amount: Double { Double(amountStr) ?? 0 }
    private var canSave: Bool  { amount > 0 }

    var body: some View {
        NavigationStack {
            Form {
                // Type picker
                Section {
                    Picker("Type", selection: $type) {
                        Text("Expense").tag(TransactionType.expense)
                        Text("Income").tag(TransactionType.income)
                    }
                    .pickerStyle(.segmented)
                }

                // Amount
                Section("Amount") {
                    HStack {
                        Text("R").foregroundStyle(.secondary)
                        TextField("0.00", text: $amountStr)
                            .keyboardType(.decimalPad)
                    }
                }

                // Category
                Section("Category") {
                    if vm.categories.isEmpty {
                        Text("No categories available").foregroundStyle(.secondary)
                    } else {
                        Picker("Category", selection: $selectedCategoryId) {
                            Text("None").tag(UUID?.none)
                            ForEach(vm.categories) { cat in
                                Label(cat.name, systemImage: "circle.fill")
                                    .tag(UUID?.some(cat.id))
                            }
                        }
                    }
                }

                // Date & notes
                Section("Details") {
                    DatePicker("Date", selection: $date, displayedComponents: .date)
                    TextField("Notes (optional)", text: $notes, axis: .vertical)
                        .lineLimit(3)
                }

                // Receipt photo
                Section("Receipt") {
                    PhotosPicker(selection: $photosItem, matching: .images) {
                        Label(receiptImage == nil ? "Attach Receipt" : "Change Receipt",
                              systemImage: "camera")
                            .foregroundColor(.teal)
                    }
                    if let img = receiptImage {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFit()
                            .frame(maxHeight: 160)
                            .cornerRadius(10)
                    }
                }
            }
            .navigationTitle(type == .expense ? "Add Expense" : "Add Income")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { save() }
                        .disabled(!canSave || vm.isLoading)
                        .bold()
                }
            }
            .onChange(of: photosItem) { _, item in
                Task {
                    if let data = try? await item?.loadTransferable(type: Data.self),
                       let img  = UIImage(data: data) {
                        receiptImage = img
                    }
                }
            }
        }
    }

    private func save() {
        vm.save(userId: userId, amount: amount, categoryId: selectedCategoryId,
                date: date, notes: notes, type: type, receiptImage: receiptImage)
        dismiss()
    }
}
