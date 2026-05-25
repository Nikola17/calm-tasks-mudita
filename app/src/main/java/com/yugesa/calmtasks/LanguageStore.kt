package com.yugesa.calmtasks

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

object LanguageStore {
    const val SYSTEM = ""

    private const val PREFS = "calm_tasks_prefs"
    private const val KEY_LANGUAGE = "language_code"

    fun current(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANGUAGE, SYSTEM) ?: SYSTEM
    }

    fun set(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    fun wrap(context: Context): Context {
        val languageCode = current(context)
        if (languageCode.isBlank()) {
            Locale.setDefault(Resources.getSystem().configuration.locales[0])
            return context
        }

        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList(locale))
        return context.createConfigurationContext(configuration)
    }
}

