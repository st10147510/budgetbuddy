package com.budgetbuddy.ui.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    /**
     * Schedule daily reminder at approximately 8 PM.
     * Uses periodic work with an initial delay calculated from current time.
     */
    fun scheduleDailyReminder(context: Context) {
        val dailyWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(20, TimeUnit.HOURS) // roughly 8 PM
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWork
        )
    }

    /**
     * Schedule budget alert checks every 6 hours.
     */
    fun scheduleBudgetAlerts(context: Context, userId: String) {
        val alertWork = PeriodicWorkRequestBuilder<BudgetAlertWorker>(6, TimeUnit.HOURS)
            .setInputData(workDataOf(BudgetAlertWorker.KEY_USER_ID to userId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "budget_alerts",
            ExistingPeriodicWorkPolicy.REPLACE,
            alertWork
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }
}
