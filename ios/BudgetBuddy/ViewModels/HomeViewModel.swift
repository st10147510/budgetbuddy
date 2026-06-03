import Foundation
import Combine

struct HomeUiState {
    var balance: Double               = 0
    var totalIncomeThisMonth: Double  = 0
    var totalSpendThisMonth: Double   = 0
    var allTimeIncome: Double         = 0
    var allTimeExpense: Double        = 0
    var recentTransactions: [BBTransaction] = []
    var activeGoals: [BBGoal]         = []
    var categories: [BBCategory]      = []
    var isLoading: Bool               = false
    var error: String?                = nil
}

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var uiState = HomeUiState()

    private let transactionRepo: TransactionRepository
    private let budgetRepo: BudgetRepository
    private let goalRepo: GoalRepository
    private let categoryRepo: CategoryRepository

    init(transactionRepo: TransactionRepository, budgetRepo: BudgetRepository,
         goalRepo: GoalRepository, categoryRepo: CategoryRepository) {
        self.transactionRepo = transactionRepo
        self.budgetRepo      = budgetRepo
        self.goalRepo        = goalRepo
        self.categoryRepo    = categoryRepo
    }

    func load(userId: String) {
        uiState.isLoading = true
        Task {
            do {
                let categories = try categoryRepo.all()
                let recent     = try transactionRepo.recent(userId: userId, limit: 5)
                let goals      = try goalRepo.active(userId: userId)

                let start      = DateUtils.startOfMonth()
                let end        = DateUtils.endOfMonth()
                let income     = try transactionRepo.totalIncome(userId: userId, start: start, end: end)
                let expense    = try transactionRepo.totalExpense(userId: userId, start: start, end: end)

                let allTxs     = try transactionRepo.all(userId: userId)
                let allIncome  = allTxs.filter { $0.type == .income  }.reduce(0) { $0 + $1.amount }
                let allExpense = allTxs.filter { $0.type == .expense }.reduce(0) { $0 + $1.amount }

                uiState = HomeUiState(
                    balance:               income - expense,
                    totalIncomeThisMonth:  income,
                    totalSpendThisMonth:   expense,
                    allTimeIncome:         allIncome,
                    allTimeExpense:        allExpense,
                    recentTransactions:    recent,
                    activeGoals:           goals,
                    categories:            categories,
                    isLoading:             false
                )
            } catch {
                uiState.isLoading = false
                uiState.error     = error.localizedDescription
            }
        }
    }
}
