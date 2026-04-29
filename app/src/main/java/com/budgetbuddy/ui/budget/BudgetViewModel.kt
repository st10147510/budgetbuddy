package com.budgetbuddy.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.BudgetEntity
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.data.repository.BudgetRepository
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetWithSpend(
    val budget: BudgetEntity,
    val category: CategoryEntity,
    val spent: Double,
    val progressPercent: Int,
    val status: BudgetStatus
)

enum class BudgetStatus { OK, WARNING, EXCEEDED }

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _budgetsWithSpend = MutableStateFlow<List<BudgetWithSpend>>(emptyList())
    val budgetsWithSpend: StateFlow<List<BudgetWithSpend>> = _budgetsWithSpend.asStateFlow()

    private val _saveState = MutableStateFlow<Boolean?>(null)
    val saveState: StateFlow<Boolean?> = _saveState.asStateFlow()

    private var budgetJob: kotlinx.coroutines.Job? = null

    fun loadBudgets(userId: String) {
        if (budgetJob?.isActive == true) return
        budgetJob = viewModelScope.launch {
            val month = DateUtils.currentMonth()
            val year  = DateUtils.currentYear()
            val start = DateUtils.startOfMonth()
            val end   = DateUtils.endOfMonth()

            combine(
                budgetRepository.getBudgetsForMonth(userId, month, year),
                transactionRepository.getTransactionsByDateRange(userId, start, end)
            ) { budgets, transactions ->
                val spendByCat = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .mapValues { (_, txs) -> txs.sumOf { it.amount } }
                budgets.mapNotNull { budget ->
                    val category = categoryRepository.getCategoryById(budget.categoryId) ?: return@mapNotNull null
                    val spent = spendByCat[budget.categoryId] ?: 0.0
                    val pct = if (budget.limitAmount > 0) ((spent / budget.limitAmount) * 100).toInt().coerceIn(0, 150) else 0
                    val status = when {
                        pct >= 100 -> BudgetStatus.EXCEEDED
                        pct >= 80  -> BudgetStatus.WARNING
                        else       -> BudgetStatus.OK
                    }
                    BudgetWithSpend(budget, category, spent, pct, status)
                }
            }.collect { _budgetsWithSpend.value = it }
        }
    }

    fun saveBudget(userId: String, categoryId: Long, limitAmount: Double) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                userId = userId,
                categoryId = categoryId,
                limitAmount = limitAmount,
                month = DateUtils.currentMonth(),
                year  = DateUtils.currentYear()
            )
            budgetRepository.insertOrUpdateBudget(budget)
            _saveState.value = true
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch { budgetRepository.deleteBudget(budget) }
    }
}
