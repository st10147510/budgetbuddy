package com.budgetbuddy.data.local.dao

import androidx.room.*
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
        ORDER BY date DESC
    """)
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
        AND categoryId = :categoryId
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getTransactionsByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE userId = :userId
        AND type = 'EXPENSE'
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalExpenseForPeriod(userId: String, startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE userId = :userId
        AND type = 'INCOME'
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalIncomeForPeriod(userId: String, startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE userId = :userId
        AND categoryId = :categoryId
        AND type = 'EXPENSE'
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalExpenseByCategoryAndPeriod(
        userId: String,
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Double?

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
        ORDER BY date DESC
        LIMIT :limit
    """)
    fun getRecentTransactions(userId: String, limit: Int = 5): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE userId = :userId
        AND date BETWEEN :startOfDay AND :endOfDay
    """)
    suspend fun getTransactionCountForDay(userId: String, startOfDay: Long, endOfDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}
