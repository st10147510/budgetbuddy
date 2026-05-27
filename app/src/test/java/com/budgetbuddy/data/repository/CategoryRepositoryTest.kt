package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.local.dao.CategoryDao
import com.budgetbuddy.data.local.entities.CategoryEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class CategoryRepositoryTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        categoryDao = mock()
        firestoreRepository = mock()
        sessionManager = mock()
        whenever(sessionManager.userId).thenReturn("user1")
        repository = CategoryRepository(categoryDao, firestoreRepository, sessionManager)
    }

    private fun category(id: Long, isDefault: Boolean) =
        CategoryEntity(id = id, name = "Food", icon = "🍔", colorHex = "#FF5733", isDefault = isDefault)

    @Test
    fun `insertCategory returns DAO-assigned id`() = runTest {
        val c = category(0, false)
        whenever(categoryDao.insertCategory(c)).thenReturn(7L)
        val id = repository.insertCategory(c)
        assertEquals(7L, id)
    }

    @Test
    fun `insertCategory syncs non-default category to Firestore`() = runTest {
        val c = category(0, false)
        whenever(categoryDao.insertCategory(c)).thenReturn(7L)
        repository.insertCategory(c)
        verify(firestoreRepository).saveCategory("user1", c.copy(id = 7L))
    }

    @Test
    fun `insertCategory does not sync default category to Firestore`() = runTest {
        val c = category(0, true)
        whenever(categoryDao.insertCategory(c)).thenReturn(1L)
        repository.insertCategory(c)
        verify(firestoreRepository, never()).saveCategory(any(), any())
    }

    @Test
    fun `updateCategory syncs non-default to Firestore`() = runTest {
        val c = category(3L, false)
        repository.updateCategory(c)
        verify(categoryDao).updateCategory(c)
        verify(firestoreRepository).saveCategory("user1", c)
    }

    @Test
    fun `updateCategory does not sync default category to Firestore`() = runTest {
        val c = category(1L, true)
        repository.updateCategory(c)
        verify(firestoreRepository, never()).saveCategory(any(), any())
    }

    @Test
    fun `deleteCategory removes non-default from Room and Firestore`() = runTest {
        val c = category(3L, false)
        repository.deleteCategory(c)
        verify(categoryDao).deleteCategory(c)
        verify(firestoreRepository).deleteCategory("user1", 3L)
    }

    @Test
    fun `deleteCategory is a no-op for default categories`() = runTest {
        val c = category(1L, true)
        repository.deleteCategory(c)
        verify(categoryDao, never()).deleteCategory(any())
        verify(firestoreRepository, never()).deleteCategory(any(), any())
    }
}
