package com.budgetbuddy.data.local.dao

import androidx.room.*
import com.budgetbuddy.data.local.entities.BadgeEntity
import com.budgetbuddy.data.local.entities.BadgeType
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getBadges(userId: String): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY earnedAt DESC")
    suspend fun getAllBadgesOnce(userId: String): List<BadgeEntity>

    @Query("SELECT * FROM badges WHERE userId = :userId AND badgeType = :type LIMIT 1")
    suspend fun getBadgeByType(userId: String, type: BadgeType): BadgeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: BadgeEntity): Long

    @Query("SELECT COUNT(*) FROM badges WHERE userId = :userId")
    suspend fun getBadgeCount(userId: String): Int
}
