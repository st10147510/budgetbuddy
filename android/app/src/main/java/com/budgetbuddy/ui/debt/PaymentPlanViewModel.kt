package com.budgetbuddy.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.local.entities.PayoffStrategy
import com.budgetbuddy.data.repository.DebtPayoffMonth
import com.budgetbuddy.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PlanSummary(
    val strategy: PayoffStrategy,
    val totalMonths: Int,
    val totalInterestPaid: Double,
    val totalPaid: Double
)

data class PaymentPlanUiState(
    val snowball: PlanSummary? = null,
    val avalanche: PlanSummary? = null,
    val selectedStrategy: PayoffStrategy? = null,
    val schedule: List<ScheduleRow> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)

sealed class ScheduleRow {
    data class Header(val month: Int) : ScheduleRow()
    data class DebtRow(
        val debtName: String,
        val payment: Double,
        val remainingBalance: Double
    ) : ScheduleRow()
}

@HiltViewModel
class PaymentPlanViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentPlanUiState())
    val uiState: StateFlow<PaymentPlanUiState> = _uiState.asStateFlow()

    private var cachedDebts: List<DebtEntity> = emptyList()
    private var cachedSnowball: List<DebtPayoffMonth> = emptyList()
    private var cachedAvalanche: List<DebtPayoffMonth> = emptyList()

    fun loadDebts(userId: String) {
        viewModelScope.launch {
            debtRepository.getActiveDebts(userId).collect { debts ->
                cachedDebts = debts
                _uiState.value = _uiState.value.copy(isEmpty = debts.isEmpty())
            }
        }
    }

    fun calculatePlan(extraPayment: Double) {
        if (cachedDebts.isEmpty()) {
            _uiState.value = _uiState.value.copy(isEmpty = true)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val (snowballSchedule, avalancheSchedule) = withContext(computeDispatcher) {
                val s = debtRepository.computePayoffSchedule(cachedDebts, PayoffStrategy.SNOWBALL, extraPayment)
                val a = debtRepository.computePayoffSchedule(cachedDebts, PayoffStrategy.AVALANCHE, extraPayment)
                s to a
            }
            cachedSnowball = snowballSchedule
            cachedAvalanche = avalancheSchedule

            val totalPrincipal = cachedDebts.sumOf { it.balance }
            val snowballSummary = buildSummary(PayoffStrategy.SNOWBALL, snowballSchedule, totalPrincipal)
            val avalancheSummary = buildSummary(PayoffStrategy.AVALANCHE, avalancheSchedule, totalPrincipal)

            _uiState.value = _uiState.value.copy(
                snowball = snowballSummary,
                avalanche = avalancheSummary,
                isLoading = false
            )
        }
    }

    fun selectStrategy(strategy: PayoffStrategy) {
        val raw = if (strategy == PayoffStrategy.SNOWBALL) cachedSnowball else cachedAvalanche
        val rows = buildScheduleRows(raw)
        _uiState.value = _uiState.value.copy(selectedStrategy = strategy, schedule = rows)
    }

    private fun buildSummary(
        strategy: PayoffStrategy,
        schedule: List<DebtPayoffMonth>,
        totalPrincipal: Double
    ): PlanSummary {
        if (schedule.isEmpty()) return PlanSummary(strategy, 0, 0.0, 0.0)
        val totalMonths = schedule.maxOf { it.month }
        val totalPaid = schedule.sumOf { it.payment }
        val totalInterest = maxOf(0.0, totalPaid - totalPrincipal)
        return PlanSummary(strategy, totalMonths, totalInterest, totalPaid)
    }

    private fun buildScheduleRows(schedule: List<DebtPayoffMonth>): List<ScheduleRow> {
        val rows = mutableListOf<ScheduleRow>()
        var lastMonth = -1
        for (entry in schedule) {
            if (entry.month != lastMonth) {
                rows.add(ScheduleRow.Header(entry.month))
                lastMonth = entry.month
            }
            rows.add(ScheduleRow.DebtRow(entry.debtName, entry.payment, entry.remainingBalance))
        }
        return rows
    }
}
