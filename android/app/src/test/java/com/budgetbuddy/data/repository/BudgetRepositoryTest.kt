package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.BudgetDao
import com.budgetbuddy.data.local.entities.BudgetEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BudgetRepositoryTest {

    private lateinit var budgetDao: BudgetDao
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var repository: BudgetRepository

    @Before
    fun setup() {
        budgetDao = mock()
        firestoreRepository = mock()
        repository = BudgetRepository(budgetDao, firestoreRepository)
    }

    private fun budget(id: Long = 0L, limit: Double = 500.0, min: Double = 0.0) =
        BudgetEntity(
            id = id, userId = "user1", categoryId = 1L,
            limitAmount = limit, minAmount = min, month = 5, year = 2026
        )

    @Test
    fun `insertOrUpdateBudget returns id from DAO`() = runTest {
        val b = budget(0L)
        whenever(budgetDao.insertOrUpdateBudget(b)).thenReturn(42L)

        val id = repository.insertOrUpdateBudget(b)

        assertEquals(42L, id)
    }

    @Test
    fun `insertOrUpdateBudget syncs assigned id to Firestore`() = runTest {
        val b = budget(0L)
        whenever(budgetDao.insertOrUpdateBudget(b)).thenReturn(42L)

        repository.insertOrUpdateBudget(b)

        // Firestore must receive the DAO-assigned id (42), not the original 0
        verify(firestoreRepository).saveBudget("user1", b.copy(id = 42L))
    }

    @Test
    fun `insertOrUpdateBudget includes minAmount in Firestore payload`() = runTest {
        val b = budget(0L, limit = 1000.0, min = 200.0)
        whenever(budgetDao.insertOrUpdateBudget(b)).thenReturn(7L)

        repository.insertOrUpdateBudget(b)

        verify(firestoreRepository).saveBudget("user1", b.copy(id = 7L))
        // implicitly verifies minAmount=200.0 because b.copy only changes id
    }

    @Test
    fun `deleteBudget removes from DAO`() = runTest {
        val b = budget(5L)

        repository.deleteBudget(b)

        verify(budgetDao).deleteBudget(b)
    }

    @Test
    fun `deleteBudget removes from Firestore using correct userId and id`() = runTest {
        val b = budget(5L)

        repository.deleteBudget(b)

        verify(firestoreRepository).deleteBudget("user1", 5L)
    }
}
