package com.budgetbuddy.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtDetailViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {

    private val _debt = MutableStateFlow<DebtEntity?>(null)
    val debt: StateFlow<DebtEntity?> = _debt.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch { _debt.value = debtRepository.getDebtById(id) }
    }

    fun update(name: String, interestRate: Double, minimumPayment: Double) {
        val current = _debt.value ?: return
        viewModelScope.launch {
            val updated = current.copy(name = name, interestRate = interestRate, minimumPayment = minimumPayment)
            debtRepository.updateDebt(updated)
            _debt.value = updated
        }
    }

    fun makePayment(amount: Double) {
        val current = _debt.value ?: return
        if (amount <= 0) return
        viewModelScope.launch {
            val newBalance = maxOf(0.0, current.balance - amount)
            val updated = current.copy(balance = newBalance, isPaidOff = newBalance == 0.0)
            debtRepository.updateDebt(updated)
            _debt.value = updated
        }
    }

    fun delete() {
        val current = _debt.value ?: return
        viewModelScope.launch {
            debtRepository.deleteDebt(current)
            _finished.value = true
        }
    }
}
