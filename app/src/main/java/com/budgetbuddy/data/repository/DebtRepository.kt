package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.dao.DebtDao
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.local.entities.PayoffStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DebtRepo"

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
        try { firestoreRepository.saveDebt(debt.userId, debt.copy(id = id)) }
        catch (e: Exception) { Log.w(TAG, "saveDebt failed: ${e.message}") }
        return id
    }

    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
        try { firestoreRepository.saveDebt(debt.userId, debt) }
        catch (e: Exception) { Log.w(TAG, "updateDebt failed: ${e.message}") }
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        debtDao.deleteDebt(debt)
        try { firestoreRepository.deleteDebt(debt.userId, debt.id) }
        catch (e: Exception) { Log.w(TAG, "deleteDebt failed: ${e.message}") }
    }

    suspend fun markDebtPaidOff(id: Long) {
        debtDao.markDebtPaidOff(id)
        try { debtDao.getDebtById(id)?.let { firestoreRepository.saveDebt(it.userId, it.copy(isPaidOff = true)) } }
        catch (e: Exception) { Log.w(TAG, "markDebtPaidOff sync failed: ${e.message}") }
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

        while (balances.any { it > 0 } && month <= 360) {
            // Freed minimums from fully-paid debts roll over to the current target (the snowball/avalanche cascade)
            val freedMinimums = sorted.indices.filter { balances[it] <= 0 }.sumOf { sorted[it].minimumPayment }
            val targetIndex = balances.indexOfFirst { it > 0 }
            val targetBonus = extraPayment + freedMinimums

            sorted.forEachIndexed { index, debt ->
                if (balances[index] <= 0) return@forEachIndexed
                val monthlyRate = debt.interestRate / 100.0 / 12.0
                val interest = balances[index] * monthlyRate
                val payment = if (index == targetIndex) debt.minimumPayment + targetBonus else debt.minimumPayment
                val actualPayment = minOf(payment, balances[index] + interest)
                balances[index] = maxOf(0.0, balances[index] + interest - actualPayment)
                schedule.add(DebtPayoffMonth(month, debt.name, actualPayment, balances[index]))
            }
            month++
        }
        return schedule
    }
}
