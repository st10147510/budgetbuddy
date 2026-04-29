package com.budgetbuddy.data.local.dao

import androidx.room.*
import com.budgetbuddy.data.local.entities.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Query("SELECT * FROM debts WHERE userId = :userId AND isPaidOff = 0 ORDER BY balance ASC")
    fun getActiveDebts(userId: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllDebts(userId: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("UPDATE debts SET isPaidOff = 1 WHERE id = :id")
    suspend fun markDebtPaidOff(id: Long)

    @Query("SELECT COUNT(*) FROM debts WHERE userId = :userId AND isPaidOff = 1")
    suspend fun getPaidOffDebtsCount(userId: String): Int
}
