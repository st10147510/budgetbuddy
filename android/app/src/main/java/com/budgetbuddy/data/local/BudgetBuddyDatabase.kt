package com.budgetbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budgetbuddy.data.local.dao.*
import com.budgetbuddy.data.local.entities.*
import com.budgetbuddy.util.Converters

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        DebtEntity::class,
        BadgeEntity::class,
        NotificationRuleEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BudgetBuddyDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun debtDao(): DebtDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        const val DATABASE_NAME = "budget_buddy.db"

        /** Default categories seeded on first install */
        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(name = "Food & Groceries",   icon = "🛒", colorHex = "#4CAF50", isDefault = true),
            CategoryEntity(name = "Transport",           icon = "🚗", colorHex = "#2196F3", isDefault = true),
            CategoryEntity(name = "Entertainment",       icon = "🎬", colorHex = "#9C27B0", isDefault = true),
            CategoryEntity(name = "Healthcare",          icon = "💊", colorHex = "#F44336", isDefault = true),
            CategoryEntity(name = "Utilities",           icon = "💡", colorHex = "#FF9800", isDefault = true),
            CategoryEntity(name = "Housing",             icon = "🏠", colorHex = "#795548", isDefault = true),
            CategoryEntity(name = "Education",           icon = "📚", colorHex = "#00BCD4", isDefault = true),
            CategoryEntity(name = "Clothing",            icon = "👗", colorHex = "#E91E63", isDefault = true),
            CategoryEntity(name = "Savings",             icon = "💰", colorHex = "#8BC34A", isDefault = true),
            CategoryEntity(name = "Other",               icon = "📦", colorHex = "#607D8B", isDefault = true)
        )
    }
}
