package com.budgetbuddy.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgetbuddy.data.local.BudgetBuddyDatabase
import com.budgetbuddy.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BudgetBuddyDatabase {
        var db: BudgetBuddyDatabase? = null
        db = Room.databaseBuilder(context, BudgetBuddyDatabase::class.java, BudgetBuddyDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                // Single seeding path: fires on every open, seeds only when table is empty.
                // Using raw SQL for the count avoids re-entering Room while the DB is
                // still initialising (which can deadlock). The actual insert is deferred
                // to a coroutine so the onOpen lock is already released before Room is
                // accessed again via the DAO.
                override fun onOpen(sqLiteDatabase: SupportSQLiteDatabase) {
                    super.onOpen(sqLiteDatabase)
                    val cursor = sqLiteDatabase.query("SELECT COUNT(*) FROM categories")
                    val isEmpty = cursor.moveToFirst() && cursor.getInt(0) == 0
                    cursor.close()
                    if (isEmpty) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db?.categoryDao()?.insertCategories(BudgetBuddyDatabase.DEFAULT_CATEGORIES)
                        }
                    }
                }
            })
            .build()
        return db
    }

    @Provides fun provideUserDao(db: BudgetBuddyDatabase): UserDao = db.userDao()
    @Provides fun provideCategoryDao(db: BudgetBuddyDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideTransactionDao(db: BudgetBuddyDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideBudgetDao(db: BudgetBuddyDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideGoalDao(db: BudgetBuddyDatabase): GoalDao = db.goalDao()
    @Provides fun provideDebtDao(db: BudgetBuddyDatabase): DebtDao = db.debtDao()
    @Provides fun provideBadgeDao(db: BudgetBuddyDatabase): BadgeDao = db.badgeDao()
}
