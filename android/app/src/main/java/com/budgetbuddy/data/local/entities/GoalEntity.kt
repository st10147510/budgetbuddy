package com.budgetbuddy.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDate: Long? = null,           // epoch ms, nullable
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Int
        get() = if (targetAmount <= 0) 0 else ((savedAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
}
