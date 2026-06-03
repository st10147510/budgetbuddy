package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.dao.BadgeDao
import com.budgetbuddy.data.local.dao.BudgetDao
import com.budgetbuddy.data.local.dao.CategoryDao
import com.budgetbuddy.data.local.dao.DebtDao
import com.budgetbuddy.data.local.dao.GoalDao
import com.budgetbuddy.data.local.dao.TransactionDao
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SyncRepository"

@Singleton
class SyncRepository @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val debtDao: DebtDao,
    private val categoryDao: CategoryDao,
    private val badgeDao: BadgeDao
) {
    /**
     * Pushes all local Room data to Firestore.
     * All entity types are attempted independently (partial-failure tolerance).
     * Throws after all operations if any entity failed, so the caller can surface the error.
     */
    suspend fun syncToFirestore(userId: String) {
        val failed = mutableListOf<String>()
        try {
            transactionDao.getAllTransactionsOnce(userId).forEach {
                firestoreRepository.saveTransaction(userId, it)
            }
            Log.d(TAG, "pushed transactions for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "transaction push failed: ${e.message}")
            failed += "transactions"
        }
        try {
            budgetDao.getAllBudgetsOnce(userId).forEach {
                firestoreRepository.saveBudget(userId, it)
            }
            Log.d(TAG, "pushed budgets for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "budget push failed: ${e.message}")
            failed += "budgets"
        }
        try {
            goalDao.getAllGoalsOnce(userId).forEach {
                firestoreRepository.saveGoal(userId, it)
            }
            Log.d(TAG, "pushed goals for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "goal push failed: ${e.message}")
            failed += "goals"
        }
        try {
            debtDao.getAllDebtsOnce(userId).forEach {
                firestoreRepository.saveDebt(userId, it)
            }
            Log.d(TAG, "pushed debts for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "debt push failed: ${e.message}")
            failed += "debts"
        }
        try {
            categoryDao.getNonDefaultCategories().forEach {
                firestoreRepository.saveCategory(userId, it)
            }
            Log.d(TAG, "pushed categories for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "category push failed: ${e.message}")
            failed += "categories"
        }
        try {
            badgeDao.getAllBadgesOnce(userId).forEach {
                firestoreRepository.saveBadge(userId, it)
            }
            Log.d(TAG, "pushed badges for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "badge push failed: ${e.message}")
            failed += "badges"
        }
        if (failed.isNotEmpty()) throw Exception("Upload failed for: ${failed.joinToString()}")
    }

    /**
     * Pulls all user data from Firestore and upserts into Room.
     * All entity types are attempted independently (partial-failure tolerance).
     * Throws after all operations if any entity failed.
     */
    suspend fun syncFromFirestore(userId: String) {
        val failed = mutableListOf<String>()
        try {
            firestoreRepository.getTransactions(userId).forEach { transactionDao.insertTransaction(it) }
            Log.d(TAG, "synced transactions for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "transaction sync failed: ${e.message}")
            failed += "transactions"
        }
        try {
            firestoreRepository.getBudgets(userId).forEach { budgetDao.insertOrUpdateBudget(it) }
            Log.d(TAG, "synced budgets for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "budget sync failed: ${e.message}")
            failed += "budgets"
        }
        try {
            firestoreRepository.getGoals(userId).forEach { goalDao.insertGoal(it) }
            Log.d(TAG, "synced goals for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "goal sync failed: ${e.message}")
            failed += "goals"
        }
        try {
            firestoreRepository.getDebts(userId).forEach { debtDao.insertDebt(it) }
            Log.d(TAG, "synced debts for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "debt sync failed: ${e.message}")
            failed += "debts"
        }
        try {
            firestoreRepository.getCategories(userId).forEach { categoryDao.insertCategory(it) }
            Log.d(TAG, "synced categories for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "category sync failed: ${e.message}")
            failed += "categories"
        }
        try {
            firestoreRepository.getBadges(userId).forEach { badgeDao.insertBadge(it) }
            Log.d(TAG, "synced badges for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "badge sync failed: ${e.message}")
            failed += "badges"
        }
        if (failed.isNotEmpty()) throw Exception("Download failed for: ${failed.joinToString()}")
    }
}
