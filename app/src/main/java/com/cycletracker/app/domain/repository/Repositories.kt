package com.cycletracker.app.domain.repository

import com.cycletracker.app.domain.model.AppSettings
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.Period
import com.cycletracker.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CycleRepository {
    fun observePeriods(): Flow<List<Period>>
    suspend fun getPeriods(): List<Period>
    suspend fun upsertPeriod(period: Period)
    suspend fun deletePeriod(id: String)
}

interface DailyLogRepository {
    fun observeLog(date: LocalDate): Flow<DailyLog?>
    fun observeLogsBetween(start: LocalDate, end: LocalDate): Flow<List<DailyLog>>
    suspend fun getLogsBetween(start: LocalDate, end: LocalDate): List<DailyLog>
    suspend fun upsertLog(log: DailyLog)
    suspend fun deleteLog(date: LocalDate)
}

interface SettingsRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun getProfile(): UserProfile
    suspend fun updateProfile(profile: UserProfile)

    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
}
