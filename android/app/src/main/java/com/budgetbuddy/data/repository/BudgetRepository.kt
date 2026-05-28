package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.dao.BudgetDao
import com.budgetbuddy.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BudgetRepo"

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val firestoreRepository: FirestoreRepository
) {
    fun getBudgetsForMonth(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(userId, month, year)

    suspend fun getBudgetForCategory(userId: String, categoryId: Long, month: Int, year: Int): BudgetEntity? =
        budgetDao.getBudgetForCategory(userId, categoryId, month, year)

    suspend fun getBudgetById(id: Long): BudgetEntity? = budgetDao.getBudgetById(id)

    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long {
        val id = budgetDao.insertOrUpdateBudget(budget)
        try { firestoreRepository.saveBudget(budget.userId, budget.copy(id = id)) }
        catch (e: Exception) { Log.w(TAG, "saveBudget failed: ${e.message}") }
        return id
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
        try { firestoreRepository.deleteBudget(budget.userId, budget.id) }
        catch (e: Exception) { Log.w(TAG, "deleteBudget failed: ${e.message}") }
    }
}
