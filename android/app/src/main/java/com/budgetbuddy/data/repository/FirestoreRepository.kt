package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.entities.BadgeEntity
import com.budgetbuddy.data.local.entities.BadgeType
import com.budgetbuddy.data.local.entities.BudgetEntity
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.local.entities.GoalEntity
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreRepo"

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(userId: String)      = firestore.collection("users").document(userId)
    private fun txCol(userId: String)        = userDoc(userId).collection("transactions")
    private fun budgetCol(userId: String)    = userDoc(userId).collection("budgets")
    private fun goalCol(userId: String)      = userDoc(userId).collection("goals")
    private fun debtCol(userId: String)      = userDoc(userId).collection("debts")
    private fun categoryCol(userId: String)  = userDoc(userId).collection("categories")
    private fun badgeCol(userId: String)     = userDoc(userId).collection("badges")

    // ─── Transactions ────────────────────────────────────────────────────────

    suspend fun saveTransaction(userId: String, entity: TransactionEntity) {
        txCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
    }

    suspend fun deleteTransaction(userId: String, id: Long) {
        txCol(userId).document(id.toString()).delete().await()
    }

    suspend fun getTransactions(userId: String): List<TransactionEntity> {
        return try {
            txCol(userId).get().await().documents.mapNotNull { it.toTransaction(userId) }
        } catch (e: Exception) {
            Log.w(TAG, "getTransactions failed: ${e.message}")
            emptyList()
        }
    }

    // ─── Budgets ─────────────────────────────────────────────────────────────

    suspend fun saveBudget(userId: String, entity: BudgetEntity) {
        budgetCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
    }

    suspend fun deleteBudget(userId: String, id: Long) {
        budgetCol(userId).document(id.toString()).delete().await()
    }

    suspend fun getBudgets(userId: String): List<BudgetEntity> {
        return try {
            budgetCol(userId).get().await().documents.mapNotNull { it.toBudget(userId) }
        } catch (e: Exception) {
            Log.w(TAG, "getBudgets failed: ${e.message}")
            emptyList()
        }
    }

    // ─── Goals ────────────────────────────────────────────────────────────────

    suspend fun saveGoal(userId: String, entity: GoalEntity) {
        goalCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
    }

    suspend fun deleteGoal(userId: String, id: Long) {
        goalCol(userId).document(id.toString()).delete().await()
    }

    suspend fun getGoals(userId: String): List<GoalEntity> {
        return try {
            goalCol(userId).get().await().documents.mapNotNull { it.toGoal(userId) }
        } catch (e: Exception) {
            Log.w(TAG, "getGoals failed: ${e.message}")
            emptyList()
        }
    }

    // ─── Debts ────────────────────────────────────────────────────────────────

    suspend fun saveDebt(userId: String, entity: DebtEntity) {
        debtCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
    }

    suspend fun deleteDebt(userId: String, id: Long) {
        debtCol(userId).document(id.toString()).delete().await()
    }

    suspend fun getDebts(userId: String): List<DebtEntity> {
        return try {
            debtCol(userId).get().await().documents.mapNotNull { it.toDebt(userId) }
        } catch (e: Exception) {
            Log.w(TAG, "getDebts failed: ${e.message}")
            emptyList()
        }
    }

    // ─── Categories ──────────────────────────────────────────────────────────

    suspend fun saveCategory(userId: String, entity: CategoryEntity) {
        categoryCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
    }

    suspend fun deleteCategory(userId: String, id: Long) {
        categoryCol(userId).document(id.toString()).delete().await()
    }

    suspend fun getCategories(userId: String): List<CategoryEntity> {
        return try {
            categoryCol(userId).get().await().documents.mapNotNull { it.toCategory() }
        } catch (e: Exception) {
            Log.w(TAG, "getCategories failed: ${e.message}")
            emptyList()
        }
    }

    // ─── Badges ──────────────────────────────────────────────────────────────

    suspend fun saveBadge(userId: String, entity: BadgeEntity) {
        badgeCol(userId).document(entity.badgeType.name).set(entity.toMap()).await()
    }

    suspend fun getBadges(userId: String): List<BadgeEntity> {
        return try {
            badgeCol(userId).get().await().documents.mapNotNull { it.toBadge(userId) }
        } catch (e: Exception) {
            Log.w(TAG, "getBadges failed: ${e.message}")
            emptyList()
        }
    }

    // ─── User Profile ─────────────────────────────────────────────────────────

    suspend fun saveUserProfile(userId: String, displayName: String, email: String, photoUrl: String? = null) {
        try {
            val data = buildMap<String, Any> {
                put("displayName", displayName)
                put("email", email)
                put("updatedAt", System.currentTimeMillis())
                photoUrl?.let { put("photoUrl", it) }
            }
            userDoc(userId).set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveUserProfile failed: ${e.message}")
        }
    }

    suspend fun getUserPhotoUrl(userId: String): String? {
        return try {
            userDoc(userId).get().await().getString("photoUrl")
        } catch (e: Exception) {
            Log.w(TAG, "getUserPhotoUrl failed: ${e.message}")
            null
        }
    }

    // ─── Serialisation helpers ────────────────────────────────────────────────

    private fun TransactionEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "amount" to amount,
        "categoryId" to categoryId,
        "date" to date,
        "notes" to (notes ?: ""),
        "receiptImagePath" to (receiptImagePath ?: ""),
        "type" to type.name,
        "createdAt" to createdAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toTransaction(userId: String): TransactionEntity? {
        return try {
            TransactionEntity(
                id = getLong("id") ?: 0L,
                userId = userId,
                amount = getDouble("amount") ?: 0.0,
                categoryId = getLong("categoryId") ?: 0L,
                date = getLong("date") ?: 0L,
                notes = getString("notes")?.takeIf { it.isNotBlank() },
                receiptImagePath = getString("receiptImagePath")?.takeIf { it.isNotBlank() },
                type = TransactionType.valueOf(getString("type") ?: "EXPENSE"),
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun BudgetEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "categoryId" to categoryId,
        "limitAmount" to limitAmount,
        "minAmount" to minAmount,
        "month" to month,
        "year" to year,
        "createdAt" to createdAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toBudget(userId: String): BudgetEntity? {
        return try {
            BudgetEntity(
                id = getLong("id") ?: 0L,
                userId = userId,
                categoryId = getLong("categoryId") ?: 0L,
                limitAmount = getDouble("limitAmount") ?: 0.0,
                minAmount = getDouble("minAmount") ?: 0.0,
                month = getLong("month")?.toInt() ?: 1,
                year = getLong("year")?.toInt() ?: 2024,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun GoalEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "targetAmount" to targetAmount,
        "savedAmount" to savedAmount,
        "targetDate" to (targetDate ?: 0L),
        "isCompleted" to isCompleted,
        "createdAt" to createdAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toGoal(userId: String): GoalEntity? {
        return try {
            GoalEntity(
                id = getLong("id") ?: 0L,
                userId = userId,
                name = getString("name") ?: "",
                targetAmount = getDouble("targetAmount") ?: 0.0,
                savedAmount = getDouble("savedAmount") ?: 0.0,
                targetDate = getLong("targetDate")?.takeIf { it > 0 },
                isCompleted = getBoolean("isCompleted") ?: false,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DebtEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "originalBalance" to originalBalance,
        "balance" to balance,
        "interestRate" to interestRate,
        "minimumPayment" to minimumPayment,
        "isPaidOff" to isPaidOff,
        "createdAt" to createdAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toDebt(userId: String): DebtEntity? {
        return try {
            DebtEntity(
                id = getLong("id") ?: 0L,
                userId = userId,
                name = getString("name") ?: "",
                originalBalance = getDouble("originalBalance") ?: 0.0,
                balance = getDouble("balance") ?: 0.0,
                interestRate = getDouble("interestRate") ?: 0.0,
                minimumPayment = getDouble("minimumPayment") ?: 0.0,
                isPaidOff = getBoolean("isPaidOff") ?: false,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun CategoryEntity.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "icon" to icon,
        "colorHex" to colorHex,
        "isDefault" to isDefault
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toCategory(): CategoryEntity? {
        return try {
            CategoryEntity(
                id = getLong("id") ?: 0L,
                name = getString("name") ?: "",
                icon = getString("icon") ?: "📦",
                colorHex = getString("colorHex") ?: "#607D8B",
                isDefault = getBoolean("isDefault") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun BadgeEntity.toMap() = mapOf(
        "userId" to userId,
        "badgeType" to badgeType.name,
        "earnedAt" to earnedAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toBadge(userId: String): BadgeEntity? {
        return try {
            BadgeEntity(
                id = 0,
                userId = userId,
                badgeType = BadgeType.valueOf(getString("badgeType") ?: return null),
                earnedAt = getLong("earnedAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
