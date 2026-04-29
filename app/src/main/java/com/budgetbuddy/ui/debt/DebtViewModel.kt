package com.budgetbuddy.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.local.entities.PayoffStrategy
import com.budgetbuddy.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {

    private val _debts = MutableStateFlow<List<DebtEntity>>(emptyList())
    val debts: StateFlow<List<DebtEntity>> = _debts.asStateFlow()

    private val _strategy = MutableStateFlow(PayoffStrategy.SNOWBALL)
    val strategy: StateFlow<PayoffStrategy> = _strategy.asStateFlow()

    fun loadDebts(userId: String) {
        viewModelScope.launch {
            debtRepository.getActiveDebts(userId).collect { _debts.value = it }
        }
    }

    fun setStrategy(strategy: PayoffStrategy) { _strategy.value = strategy }

    fun addDebt(userId: String, name: String, balance: Double, interestRate: Double, minPayment: Double) {
        viewModelScope.launch {
            debtRepository.insertDebt(
                DebtEntity(
                    userId = userId,
                    name = name,
                    originalBalance = balance, // snapshot of the starting balance for progress tracking
                    balance = balance,
                    interestRate = interestRate,
                    minimumPayment = minPayment
                )
            )
        }
    }

    /**
     * Records a payment against a debt:
     * - Reduces the current balance by [amount]
     * - Marks the debt as paid off if balance reaches zero
     */
    fun makePayment(debt: DebtEntity, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            val newBalance = maxOf(0.0, debt.balance - amount)
            val updatedDebt = debt.copy(balance = newBalance, isPaidOff = newBalance == 0.0)
            debtRepository.updateDebt(updatedDebt)
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch { debtRepository.deleteDebt(debt) }
    }
}
