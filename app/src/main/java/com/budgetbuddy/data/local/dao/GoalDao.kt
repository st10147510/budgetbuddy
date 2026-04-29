package com.budgetbuddy.data.local.dao

import androidx.room.*
import com.budgetbuddy.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoals(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 0")
    fun getActiveGoals(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedGoalsCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
}
