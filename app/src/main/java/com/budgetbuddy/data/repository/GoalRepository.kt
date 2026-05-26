package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.GoalDao
import com.budgetbuddy.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

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
        firestoreRepository.saveGoal(goal.userId, goal.copy(id = id))
        return id
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
        firestoreRepository.saveGoal(goal.userId, goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
        firestoreRepository.deleteGoal(goal.userId, goal.id)
    }
}
