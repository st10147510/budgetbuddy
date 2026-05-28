package com.budgetbuddy.util

import android.content.Context
import android.telephony.TelephonyManager
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    private var cachedLocale: Locale? = null

    fun format(context: Context, amount: Double): String {
        return try {
            val locale = getCountryLocale(context)
            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.currency = Currency.getInstance(locale)
            formatter.format(amount)
        } catch (e: Exception) {
            "R %.2f".format(amount)
        }
    }

    fun getSymbol(context: Context): String {
        return try {
            val locale = getCountryLocale(context)
            Currency.getInstance(locale).getSymbol(locale)
        } catch (e: Exception) {
            "R"
        }
    }

    fun resetCache() {
        cachedLocale = null
    }

    private fun getCountryLocale(context: Context): Locale {
        cachedLocale?.let { return it }

        // Try SIM country first — most reliable for physical location
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simCountry = tm?.simCountryIso?.uppercase()
        if (!simCountry.isNullOrBlank()) {
            runCatching {
                val locale = Locale("", simCountry)
                Currency.getInstance(locale)
                cachedLocale = locale
                return locale
            }
        }

        // Fall back to system default locale's country
        val default = Locale.getDefault()
        if (default.country.isNotBlank()) {
            runCatching {
                Currency.getInstance(default)
                cachedLocale = default
                return default
            }
        }

        // Default: South Africa (ZAR)
        val fallback = Locale("en", "ZA")
        cachedLocale = fallback
        return fallback
    }
}
