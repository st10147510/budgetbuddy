package com.budgetbuddy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BudgetBuddyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
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
        }
    }

    companion object {
        const val CHANNEL_BUDGET_ALERTS  = "budget_alerts"
        const val CHANNEL_DAILY_REMINDER = "daily_reminder"
    }
}
