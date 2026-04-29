package com.budgetbuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.repository.BudgetRepository
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.GoalRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val balance: Double = 0.0,             // income - expenses (all time)
    val totalIncomeThisMonth: Double = 0.0,
    val totalSpendThisMonth: Double = 0.0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _userId = MutableStateFlow("")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val categories = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _goalsState = MutableStateFlow<List<com.budgetbuddy.data.local.entities.GoalEntity>>(emptyList())
    val goals: StateFlow<List<com.budgetbuddy.data.local.entities.GoalEntity>> = _goalsState.asStateFlow()

    fun init(userId: String) {
        _userId.value = userId
        loadRecentTransactions(userId)
        loadMonthlyTotal(userId)
        loadGoals(userId)
    }

    private fun loadGoals(userId: String) {
        viewModelScope.launch {
            goalRepository.getActiveGoals(userId).collect { goals ->
                _goalsState.value = goals
            }
        }
    }

    private fun loadRecentTransactions(userId: String) {
        viewModelScope.launch {
            transactionRepository.getRecentTransactions(userId, 5).collect { transactions ->
                _uiState.update { it.copy(recentTransactions = transactions) }
            }
        }
    }

    private fun loadMonthlyTotal(userId: String) {
        viewModelScope.launch {
            val start = DateUtils.startOfMonth()
            val end = DateUtils.endOfMonth()
            // Load both income and expense for the month so balance = income - expenses
            val expense = transactionRepository.getTotalExpenseForPeriod(userId, start, end)
            val income  = transactionRepository.getTotalIncomeForPeriod(userId, start, end)
            val balance = income - expense
            _uiState.update {
                it.copy(
                    totalSpendThisMonth = expense,
                    totalIncomeThisMonth = income,
                    balance = balance
                )
            }
        }
    }

    fun refresh(userId: String) {
        loadRecentTransactions(userId)
        loadMonthlyTotal(userId)
    }
}
