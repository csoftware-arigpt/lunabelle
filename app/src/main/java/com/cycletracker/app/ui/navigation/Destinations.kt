package com.cycletracker.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object Lock
@Serializable data object Onboarding
@Serializable data object Disclaimer
@Serializable data object Today
@Serializable data object Calendar
@Serializable data object Insights
@Serializable data object History
@Serializable data object Settings
@Serializable data object About
@Serializable data class LogEntry(val dateEpochDay: Long)
