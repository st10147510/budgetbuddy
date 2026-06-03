package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.GoalDao
import com.budgetbuddy.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class GoalRepositoryTest {

    private lateinit var goalDao: GoalDao
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var repository: GoalRepository

    @Before
    fun setup() {
        goalDao = mock()
        firestoreRepository = mock()
        repository = GoalRepository(goalDao, firestoreRepository)
    }

    private fun goal(id: Long, target: Double, saved: Double, completed: Boolean = false) =
        GoalEntity(id = id, userId = "user1", name = "Test Goal",
            targetAmount = target, savedAmount = saved, isCompleted = completed)

    @Test
    fun `getActiveGoals delegates to dao`() = runTest {
        whenever(goalDao.getActiveGoals("user1")).thenReturn(flowOf(emptyList()))
        repository.getActiveGoals("user1")
        verify(goalDao).getActiveGoals("user1")
    }

    @Test
    fun `insertGoal delegates to dao and returns id`() = runTest {
        val g = goal(0, 5000.0, 0.0)
        whenever(goalDao.insertGoal(g)).thenReturn(1L)
        val id = repository.insertGoal(g)
        assertEquals(1L, id)
        verify(goalDao).insertGoal(g)
    }

    @Test
    fun `insertGoal syncs to Firestore with DAO-assigned id`() = runTest {
        val g = goal(0, 5000.0, 0.0)
        whenever(goalDao.insertGoal(g)).thenReturn(1L)
        repository.insertGoal(g)
        verify(firestoreRepository).saveGoal("user1", g.copy(id = 1L))
    }

    @Test
    fun `updateGoal delegates to dao`() = runTest {
        val g = goal(1, 5000.0, 2500.0)
        repository.updateGoal(g)
        verify(goalDao).updateGoal(g)
    }

    @Test
    fun `updateGoal syncs to Firestore`() = runTest {
        val g = goal(1, 5000.0, 2500.0)
        repository.updateGoal(g)
        verify(firestoreRepository).saveGoal("user1", g)
    }

    @Test
    fun `deleteGoal delegates to dao`() = runTest {
        val g = goal(1, 5000.0, 2500.0)
        repository.deleteGoal(g)
        verify(goalDao).deleteGoal(g)
    }

    @Test
    fun `deleteGoal removes from Firestore`() = runTest {
        val g = goal(1, 5000.0, 2500.0)
        repository.deleteGoal(g)
        verify(firestoreRepository).deleteGoal("user1", 1L)
    }

    @Test
    fun `GoalEntity progressPercent is 50 when half saved`() {
        val g = goal(1, 1000.0, 500.0)
        assertEquals(50, g.progressPercent)
    }

    @Test
    fun `GoalEntity progressPercent is 0 when nothing saved`() {
        val g = goal(1, 1000.0, 0.0)
        assertEquals(0, g.progressPercent)
    }

    @Test
    fun `GoalEntity progressPercent is 100 when fully saved`() {
        val g = goal(1, 1000.0, 1000.0)
        assertEquals(100, g.progressPercent)
    }

    @Test
    fun `GoalEntity progressPercent is capped at 100`() {
        val g = goal(1, 1000.0, 1500.0) // over-saved
        assertEquals(100, g.progressPercent)
    }

    @Test
    fun `GoalEntity progressPercent is 0 when targetAmount is zero`() {
        val g = goal(1, 0.0, 500.0)
        assertEquals(0, g.progressPercent)
    }
}
