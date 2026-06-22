package com.cycletracker.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycletracker.app.domain.model.CURRENT_DISCLAIMER_VERSION
import com.cycletracker.app.domain.model.GoalMode
import com.cycletracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun complete(cycleLength: Int, periodLength: Int, goalMode: GoalMode, localeTag: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            val profile = settingsRepository.getProfile()
                .copy(defaultCycleLength = cycleLength, avgPeriodLength = periodLength, goalMode = goalMode)
            settingsRepository.updateProfile(profile)
            settingsRepository.updateSettings {
                it.copy(
                    onboardingComplete = true,
                    disclaimerAcceptedVersion = CURRENT_DISCLAIMER_VERSION,
                    localeTag = localeTag,
                )
            }
            onDone()
        }
    }
}

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun accept(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(disclaimerAcceptedVersion = CURRENT_DISCLAIMER_VERSION) }
            onDone()
        }
    }
}
