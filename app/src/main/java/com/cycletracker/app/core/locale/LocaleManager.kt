package com.cycletracker.app.core.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Per-app locale applied at [Context] level via attachBaseContext, so it works on all API
 * levels including Android 10 (no AppCompat dependency). The chosen tag is mirrored to a
 * synchronous SharedPreferences so it can be read before the UI starts.
 */
object LocaleManager {
    private const val PREFS = "locale_prefs"
    private const val KEY = "locale_tag"

    fun wrap(base: Context): Context {
        val tag = base.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        if (tag.isNullOrEmpty()) return base
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    fun persist(context: Context, tag: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (tag.isNullOrEmpty()) remove(KEY) else putString(KEY, tag)
            apply()
        }
    }
}
