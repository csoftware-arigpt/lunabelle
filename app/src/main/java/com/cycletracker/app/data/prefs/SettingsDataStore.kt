package com.cycletracker.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cycletracker.app.domain.model.AppSettings
import com.cycletracker.app.domain.model.PhaseReminder
import com.cycletracker.app.domain.model.ReminderPhase
import com.cycletracker.app.domain.model.TempUnit
import com.cycletracker.app.domain.model.ThemeMode
import com.cycletracker.app.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { it.toAppSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs -> prefs.writeAppSettings(transform(prefs.toAppSettings())) }
    }

    private fun Preferences.toAppSettings() = AppSettings(
        themeMode = enumByName(this[THEME], ThemeMode.SYSTEM),
        dynamicColor = this[DYNAMIC_COLOR] ?: true,
        seedColor = this[SEED] ?: 0L,
        delaySinceEpochDay = this[DELAY_SINCE] ?: 0L,
        localeTag = this[LOCALE_TAG],
        tempUnit = enumByName(this[TEMP_UNIT], TempUnit.CELSIUS),
        weightUnit = enumByName(this[WEIGHT_UNIT], WeightUnit.KG),
        periodReminderLeadDays = this[PERIOD_LEAD] ?: 2,
        phaseReminders = ReminderPhase.entries.associateWith { phase ->
            PhaseReminder(
                enabled = this[booleanPreferencesKey("rem_${phase.name}_enabled")] ?: (phase == ReminderPhase.MENSTRUAL),
                title = this[stringPreferencesKey("rem_${phase.name}_title")],
                body = this[stringPreferencesKey("rem_${phase.name}_body")],
            )
        },
        appLockEnabled = this[APP_LOCK] ?: false,
        showNotificationDetails = this[NOTIF_DETAILS] ?: false,
        onboardingComplete = this[ONBOARDING] ?: false,
        disclaimerAcceptedVersion = this[DISCLAIMER] ?: 0,
    )

    private fun MutablePreferences.writeAppSettings(s: AppSettings) {
        this[THEME] = s.themeMode.name
        this[DYNAMIC_COLOR] = s.dynamicColor
        this[SEED] = s.seedColor
        this[DELAY_SINCE] = s.delaySinceEpochDay
        s.localeTag?.let { this[LOCALE_TAG] = it } ?: remove(LOCALE_TAG)
        this[TEMP_UNIT] = s.tempUnit.name
        this[WEIGHT_UNIT] = s.weightUnit.name
        this[PERIOD_LEAD] = s.periodReminderLeadDays
        ReminderPhase.entries.forEach { phase ->
            val r = s.reminderFor(phase)
            this[booleanPreferencesKey("rem_${phase.name}_enabled")] = r.enabled
            val titleKey = stringPreferencesKey("rem_${phase.name}_title")
            val bodyKey = stringPreferencesKey("rem_${phase.name}_body")
            r.title?.let { this[titleKey] = it } ?: remove(titleKey)
            r.body?.let { this[bodyKey] = it } ?: remove(bodyKey)
        }
        this[APP_LOCK] = s.appLockEnabled
        this[NOTIF_DETAILS] = s.showNotificationDetails
        this[ONBOARDING] = s.onboardingComplete
        this[DISCLAIMER] = s.disclaimerAcceptedVersion
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SEED = longPreferencesKey("seed_color")
        val DELAY_SINCE = longPreferencesKey("delay_since")
        val LOCALE_TAG = stringPreferencesKey("locale_tag")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val PERIOD_LEAD = intPreferencesKey("period_lead_days")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val NOTIF_DETAILS = booleanPreferencesKey("notif_details")
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val DISCLAIMER = intPreferencesKey("disclaimer_version")
    }
}

private inline fun <reified T : Enum<T>> enumByName(name: String?, default: T): T =
    name?.let { runCatching { enumValueOf<T>(it) }.getOrDefault(default) } ?: default
