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
     * Called on sign-in to upload data that was saved while Firestore writes were unavailable.
     */
    suspend fun syncToFirestore(userId: String) {
        try {
            transactionDao.getAllTransactionsOnce(userId).forEach {
                firestoreRepository.saveTransaction(userId, it)
            }
            Log.d(TAG, "pushed transactions for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "transaction push failed: ${e.message}")
        }
        try {
            budgetDao.getAllBudgetsOnce(userId).forEach {
                firestoreRepository.saveBudget(userId, it)
            }
            Log.d(TAG, "pushed budgets for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "budget push failed: ${e.message}")
        }
        try {
            goalDao.getAllGoalsOnce(userId).forEach {
                firestoreRepository.saveGoal(userId, it)
            }
            Log.d(TAG, "pushed goals for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "goal push failed: ${e.message}")
        }
        try {
            debtDao.getAllDebtsOnce(userId).forEach {
                firestoreRepository.saveDebt(userId, it)
            }
            Log.d(TAG, "pushed debts for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "debt push failed: ${e.message}")
        }
        try {
            categoryDao.getNonDefaultCategories().forEach {
                firestoreRepository.saveCategory(userId, it)
            }
            Log.d(TAG, "pushed categories for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "category push failed: ${e.message}")
        }
        try {
            badgeDao.getAllBadgesOnce(userId).forEach {
                firestoreRepository.saveBadge(userId, it)
            }
            Log.d(TAG, "pushed badges for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "badge push failed: ${e.message}")
        }
    }

    /**
     * Pulls all user data from Firestore and upserts into Room.
     * Safe to call on every sign-in — all target DAOs use REPLACE strategy.
     */
    suspend fun syncFromFirestore(userId: String) {
        try {
            firestoreRepository.getTransactions(userId).forEach { transactionDao.insertTransaction(it) }
            Log.d(TAG, "synced transactions for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "transaction sync failed: ${e.message}")
        }
        try {
            firestoreRepository.getBudgets(userId).forEach { budgetDao.insertOrUpdateBudget(it) }
            Log.d(TAG, "synced budgets for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "budget sync failed: ${e.message}")
        }
        try {
            firestoreRepository.getGoals(userId).forEach { goalDao.insertGoal(it) }
            Log.d(TAG, "synced goals for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "goal sync failed: ${e.message}")
        }
        try {
            firestoreRepository.getDebts(userId).forEach { debtDao.insertDebt(it) }
            Log.d(TAG, "synced debts for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "debt sync failed: ${e.message}")
        }
        try {
            firestoreRepository.getCategories(userId).forEach { categoryDao.insertCategory(it) }
            Log.d(TAG, "synced categories for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "category sync failed: ${e.message}")
        }
        try {
            firestoreRepository.getBadges(userId).forEach { badgeDao.insertBadge(it) }
            Log.d(TAG, "synced badges for $userId")
        } catch (e: Exception) {
            Log.w(TAG, "badge sync failed: ${e.message}")
        }
    }
}
