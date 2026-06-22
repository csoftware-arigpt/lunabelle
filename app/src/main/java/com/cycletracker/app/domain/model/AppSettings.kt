package com.cycletracker.app.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class TempUnit { CELSIUS, FAHRENHEIT }
enum class WeightUnit { KG, LB }

/** The four cycle phases the user can get a reminder for. */
enum class ReminderPhase { MENSTRUAL, FOLLICULAR, OVULATORY, LUTEAL }

/** Per-phase reminder config: on/off + optional custom title & body (null/blank = default text). */
data class PhaseReminder(
    val enabled: Boolean = false,
    val title: String? = null,
    val body: String? = null,
)

fun defaultPhaseReminders(): Map<ReminderPhase, PhaseReminder> = mapOf(
    ReminderPhase.MENSTRUAL to PhaseReminder(enabled = true),
    ReminderPhase.FOLLICULAR to PhaseReminder(),
    ReminderPhase.OVULATORY to PhaseReminder(),
    ReminderPhase.LUTEAL to PhaseReminder(),
)

/** Non-PHI application preferences (persisted in DataStore). */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val seedColor: Long = 0L, // 0 = built-in pink palette; else derive scheme from this ARGB colour
    val delaySinceEpochDay: Long = 0L, // >0 = delay anchor (epoch day), set when a period is cancelled as 'delay'
    val localeTag: String? = null, // null = follow system
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val periodReminderLeadDays: Int = 2, // lead days for the MENSTRUAL reminder
    val phaseReminders: Map<ReminderPhase, PhaseReminder> = defaultPhaseReminders(),
    val appLockEnabled: Boolean = false,
    val showNotificationDetails: Boolean = false,
    val onboardingComplete: Boolean = false,
    val disclaimerAcceptedVersion: Int = 0,
) {
    fun reminderFor(phase: ReminderPhase): PhaseReminder = phaseReminders[phase] ?: PhaseReminder()
}

/** The disclaimer text version users must accept; bump when wording materially changes. */
const val CURRENT_DISCLAIMER_VERSION: Int = 1
