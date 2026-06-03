package com.budgetbuddy.ui.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.budgetbuddy.R
import com.budgetbuddy.data.repository.BudgetRepository
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.ui.budget.BudgetStatus
import com.budgetbuddy.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()

        val month = DateUtils.currentMonth()
        val year = DateUtils.currentYear()
        val start = DateUtils.startOfMonth()
        val end = DateUtils.endOfMonth()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notifId = 1000

        budgetRepository.getBudgetsForMonth(userId, month, year)
            .collect { budgets ->
                budgets.forEach { budget ->
                    val category = categoryRepository.getCategoryById(budget.categoryId) ?: return@forEach
                    val spent = transactionRepository.getTotalExpenseByCategoryAndPeriod(
                        userId, budget.categoryId, start, end
                    )
                    val pct = if (budget.limitAmount > 0) (spent / budget.limitAmount * 100).toInt() else 0

                    val status = when {
                        pct >= 100 -> BudgetStatus.EXCEEDED
                        pct >= 80 -> BudgetStatus.WARNING
                        else -> BudgetStatus.OK
                    }

                    if (status != BudgetStatus.OK) {
                        val title = if (status == BudgetStatus.EXCEEDED)
                            "Budget Exceeded: ${category.name}"
                        else
                            "Budget Warning: ${category.name}"
                        val body = if (status == BudgetStatus.EXCEEDED)
                            "You've spent R%.2f of your R%.2f budget (${pct}%%)".format(spent, budget.limitAmount)
                        else
                            "You've used ${pct}%% of your ${category.name} budget"

                        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_BUDGET)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .build()

                        nm.notify(notifId++, notification)
                    }
                }
            }
        return Result.success()
    }

    companion object {
        const val KEY_USER_ID = "userId"
        const val CHANNEL_BUDGET = "budget_alerts"
    }
}
