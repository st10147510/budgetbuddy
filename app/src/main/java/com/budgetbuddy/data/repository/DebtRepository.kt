package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.DebtDao
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.local.entities.PayoffStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class DebtPayoffMonth(
    val month: Int,
    val debtName: String,
    val payment: Double,
    val remainingBalance: Double
)

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao,
    private val firestoreRepository: FirestoreRepository
) {

    fun getActiveDebts(userId: String): Flow<List<DebtEntity>> = debtDao.getActiveDebts(userId)
    fun getAllDebts(userId: String): Flow<List<DebtEntity>> = debtDao.getAllDebts(userId)

    suspend fun insertDebt(debt: DebtEntity): Long {
        val id = debtDao.insertDebt(debt)
        firestoreRepository.saveDebt(debt.userId, debt.copy(id = id))
        return id
    }

    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
        firestoreRepository.saveDebt(debt.userId, debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        debtDao.deleteDebt(debt)
        firestoreRepository.deleteDebt(debt.userId, debt.id)
    }

    suspend fun markDebtPaidOff(id: Long) {
        debtDao.markDebtPaidOff(id)
        debtDao.getDebtById(id)?.let { firestoreRepository.saveDebt(it.userId, it.copy(isPaidOff = true)) }
    }

    /**
     * Compute payoff schedule for a list of debts using snowball or avalanche strategy.
     * Returns a flat list of payments per month (simplified schedule).
     */
    fun computePayoffSchedule(debts: List<DebtEntity>, strategy: PayoffStrategy, extraPayment: Double = 0.0): List<DebtPayoffMonth> {
        if (debts.isEmpty()) return emptyList()

        val sorted = when (strategy) {
            PayoffStrategy.SNOWBALL  -> debts.sortedBy { it.balance }
            PayoffStrategy.AVALANCHE -> debts.sortedByDescending { it.interestRate }
        }

        val balances = sorted.map { it.balance }.toMutableList()
        val schedule = mutableListOf<DebtPayoffMonth>()
        var month = 1
        val totalMinimum = sorted.sumOf { it.minimumPayment }

        while (balances.any { it > 0 } && month <= 360) {
            var extra = extraPayment
            sorted.forEachIndexed { index, debt ->
                if (balances[index] <= 0) return@forEachIndexed
                val monthlyRate = debt.interestRate / 100.0 / 12.0
                val interest = balances[index] * monthlyRate
                var payment = debt.minimumPayment
                if (index == sorted.indexOfFirst { balances[sorted.indexOf(it)] > 0 }) {
                    payment += extra
                }
                val actualPayment = minOf(payment, balances[index] + interest)
                balances[index] = maxOf(0.0, balances[index] + interest - actualPayment)
                schedule.add(DebtPayoffMonth(month, debt.name, actualPayment, balances[index]))
            }
            month++
        }
        return schedule
    }
}
