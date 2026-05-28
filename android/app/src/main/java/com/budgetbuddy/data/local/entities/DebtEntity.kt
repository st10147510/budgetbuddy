package com.budgetbuddy.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PayoffStrategy { SNOWBALL, AVALANCHE }

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val originalBalance: Double,    // balance at the time the debt was added
    val balance: Double,            // current outstanding balance (reduced by payments)
    val interestRate: Double,       // annual percentage, e.g. 18.5
    val minimumPayment: Double,
    val isPaidOff: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
