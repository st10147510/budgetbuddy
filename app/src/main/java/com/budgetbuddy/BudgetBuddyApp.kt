package com.budgetbuddy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.budgetbuddy.ui.notifications.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BudgetBuddyApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        NotificationScheduler.scheduleDailyReminder(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_BUDGET_ALERTS,
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Alerts when approaching or exceeding budget limits" }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_DAILY_REMINDER,
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Daily nudges to log expenses" }
            )

            // High-importance so the notification pops up on screen as a heads-up
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_BADGE_ACHIEVEMENTS,
                    "Badge Achievements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Congratulatory pop-ups when a new badge is earned" }
            )
        }
    }

    companion object {
        const val CHANNEL_BUDGET_ALERTS      = "budget_alerts"
        const val CHANNEL_DAILY_REMINDER     = "daily_reminder"
        const val CHANNEL_BADGE_ACHIEVEMENTS = "badge_achievements"
    }
}
