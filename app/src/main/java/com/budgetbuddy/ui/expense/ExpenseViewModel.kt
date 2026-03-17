package com.budgetbuddy.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.data.repository.BadgeRepository
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExpenseUiState {
    object Idle : ExpenseUiState()
    object Loading : ExpenseUiState()
    object Saved : ExpenseUiState()
    object Deleted : ExpenseUiState()
    data class Error(val message: String) : ExpenseUiState()
}

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val badgeRepository: BadgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Idle)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    val categories = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var _currentTransaction: TransactionEntity? = null

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _currentTransaction = transactionRepository.getTransactionById(id)
        }
    }

    fun getCurrentTransaction() = _currentTransaction

    fun saveExpense(
        userId: String,
        amount: Double,
        categoryId: Long,
        date: Long,
        notes: String?,
        receiptPath: String?,
        type: TransactionType = TransactionType.EXPENSE,
        existingId: Long = -1
    ) {
        if (amount <= 0) { _uiState.value = ExpenseUiState.Error("Please enter a valid amount"); return }
        if (categoryId <= 0) { _uiState.value = ExpenseUiState.Error("Please select a category"); return }

        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val transaction = TransactionEntity(
                    id = if (existingId > 0) existingId else 0,
                    userId = userId,
                    amount = amount,
                    categoryId = categoryId,
                    date = date,
                    notes = notes?.takeIf { it.isNotBlank() },
                    receiptImagePath = receiptPath,
                    type = type
                )
                if (existingId > 0) transactionRepository.updateTransaction(transaction)
                else transactionRepository.insertTransaction(transaction)
                badgeRepository.checkAndAwardBadges(userId)
                _uiState.value = ExpenseUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to save expense")
            }
        }
    }

    fun deleteExpense(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            transactionRepository.deleteTransaction(transaction)
            _uiState.value = ExpenseUiState.Deleted
        }
    }

    fun resetState() { _uiState.value = ExpenseUiState.Idle }
}
