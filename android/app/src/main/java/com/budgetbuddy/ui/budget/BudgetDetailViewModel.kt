package com.budgetbuddy.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.BudgetEntity
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.data.repository.BudgetRepository
import com.budgetbuddy.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetDetailState(
    val budget: BudgetEntity? = null,
    val category: CategoryEntity? = null,
    val spent: Double = 0.0
)

@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetDetailState())
    val state: StateFlow<BudgetDetailState> = _state.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun load(id: Long, spent: Double) {
        viewModelScope.launch {
            val budget = budgetRepository.getBudgetById(id) ?: return@launch
            val category = categoryRepository.getCategoryById(budget.categoryId)
            _state.value = BudgetDetailState(budget, category, spent)
        }
    }

    fun update(limitAmount: Double, minAmount: Double) {
        val current = _state.value.budget ?: return
        viewModelScope.launch {
            val updated = current.copy(limitAmount = limitAmount, minAmount = minAmount)
            budgetRepository.insertOrUpdateBudget(updated)
            _state.value = _state.value.copy(budget = updated)
        }
    }

    fun delete() {
        val current = _state.value.budget ?: return
        viewModelScope.launch {
            budgetRepository.deleteBudget(current)
            _finished.value = true
        }
    }
}
