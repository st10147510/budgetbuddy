package com.budgetbuddy.data.repository

import android.util.Log
import com.budgetbuddy.data.local.dao.TransactionDao
import com.budgetbuddy.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TransactionRepo"

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val firestoreRepository: FirestoreRepository
) {
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions(userId)

    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByDateRange(userId, startDate, endDate)

    fun getRecentTransactions(userId: String, limit: Int = 5): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(userId, limit)

    suspend fun getTotalExpenseForPeriod(userId: String, startDate: Long, endDate: Long): Double =
        transactionDao.getTotalExpenseForPeriod(userId, startDate, endDate) ?: 0.0

    suspend fun getTotalIncomeForPeriod(userId: String, startDate: Long, endDate: Long): Double =
        transactionDao.getTotalIncomeForPeriod(userId, startDate, endDate) ?: 0.0

    suspend fun getTotalExpenseByCategoryAndPeriod(userId: String, categoryId: Long, startDate: Long, endDate: Long): Double =
        transactionDao.getTotalExpenseByCategoryAndPeriod(userId, categoryId, startDate, endDate) ?: 0.0

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    suspend fun getTransactionCountForDay(userId: String, startOfDay: Long, endOfDay: Long): Int =
        transactionDao.getTransactionCountForDay(userId, startOfDay, endOfDay)

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        try { firestoreRepository.saveTransaction(transaction.userId, transaction.copy(id = id)) }
        catch (e: Exception) { Log.w(TAG, "saveTransaction failed: ${e.message}") }
        return id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
        try { firestoreRepository.saveTransaction(transaction.userId, transaction) }
        catch (e: Exception) { Log.w(TAG, "updateTransaction failed: ${e.message}") }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        try { firestoreRepository.deleteTransaction(transaction.userId, transaction.id) }
        catch (e: Exception) { Log.w(TAG, "deleteTransaction failed: ${e.message}") }
    }

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)
}
