package com.budgetbuddy.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREFS = "budget_buddy_locale"
    private const val KEY_LANG = "language_tag"

    fun applyLocale(base: Context): Context {
        val tag = getSavedTag(base) ?: return base
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    fun setLanguage(context: Context, languageTag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, languageTag).apply()
    }

    fun getSavedTag(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, null)

    fun getSupportedLanguages(): List<Language> = listOf(
        Language("en", "English"),
        Language("af", "Afrikaans"),
        Language("zu", "isiZulu"),
        Language("st", "Sesotho"),
        Language("fr", "Français"),
        Language("es", "Español"),
        Language("pt", "Português"),
        Language("ar", "العربية"),
        Language("sw", "Kiswahili"),
        Language("de", "Deutsch"),
        Language("it", "Italiano"),
        Language("nl", "Nederlands"),
    )
}

data class Language(val tag: String, val displayName: String)
