package com.budgetbuddy.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType { BUDGET_NEAR_LIMIT, BUDGET_EXCEEDED, DAILY_REMINDER }

@Entity(tableName = "notification_rules")
data class NotificationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val type: NotificationType,
    val threshold: Double = 0.8,    // for BUDGET_NEAR_LIMIT: 0.80 = 80%
    val enabled: Boolean = true
)
