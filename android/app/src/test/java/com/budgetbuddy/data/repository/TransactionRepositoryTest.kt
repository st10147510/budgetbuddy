package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.TransactionDao
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class TransactionRepositoryTest {

    private lateinit var transactionDao: TransactionDao
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        transactionDao = mock()
        firestoreRepository = mock()
        repository = TransactionRepository(transactionDao, firestoreRepository)
    }

    private fun tx(id: Long = 0L, type: TransactionType = TransactionType.EXPENSE) =
        TransactionEntity(id = id, userId = "user1", amount = 50.0, categoryId = 1L,
            date = 1_000_000L, type = type)

    @Test
    fun `insertTransaction returns DAO-assigned id`() = runTest {
        val t = tx()
        whenever(transactionDao.insertTransaction(t)).thenReturn(42L)
        val id = repository.insertTransaction(t)
        assertEquals(42L, id)
    }

    @Test
    fun `insertTransaction syncs to Firestore with DAO-assigned id`() = runTest {
        val t = tx()
        whenever(transactionDao.insertTransaction(t)).thenReturn(42L)
        repository.insertTransaction(t)
        verify(firestoreRepository).saveTransaction("user1", t.copy(id = 42L))
    }

    @Test
    fun `updateTransaction saves to both Room and Firestore`() = runTest {
        val t = tx(id = 5L)
        repository.updateTransaction(t)
        verify(transactionDao).updateTransaction(t)
        verify(firestoreRepository).saveTransaction("user1", t)
    }

    @Test
    fun `deleteTransaction removes from both Room and Firestore`() = runTest {
        val t = tx(id = 3L)
        repository.deleteTransaction(t)
        verify(transactionDao).deleteTransaction(t)
        verify(firestoreRepository).deleteTransaction("user1", 3L)
    }

    @Test
    fun `income transaction syncs correctly`() = runTest {
        val t = tx(type = TransactionType.INCOME)
        whenever(transactionDao.insertTransaction(t)).thenReturn(9L)
        repository.insertTransaction(t)
        verify(firestoreRepository).saveTransaction("user1", t.copy(id = 9L))
    }
}
