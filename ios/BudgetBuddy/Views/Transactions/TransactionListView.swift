import SwiftUI

struct TransactionListView: View {
    let userId: String
    @ObservedObject var vm: ExpenseViewModel
    @State private var showAdd   = false
    @State private var searchText = ""

    private var filtered: [BBTransaction] {
        guard !searchText.isEmpty else { return vm.transactions }
        return vm.transactions.filter {
            ($0.notes?.localizedCaseInsensitiveContains(searchText) ?? false)
            || (vm.category(for: $0)?.name.localizedCaseInsensitiveContains(searchText) ?? false)
        }
    }

    var body: some View {
        NavigationStack {
            Group {
                if vm.transactions.isEmpty && !vm.isLoading {
                    EmptyStateView(icon: "creditcard", message: "No transactions yet.\nTap + to record one.")
                } else {
                    List {
                        ForEach(filtered) { tx in
                            NavigationLink {
                                TransactionDetailView(transaction: tx, vm: vm, userId: userId)
                            } label: {
                                TransactionRowView(transaction: tx, category: vm.category(for: tx))
                            }
                        }
                        .onDelete { idxs in
                            idxs.forEach { vm.delete(filtered[$0]) }
                        }
                    }
                    .listStyle(.plain)
                    .searchable(text: $searchText, prompt: "Search transactions")
                }
            }
            .navigationTitle("Transactions")
            .refreshable { vm.loadAll(userId: userId) }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button { showAdd = true } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAdd, onDismiss: { vm.loadAll(userId: userId) }) {
                AddTransactionView(userId: userId, vm: vm)
            }
            .overlay {
                if vm.isLoading { ProgressView() }
            }
        }
    }
}
