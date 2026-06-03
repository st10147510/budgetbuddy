package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.local.dao.CategoryDao
import com.budgetbuddy.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CategoryRepo"

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val firestoreRepository: FirestoreRepository,
    private val sessionManager: SessionManager
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: CategoryEntity): Long {
        val id = categoryDao.insertCategory(category)
        if (!category.isDefault) {
            val userId = sessionManager.userId ?: return id
            try { firestoreRepository.saveCategory(userId, category.copy(id = id)) }
            catch (e: Exception) { Log.w(TAG, "saveCategory failed: ${e.message}") }
        }
        return id
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
        if (!category.isDefault) {
            val userId = sessionManager.userId ?: return
            try { firestoreRepository.saveCategory(userId, category) }
            catch (e: Exception) { Log.w(TAG, "updateCategory failed: ${e.message}") }
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        if (category.isDefault) return
        categoryDao.deleteCategory(category)
        val userId = sessionManager.userId ?: return
        try { firestoreRepository.deleteCategory(userId, category.id) }
        catch (e: Exception) { Log.w(TAG, "deleteCategory failed: ${e.message}") }
    }
}
