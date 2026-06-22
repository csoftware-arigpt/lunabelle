package com.cycletracker.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycletracker.app.data.backup.BackupManager
import com.cycletracker.app.data.backup.ImportResult
import com.cycletracker.app.domain.model.AppSettings
import com.cycletracker.app.domain.model.ReminderPhase
import com.cycletracker.app.domain.model.ThemeMode
import com.cycletracker.app.domain.repository.SettingsRepository
import com.cycletracker.app.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> =
        settingsRepository.observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setDynamicColor(enabled: Boolean) = update { it.copy(dynamicColor = enabled) }
    fun setSeedColor(color: Long) = update { it.copy(seedColor = color) }
    fun setLanguage(tag: String?) = update { it.copy(localeTag = tag) }
    fun setAppLock(enabled: Boolean) = update { it.copy(appLockEnabled = enabled) }

    fun setPhaseEnabled(phase: ReminderPhase, enabled: Boolean) = updateAndReschedule { s ->
        s.copy(phaseReminders = s.phaseReminders + (phase to s.reminderFor(phase).copy(enabled = enabled)))
    }

    fun setPeriodLeadDays(days: Int) = updateAndReschedule { it.copy(periodReminderLeadDays = days.coerceIn(0, 14)) }

    /** Persist all per-phase custom title/body text (blank = use default). */
    fun savePhaseTexts(titles: Map<ReminderPhase, String>, bodies: Map<ReminderPhase, String>) =
        updateAndReschedule { s ->
            s.copy(
                phaseReminders = ReminderPhase.entries.associateWith { phase ->
                    s.reminderFor(phase).copy(
                        title = titles[phase]?.ifBlank { null },
                        body = bodies[phase]?.ifBlank { null },
                    )
                },
            )
        }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.updateSettings(transform) }
    }

    private fun updateAndReschedule(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(transform)
            runCatching { reminderScheduler.reschedule() }
        }
    }

    suspend fun exportJson(nowMillis: Long): String = backupManager.exportToJson(nowMillis)
    suspend fun importJson(content: String): ImportResult = backupManager.importFromJson(content, replace = false)
}
