package com.budgetbuddy.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,           // emoji or icon name
    val colorHex: String,       // e.g. "#FF5722"
    val isDefault: Boolean = false
)
