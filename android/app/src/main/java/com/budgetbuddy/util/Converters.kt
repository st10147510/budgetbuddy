package com.budgetbuddy.util

import androidx.room.TypeConverter
import com.budgetbuddy.data.local.entities.BadgeType
import com.budgetbuddy.data.local.entities.NotificationType
import com.budgetbuddy.data.local.entities.PayoffStrategy
import com.budgetbuddy.data.local.entities.TransactionType

class Converters {

    @TypeConverter fun transactionTypeToString(value: TransactionType): String = value.name
    @TypeConverter fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter fun badgeTypeToString(value: BadgeType): String = value.name
    @TypeConverter fun stringToBadgeType(value: String): BadgeType = BadgeType.valueOf(value)

    @TypeConverter fun notificationTypeToString(value: NotificationType): String = value.name
    @TypeConverter fun stringToNotificationType(value: String): NotificationType = NotificationType.valueOf(value)

    @TypeConverter fun payoffStrategyToString(value: PayoffStrategy): String = value.name
    @TypeConverter fun stringToPayoffStrategy(value: String): PayoffStrategy = PayoffStrategy.valueOf(value)
}
