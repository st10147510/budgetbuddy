import Foundation

struct CategorySpend: Identifiable {
    var id   = UUID()
    let name: String
    let icon: String
    let amount: Double
    let colorHex: String
}

struct MonthTotal: Identifiable {
    var id = UUID()
    let label: String
    let total: Double
}

struct CategoryBudgetBar: Identifiable {
    var id = UUID()
    let categoryName: String
    let icon: String
    let colorHex: String
    let spent: Double
    let minAmount: Double
    let limitAmount: Double
}

struct ReportsUiState {
    var balance: Double              = 0
    var totalExpense: Double         = 0
    var totalIncome: Double          = 0
    var transactions: [BBTransaction] = []
    var categorySpends: [CategorySpend] = []
    var categoryBudgetBars: [CategoryBudgetBar] = []
    var monthlyTotals: [MonthTotal]  = []
    var isLoading: Bool              = false
}

@MainActor
final class ReportsViewModel: ObservableObject {
    @Published var uiState = ReportsUiState()
    @Published var selectedMonth: Int  = Calendar.current.component(.month, from: .now) - 1  // 0-based
    @Published var selectedYear: Int   = Calendar.current.component(.year,  from: .now)

    private let transactionRepo: TransactionRepository
    private let budgetRepo: BudgetRepository
    private let categoryRepo: CategoryRepository
    private var userId = ""

    init(transactionRepo: TransactionRepository, budgetRepo: BudgetRepository,
         categoryRepo: CategoryRepository) {
        self.transactionRepo = transactionRepo
        self.budgetRepo      = budgetRepo
        self.categoryRepo    = categoryRepo
    }

    func load(userId: String) {
        self.userId = userId
        loadSelectedMonth()
        loadMonthlyTotals()
    }

    func selectMonth(_ month: Int, year: Int) {
        selectedMonth = month
        selectedYear  = year
        loadSelectedMonth()
    }

    private func loadSelectedMonth() {
        guard !userId.isEmpty else { return }
        uiState.isLoading = true
        Task {
            let (start, end) = monthRange(month: selectedMonth, year: selectedYear)
            let dbMonth      = selectedMonth + 1   // 0-based → 1-based

            do {
                let allCats    = try categoryRepo.all()
                let catMap     = Dictionary(uniqueKeysWithValues: allCats.map { ($0.id, $0) })
                let txs        = try transactionRepo.forDateRange(userId: userId, start: start, end: end)
                let budgets    = try budgetRepo.forMonth(userId: userId, month: dbMonth, year: selectedYear)

                let expenses   = txs.filter { $0.type == .expense }
                let incomes    = txs.filter { $0.type == .income  }
                let total      = expenses.reduce(0) { $0 + $1.amount }
                let income     = incomes.reduce(0)  { $0 + $1.amount }

                let spendByCat = Dictionary(grouping: expenses, by: { $0.categoryId })
                    .mapValues { $0.reduce(0) { $0 + $1.amount } }

                let catSpends: [CategorySpend] = spendByCat
                    .compactMap { (catId, amount) in
                        guard let cid = catId, let cat = catMap[cid] else { return nil }
                        return CategorySpend(name: cat.name, icon: cat.icon, amount: amount, colorHex: cat.colorHex)
                    }
                    .sorted { $0.amount > $1.amount }

                let bars: [CategoryBudgetBar] = budgets.compactMap { budget in
                    let cat = catMap[budget.categoryId]
                    return CategoryBudgetBar(
                        categoryName: cat?.name    ?? "Other",
                        icon:         cat?.icon    ?? "📦",
                        colorHex:     cat?.colorHex ?? "#607D8B",
                        spent:        spendByCat[budget.categoryId] ?? 0,
                        minAmount:    budget.minAmount,
                        limitAmount:  budget.limitAmount
                    )
                }.sorted { $0.spent > $1.spent }

                uiState = ReportsUiState(
                    balance:             income - total,
                    totalExpense:        total,
                    totalIncome:         income,
                    transactions:        txs.sorted { $0.date > $1.date },
                    categorySpends:      catSpends,
                    categoryBudgetBars:  bars,
                    monthlyTotals:       uiState.monthlyTotals,
                    isLoading:           false
                )
            } catch {
                uiState.isLoading = false
            }
        }
    }

    private func loadMonthlyTotals() {
        guard !userId.isEmpty else { return }
        Task {
            let labels = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]
            var totals: [MonthTotal] = []
            for i in stride(from: 5, through: 0, by: -1) {
                let date = Calendar.current.date(byAdding: .month, value: -i, to: .now)!
                let m    = Calendar.current.component(.month, from: date) - 1  // 0-based
                let y    = Calendar.current.component(.year,  from: date)
                let (start, end) = monthRange(month: m, year: y)
                let income  = (try? transactionRepo.totalIncome(userId: userId,  start: start, end: end)) ?? 0
                let expense = (try? transactionRepo.totalExpense(userId: userId, start: start, end: end)) ?? 0
                totals.append(MonthTotal(label: labels[m], total: income - expense))
            }
            uiState.monthlyTotals = totals
        }
    }

    private func monthRange(month: Int, year: Int) -> (Date, Date) {
        var cal = Calendar.current
        var comps = DateComponents(year: year, month: month + 1, day: 1, hour: 0, minute: 0, second: 0)
        let start = cal.date(from: comps) ?? .now
        comps.day = cal.range(of: .day, in: .month, for: start)!.count
        comps.hour = 23; comps.minute = 59; comps.second = 59
        let end = cal.date(from: comps) ?? .now
        return (start, end)
    }
}
