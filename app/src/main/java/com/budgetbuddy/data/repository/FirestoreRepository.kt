package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.entities.BudgetEntity
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
    private fun userDoc(userId: String) = firestore.collection("users").document(userId)
    private fun txCol(userId: String) = userDoc(userId).collection("transactions")
    private fun budgetCol(userId: String) = userDoc(userId).collection("budgets")
    private fun goalCol(userId: String) = userDoc(userId).collection("goals")
    private fun debtCol(userId: String) = userDoc(userId).collection("debts")

    // ─── Transactions ────────────────────────────────────────────────────────

    suspend fun saveTransaction(userId: String, entity: TransactionEntity) {
        try {
            txCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveTransaction failed: ${e.message}")
        }
    }

    suspend fun deleteTransaction(userId: String, id: Long) {
        try {
            txCol(userId).document(id.toString()).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteTransaction failed: ${e.message}")
        }
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
        try {
            budgetCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveBudget failed: ${e.message}")
        }
    }

    suspend fun deleteBudget(userId: String, id: Long) {
        try {
            budgetCol(userId).document(id.toString()).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteBudget failed: ${e.message}")
        }
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
        try {
            goalCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveGoal failed: ${e.message}")
        }
    }

    suspend fun deleteGoal(userId: String, id: Long) {
        try {
            goalCol(userId).document(id.toString()).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteGoal failed: ${e.message}")
        }
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
        try {
            debtCol(userId).document(entity.id.toString()).set(entity.toMap()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveDebt failed: ${e.message}")
        }
    }

    suspend fun deleteDebt(userId: String, id: Long) {
        try {
            debtCol(userId).document(id.toString()).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteDebt failed: ${e.message}")
        }
    }

    suspend fun getDebts(userId: String): List<DebtEntity> {
        return try {
            debtCol(userId).get().await().documents.mapNotNull { it.toDebt(userId) }
        } catch (e: Exception) {
            Log.w(TAG, "getDebts failed: ${e.message}")
            emptyList()
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
}
