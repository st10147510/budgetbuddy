package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.dao.GoalDao
import com.budgetbuddy.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoalRepo"

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val firestoreRepository: FirestoreRepository
) {
    fun getGoals(userId: String): Flow<List<GoalEntity>> = goalDao.getGoals(userId)
    fun getActiveGoals(userId: String): Flow<List<GoalEntity>> = goalDao.getActiveGoals(userId)
    suspend fun getGoalById(id: Long): GoalEntity? = goalDao.getGoalById(id)

    suspend fun insertGoal(goal: GoalEntity): Long {
        val id = goalDao.insertGoal(goal)
        try { firestoreRepository.saveGoal(goal.userId, goal.copy(id = id)) }
        catch (e: Exception) { Log.w(TAG, "saveGoal failed: ${e.message}") }
        return id
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
        try { firestoreRepository.saveGoal(goal.userId, goal) }
        catch (e: Exception) { Log.w(TAG, "updateGoal failed: ${e.message}") }
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
        try { firestoreRepository.deleteGoal(goal.userId, goal.id) }
        catch (e: Exception) { Log.w(TAG, "deleteGoal failed: ${e.message}") }
    }
}
