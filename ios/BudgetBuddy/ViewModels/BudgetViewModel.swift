import Foundation

enum BudgetStatus { case ok, warning, exceeded, underMin }

struct BudgetWithSpend: Identifiable {
    var id: UUID { budget.id }
    let budget: BBBudget
    let category: BBCategory
    let spent: Double
    let progressPercent: Int
    let status: BudgetStatus
}

@MainActor
final class BudgetViewModel: ObservableObject {
    @Published var budgetsWithSpend: [BudgetWithSpend] = []
    @Published var categories: [BBCategory]            = []
    @Published var isLoading  = false
    @Published var saveResult: Bool?
    @Published var error: String?

    private let budgetRepo:      BudgetRepository
    private let categoryRepo:    CategoryRepository
    private let transactionRepo: TransactionRepository

    init(budgetRepo: BudgetRepository, categoryRepo: CategoryRepository,
         transactionRepo: TransactionRepository) {
        self.budgetRepo      = budgetRepo
        self.categoryRepo    = categoryRepo
        self.transactionRepo = transactionRepo
    }

    func load(userId: String) {
        isLoading = true
        Task {
            do {
                let cal   = Calendar.current
                let month = cal.component(.month, from: .now)
                let year  = cal.component(.year,  from: .now)
                let start = DateUtils.startOfMonth()
                let end   = DateUtils.endOfMonth()

                let budgets    = try budgetRepo.forMonth(userId: userId, month: month, year: year)
                let allCats    = try categoryRepo.all()
                let catMap     = Dictionary(uniqueKeysWithValues: allCats.map { ($0.id, $0) })
                categories     = allCats

                let transactions = try transactionRepo.forDateRange(userId: userId, start: start, end: end)
                let spendByCat   = Dictionary(grouping: transactions.filter { $0.type == .expense }, by: { $0.categoryId })
                    .mapValues { $0.reduce(0) { $0 + $1.amount } }

                budgetsWithSpend = budgets.compactMap { budget in
                    guard let cat = catMap[budget.categoryId] else { return nil }
                    let spent = spendByCat[budget.categoryId] ?? 0
                    let pct   = budget.limitAmount > 0
                        ? min(150, Int((spent / budget.limitAmount) * 100)) : 0
                    let status: BudgetStatus = pct >= 100 ? .exceeded
                                             : pct >= 80  ? .warning
                                             : (budget.minAmount > 0 && spent < budget.minAmount) ? .underMin
                                             : .ok
                    return BudgetWithSpend(budget: budget, category: cat, spent: spent,
                                           progressPercent: pct, status: status)
                }
            } catch { self.error = error.localizedDescription }
            isLoading = false
        }
    }

    func save(userId: String, categoryId: UUID, limitAmount: Double,
              minAmount: Double = 0, completion: (() -> Void)? = nil) {
        Task {
            do {
                let cal   = Calendar.current
                try await budgetRepo.insertOrUpdate(userId: userId, categoryId: categoryId,
                                                    limitAmount: limitAmount, minAmount: minAmount,
                                                    month: cal.component(.month, from: .now),
                                                    year:  cal.component(.year,  from: .now))
                saveResult = true
                load(userId: userId)
                completion?()
            } catch {
                saveResult  = false
                self.error  = error.localizedDescription
            }
        }
    }

    func delete(_ budget: BBBudget, userId: String) {
        Task {
            try? await budgetRepo.delete(budget)
            load(userId: userId)
        }
    }
}
