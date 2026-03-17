package com.budgetbuddy.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject



data class CategorySpend(val name: String, val icon: String, val amount: Double, val colorHex: String)

data class ReportsUiState(
    val categorySpends: List<CategorySpend> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun loadReports(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val start = DateUtils.startOfMonth()
            val end = DateUtils.endOfMonth()

            transactionRepository.getTransactionsByDateRange(userId, start, end).collect { transactions ->
                val categories = categoryRepository.getAllCategories().first()
                val catMap = categories.associateBy { it.id }

                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val incomes = transactions.filter { it.type == TransactionType.INCOME }

                val categorySpends = expenses
                    .groupBy { it.categoryId }
                    .map { (catId, txs) ->
                        val cat = catMap[catId]
                        CategorySpend(
                            name = cat?.name ?: "Other",
                            icon = cat?.icon ?: "📦",
                            amount = txs.sumOf { it.amount },
                            colorHex = cat?.colorHex ?: "#607D8B"
                        )
                    }
                    .sortedByDescending { it.amount }

                _uiState.update {
                    it.copy(
                        categorySpends = categorySpends,
                        totalExpense = expenses.sumOf { it.amount },
                        totalIncome = incomes.sumOf { it.amount },
                        isLoading = false
                    )
                }
            }
        }
    }
}
