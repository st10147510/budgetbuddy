import Foundation

@MainActor
final class DebtViewModel: ObservableObject {
    @Published var debts: [BBDebt]             = []
    @Published var payoffSchedule: [DebtPayoffMonth] = []
    @Published var isLoading  = false
    @Published var error: String?

    private let debtRepo: DebtRepository

    init(debtRepo: DebtRepository) { self.debtRepo = debtRepo }

    var activeDebts: [BBDebt] { debts.filter { !$0.isPaidOff } }
    var paidDebts:   [BBDebt] { debts.filter {  $0.isPaidOff } }

    var totalOwed: Double    { activeDebts.reduce(0) { $0 + $1.balance } }
    var totalMinPayment: Double { activeDebts.reduce(0) { $0 + $1.minimumPayment } }

    func load(userId: String) {
        isLoading = true
        Task {
            do   { debts = try debtRepo.all(userId: userId) }
            catch { self.error = error.localizedDescription }
            isLoading = false
        }
    }

    func create(userId: String, name: String, balance: Double,
                interestRate: Double, minimumPayment: Double, completion: (() -> Void)? = nil) {
        let debt = BBDebt(userId: userId, name: name, balance: balance,
                          interestRate: interestRate, minimumPayment: minimumPayment)
        Task {
            do {
                try await debtRepo.insert(debt)
                load(userId: userId)
                completion?()
            } catch { self.error = error.localizedDescription }
        }
    }

    func makePayment(debt: BBDebt, amount: Double, userId: String) {
        debt.balance = max(0, debt.balance - amount)
        if debt.balance == 0 { debt.isPaidOff = true }
        Task {
            try? await debtRepo.update(debt)
            load(userId: userId)
        }
    }

    func delete(_ debt: BBDebt) {
        Task {
            try? await debtRepo.delete(debt)
            debts.removeAll { $0.id == debt.id }
        }
    }

    func computePayoffSchedule(strategy: PayoffStrategy, extraPayment: Double = 0) {
        payoffSchedule = debtRepo.computePayoffSchedule(debts: activeDebts,
                                                         strategy: strategy,
                                                         extraPayment: extraPayment)
    }
}
