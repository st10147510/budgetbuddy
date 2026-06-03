package com.budgetbuddy.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index(value = ["userId", "categoryId", "month", "year"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val categoryId: Long,
    val minAmount: Double = 0.0,   // minimum spending goal for this category
    val limitAmount: Double,        // maximum spending limit for this category
    val month: Int,   // 1–12
    val year: Int,
    val createdAt: Long = System.currentTimeMillis()
)
