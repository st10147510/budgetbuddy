package com.budgetbuddy.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.data.repository.BudgetRepository
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategorySpend(val name: String, val icon: String, val amount: Double, val colorHex: String)

data class MonthTotal(val label: String, val total: Float)

data class CategoryBudgetBar(
    val categoryName: String,
    val icon: String,
    val colorHex: String,
    val spent: Double,
    val minAmount: Double,
    val limitAmount: Double
)

data class ReportsUiState(
    val balance: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val transactions: List<TransactionEntity> = emptyList(),
    val categorySpends: List<CategorySpend> = emptyList(),
    val categoryBudgetBars: List<CategoryBudgetBar> = emptyList(),
    val monthlyTotals: List<MonthTotal> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    val categories = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var userId: String = ""
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) // 0-based
    private var monthJob: kotlinx.coroutines.Job? = null

    fun loadReports(userId: String) {
        this.userId = userId
        loadSelectedMonth()
        loadMonthlyTotals()
    }

    fun selectMonth(month: Int, year: Int) {
        selectedMonth = month
        selectedYear = year
        loadSelectedMonth()
    }

    private fun loadSelectedMonth() {
        if (userId.isEmpty()) return
        monthJob?.cancel()
        monthJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val cal = Calendar.getInstance().apply { set(selectedYear, selectedMonth, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
            val start = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
            val end = cal.timeInMillis

            // selectedMonth is 0-based (Calendar.MONTH); DB stores months 1-based
            val dbMonth = selectedMonth + 1

            combine(
                transactionRepository.getTransactionsByDateRange(userId, start, end),
                budgetRepository.getBudgetsForMonth(userId, dbMonth, selectedYear)
            ) { transactions, budgets ->
                val cats = categoryRepository.getAllCategories().first()
                val catMap = cats.associateBy { it.id }
                val budgetMap = budgets.associateBy { it.categoryId }

                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val incomes  = transactions.filter { it.type == TransactionType.INCOME }
                val totalExpense = expenses.sumOf { it.amount }
                val totalIncome  = incomes.sumOf { it.amount }

                val spendByCat = expenses.groupBy { it.categoryId }
                    .mapValues { (_, txs) -> txs.sumOf { it.amount } }

                val categorySpends = spendByCat
                    .map { (catId, amount) ->
                        val cat = catMap[catId]
                        CategorySpend(cat?.name ?: "Other", cat?.icon ?: "📦", amount, cat?.colorHex ?: "#607D8B")
                    }
                    .sortedByDescending { it.amount }

                // Only include categories that have a budget set this month
                val categoryBudgetBars = budgets.map { budget ->
                    val cat = catMap[budget.categoryId]
                    CategoryBudgetBar(
                        categoryName = cat?.name ?: "Other",
                        icon = cat?.icon ?: "📦",
                        colorHex = cat?.colorHex ?: "#607D8B",
                        spent = spendByCat[budget.categoryId] ?: 0.0,
                        minAmount = budget.minAmount,
                        limitAmount = budget.limitAmount
                    )
                }.sortedByDescending { it.spent }

                ReportsUiState(
                    balance = totalIncome - totalExpense,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    transactions = transactions.sortedByDescending { it.date },
                    categorySpends = categorySpends,
                    categoryBudgetBars = categoryBudgetBars,
                    isLoading = false
                )
            }.collect { newState -> _uiState.update { newState } }
        }
    }

    private fun loadMonthlyTotals() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            val totals = mutableListOf<MonthTotal>()
            val monthLabels = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val cal = Calendar.getInstance()
            // last 6 months
            for (i in 5 downTo 0) {
                val c = Calendar.getInstance()
                c.add(Calendar.MONTH, -i)
                val m = c.get(Calendar.MONTH)
                val y = c.get(Calendar.YEAR)
                c.set(y, m, 1, 0, 0, 0); c.set(Calendar.MILLISECOND, 0)
                val start = c.timeInMillis
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59)
                val end = c.timeInMillis
                val expense = transactionRepository.getTotalExpenseForPeriod(userId, start, end)
                val income  = transactionRepository.getTotalIncomeForPeriod(userId, start, end)
                // Chart shows net balance per month (income - expense)
                totals.add(MonthTotal(monthLabels[m], (income - expense).toFloat()))
            }
            _uiState.update { it.copy(monthlyTotals = totals) }
        }
    }
}
