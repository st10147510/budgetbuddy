package com.budgetbuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.repository.BudgetRepository
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalSpendThisMonth: Double = 0.0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _userId = MutableStateFlow("")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val categories = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun init(userId: String) {
        _userId.value = userId
        loadRecentTransactions(userId)
        loadMonthlyTotal(userId)
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
            val total = transactionRepository.getTotalExpenseForPeriod(userId, start, end)
            _uiState.update { it.copy(totalSpendThisMonth = total) }
        }
    }

    fun refresh(userId: String) {
        loadRecentTransactions(userId)
        loadMonthlyTotal(userId)
    }
}
