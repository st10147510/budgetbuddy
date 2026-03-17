package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.BadgeDao
import com.budgetbuddy.data.local.dao.TransactionDao
import com.budgetbuddy.data.local.entities.BadgeEntity
import com.budgetbuddy.data.local.entities.BadgeType
import com.budgetbuddy.util.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepository @Inject constructor(
    private val badgeDao: BadgeDao,
    private val transactionDao: TransactionDao
) {
    fun getBadges(userId: String): Flow<List<BadgeEntity>> = badgeDao.getBadges(userId)

    private suspend fun awardBadge(userId: String, type: BadgeType) {
        if (badgeDao.getBadgeByType(userId, type) == null) {
            badgeDao.insertBadge(BadgeEntity(userId = userId, badgeType = type))
        }
    }

    suspend fun checkAndAwardBadges(userId: String) {
        // First Step badge
        val totalCount = transactionDao.getTransactionCountForDay(
            userId, 0L, System.currentTimeMillis()
        )
        if (totalCount >= 1) awardBadge(userId, BadgeType.FIRST_STEP)

        // Daily Tracker — check 7-day streak
        checkLoggingStreak(userId)
    }

    private suspend fun checkLoggingStreak(userId: String) {
        var streakDays = 0
        for (i in 0 until 7) {
            val dayMs = System.currentTimeMillis() - (i * 86400000L)
            val count = transactionDao.getTransactionCountForDay(
                userId, DateUtils.startOfDay(dayMs), DateUtils.endOfDay(dayMs)
            )
            if (count > 0) streakDays++ else break
        }
        if (streakDays >= 7) awardBadge(userId, BadgeType.DAILY_TRACKER)
    }
}
