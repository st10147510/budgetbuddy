package com.budgetbuddy.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BadgeType {
    FIRST_STEP,
    DAILY_TRACKER,
    PERFECT_MONTH,
    BUDGET_MASTER,
    DEBT_SLAYER,
    GOAL_GETTER,
    THRIFTY_CHAMP
}

@Entity(tableName = "badges", indices = [androidx.room.Index(value = ["userId", "badgeType"], unique = true)])
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val badgeType: BadgeType,
    val earnedAt: Long = System.currentTimeMillis()
)
