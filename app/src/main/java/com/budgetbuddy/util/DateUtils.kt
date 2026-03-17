package com.budgetbuddy.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun startOfMonth(calendar: Calendar = Calendar.getInstance()): Long {
        return calendar.clone<Calendar>().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun endOfMonth(calendar: Calendar = Calendar.getInstance()): Long {
        return calendar.clone<Calendar>().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun startOfDay(dateMs: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = dateMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun endOfDay(dateMs: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = dateMs
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun startOfWeek(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun formatDate(dateMs: Long, pattern: String = "dd MMM yyyy"): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(dateMs))

    fun formatAmount(amount: Double, currencySymbol: String = "R"): String =
        "$currencySymbol ${String.format("%.2f", amount)}"

    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun currentYear(): Int  = Calendar.getInstance().get(Calendar.YEAR)
}
