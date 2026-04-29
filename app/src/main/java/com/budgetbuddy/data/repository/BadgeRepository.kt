package com.budgetbuddy.data.repository

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.budgetbuddy.BudgetBuddyApp
import com.budgetbuddy.R
import com.budgetbuddy.data.local.dao.BadgeDao
import com.budgetbuddy.data.local.dao.BudgetDao
import com.budgetbuddy.data.local.dao.DebtDao
import com.budgetbuddy.data.local.dao.GoalDao
import com.budgetbuddy.data.local.dao.TransactionDao
import com.budgetbuddy.data.local.entities.BadgeEntity
import com.budgetbuddy.data.local.entities.BadgeType
import com.budgetbuddy.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val badgeDao: BadgeDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val debtDao: DebtDao
) {
    fun getBadges(userId: String): Flow<List<BadgeEntity>> = badgeDao.getBadges(userId)

    private suspend fun awardBadge(userId: String, type: BadgeType) {
        // Only award (and notify) if this badge hasn't been earned before
        if (badgeDao.getBadgeByType(userId, type) == null) {
            badgeDao.insertBadge(BadgeEntity(userId = userId, badgeType = type))
            showBadgeNotification(type)
            Log.i("BadgeRepository", "Badge awarded: ${type.name}")
        }
    }

    /**
     * Posts a heads-up notification congratulating the user on earning a new badge.
     * Each badge type uses a fixed notification ID so a second (impossible) award
     * would just update the existing notification rather than stacking duplicates.
     */
    private fun showBadgeNotification(type: BadgeType) {
        val (icon, name, message) = badgeNotificationContent(type)
        val notification = NotificationCompat.Builder(context, BudgetBuddyApp.CHANNEL_BADGE_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$icon  Badge Unlocked — $name")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use the badge type's ordinal as a stable, unique notification ID
        nm.notify(BADGE_NOTIF_BASE_ID + type.ordinal, notification)
    }

    private fun badgeNotificationContent(type: BadgeType): Triple<String, String, String> = when (type) {
        BadgeType.FIRST_STEP    -> Triple("🎯", "First Step",
            "You logged your first transaction. Your financial journey has begun!")
        BadgeType.DAILY_TRACKER -> Triple("🔥", "Daily Tracker",
            "7-day logging streak! Consistency is the key to financial control.")
        BadgeType.PERFECT_MONTH -> Triple("📅", "Perfect Month",
            "You earned more than you spent this month. Great work staying in the green!")
        BadgeType.BUDGET_MASTER -> Triple("💰", "Budget Master",
            "Every budget category stayed under its limit this month. Impressive discipline!")
        BadgeType.DEBT_SLAYER   -> Triple("⚔️", "Debt Slayer",
            "You paid off a debt in full. One less thing weighing on your finances!")
        BadgeType.GOAL_GETTER   -> Triple("🏆", "Goal Getter",
            "Savings goal completed! Set your next target and keep the momentum going.")
        BadgeType.THRIFTY_CHAMP -> Triple("💎", "Thrifty Champ",
            "You saved over 33% of your income this month. That's serious financial discipline!")
    }

    suspend fun checkAndAwardBadges(userId: String) {
        checkFirstStep(userId)
        checkLoggingStreak(userId)
        checkPerfectMonth(userId)
        checkBudgetMaster(userId)
        checkDebtSlayer(userId)
        checkGoalGetter(userId)
        checkThriftyChamp(userId)
    }

    /** Awarded for logging the very first transaction. */
    private suspend fun checkFirstStep(userId: String) {
        val count = transactionDao.getTransactionCountForDay(userId, 0L, System.currentTimeMillis())
        if (count >= 1) awardBadge(userId, BadgeType.FIRST_STEP)
    }

    /** Awarded for logging at least one transaction every day for 7 consecutive days. */
    private suspend fun checkLoggingStreak(userId: String) {
        var streakDays = 0
        for (i in 0 until 7) {
            val dayMs = System.currentTimeMillis() - (i * 86_400_000L)
            val count = transactionDao.getTransactionCountForDay(
                userId, DateUtils.startOfDay(dayMs), DateUtils.endOfDay(dayMs)
            )
            if (count > 0) streakDays++ else break
        }
        if (streakDays >= 7) awardBadge(userId, BadgeType.DAILY_TRACKER)
    }

    /** Awarded when total income exceeds total expenses in the current month. */
    private suspend fun checkPerfectMonth(userId: String) {
        val start = DateUtils.startOfMonth()
        val end = DateUtils.endOfMonth()
        val income = transactionDao.getTotalIncomeForPeriod(userId, start, end) ?: 0.0
        val expense = transactionDao.getTotalExpenseForPeriod(userId, start, end) ?: 0.0
        if (income > 0 && income > expense) awardBadge(userId, BadgeType.PERFECT_MONTH)
    }

    /** Awarded when no budget category is exceeded (spent < limit) in the current month. */
    private suspend fun checkBudgetMaster(userId: String) {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1  // BudgetEntity stores 1-based month
        val year = cal.get(Calendar.YEAR)
        val start = DateUtils.startOfMonth()
        val end = DateUtils.endOfMonth()
        val budgets = budgetDao.getBudgetsForMonthOnce(userId, month, year)
        if (budgets.isEmpty()) return  // no budgets set — badge not yet earnable
        val allUnderLimit = budgets.all { budget ->
            val spent = transactionDao.getTotalExpenseByCategoryAndPeriod(
                userId, budget.categoryId, start, end
            ) ?: 0.0
            spent < budget.limitAmount
        }
        if (allUnderLimit) awardBadge(userId, BadgeType.BUDGET_MASTER)
    }

    /** Awarded when the user fully pays off at least one debt. */
    private suspend fun checkDebtSlayer(userId: String) {
        if (debtDao.getPaidOffDebtsCount(userId) >= 1) awardBadge(userId, BadgeType.DEBT_SLAYER)
    }

    /** Awarded when the user completes at least one savings goal. */
    private suspend fun checkGoalGetter(userId: String) {
        if (goalDao.getCompletedGoalsCount(userId) >= 1) awardBadge(userId, BadgeType.GOAL_GETTER)
    }

    /** Awarded when income is at least 1.5× expenses this month (saved ≥ 33% of income). */
    private suspend fun checkThriftyChamp(userId: String) {
        val start = DateUtils.startOfMonth()
        val end = DateUtils.endOfMonth()
        val income = transactionDao.getTotalIncomeForPeriod(userId, start, end) ?: 0.0
        val expense = transactionDao.getTotalExpenseForPeriod(userId, start, end) ?: 0.0
        if (income > 0 && income >= expense * 1.5) awardBadge(userId, BadgeType.THRIFTY_CHAMP)
    }

    companion object {
        // Notification IDs for badges start at 2000 to avoid clashing with budget (1000+) notifications
        private const val BADGE_NOTIF_BASE_ID = 2000
    }
}
