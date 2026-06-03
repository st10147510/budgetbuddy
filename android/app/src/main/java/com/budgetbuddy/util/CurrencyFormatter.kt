package com.budgetbuddy.util

import android.content.Context
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    private val ZA_LOCALE = Locale("en", "ZA")

    fun format(@Suppress("UNUSED_PARAMETER") context: Context, amount: Double): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(ZA_LOCALE)
            formatter.currency = Currency.getInstance("ZAR")
            formatter.format(amount)
        } catch (e: Exception) {
            "R %.2f".format(amount)
        }
    }

    fun getSymbol(@Suppress("UNUSED_PARAMETER") context: Context): String = "R"

    fun resetCache() {
        // No-op: locale is fixed to ZAR; kept for API compatibility.
    }
}
