package com.budgetbuddy.data.local.dao

import androidx.room.*
import com.budgetbuddy.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("""
        SELECT * FROM budgets
        WHERE userId = :userId AND month = :month AND year = :year
    """)
    fun getBudgetsForMonth(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("""
        SELECT * FROM budgets
        WHERE userId = :userId AND categoryId = :categoryId AND month = :month AND year = :year
    """)
    suspend fun getBudgetForCategory(userId: String, categoryId: Long, month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getBudgetsForMonthOnce(userId: String, month: Int, year: Int): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getAllBudgetsOnce(userId: String): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)
}
