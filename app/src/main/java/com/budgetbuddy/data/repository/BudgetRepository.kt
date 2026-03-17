package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.BudgetDao
import com.budgetbuddy.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun getBudgetsForMonth(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(userId, month, year)

    suspend fun getBudgetForCategory(userId: String, categoryId: Long, month: Int, year: Int): BudgetEntity? =
        budgetDao.getBudgetForCategory(userId, categoryId, month, year)

    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long =
        budgetDao.insertOrUpdateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)
}
