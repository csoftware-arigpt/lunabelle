package com.cycletracker.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycletracker.app.core.lock.AppLockManager
import com.cycletracker.app.domain.model.AppSettings
import com.cycletracker.app.domain.repository.SettingsRepository
import com.cycletracker.app.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> =
        settingsRepository.observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val authenticated: StateFlow<Boolean> = AppLockManager.authenticated

    init {
        // Refresh reminder schedule on each app start (predictions may have changed).
        viewModelScope.launch { runCatching { reminderScheduler.reschedule() } }
    }
}
