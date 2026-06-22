package com.cycletracker.app.data.repository

import com.cycletracker.app.data.db.dao.DailyLogDao
import com.cycletracker.app.data.db.dao.PeriodDao
import com.cycletracker.app.data.db.dao.ProfileDao
import com.cycletracker.app.data.db.dao.SymptomDao
import com.cycletracker.app.data.mapper.toDomain
import com.cycletracker.app.data.mapper.toEntity
import com.cycletracker.app.data.mapper.toEpochDay
import com.cycletracker.app.data.prefs.SettingsDataStore
import com.cycletracker.app.domain.model.AppSettings
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.Period
import com.cycletracker.app.domain.model.UserProfile
import com.cycletracker.app.domain.repository.CycleRepository
import com.cycletracker.app.domain.repository.DailyLogRepository
import com.cycletracker.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class CycleRepositoryImpl @Inject constructor(
    private val dao: PeriodDao,
) : CycleRepository {
    override fun observePeriods(): Flow<List<Period>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getPeriods(): List<Period> = dao.getAll().map { it.toDomain() }
    override suspend fun upsertPeriod(period: Period) = dao.upsert(period.toEntity())
    override suspend fun deletePeriod(id: String) = dao.deleteById(id)
}

class DailyLogRepositoryImpl @Inject constructor(
    private val logDao: DailyLogDao,
    private val symptomDao: SymptomDao,
) : DailyLogRepository {

    override fun observeLog(date: LocalDate): Flow<DailyLog?> {
        val epochDay = date.toEpochDay()
        return combine(logDao.observeByDate(epochDay), symptomDao.observeByDate(epochDay)) { log, symptoms ->
            log?.toDomain(symptoms)
        }
    }

    override fun observeLogsBetween(start: LocalDate, end: LocalDate): Flow<List<DailyLog>> {
        val s = start.toEpochDay()
        val e = end.toEpochDay()
        return combine(logDao.observeBetween(s, e), symptomDao.observeBetween(s, e)) { logs, symptoms ->
            val byDate = symptoms.groupBy { it.dateEpochDay }
            logs.map { it.toDomain(byDate[it.dateEpochDay].orEmpty()) }
        }
    }

    override suspend fun getLogsBetween(start: LocalDate, end: LocalDate): List<DailyLog> {
        val s = start.toEpochDay()
        val e = end.toEpochDay()
        val byDate = symptomDao.getBetween(s, e).groupBy { it.dateEpochDay }
        return logDao.getBetween(s, e).map { it.toDomain(byDate[it.dateEpochDay].orEmpty()) }
    }

    override suspend fun upsertLog(log: DailyLog) {
        val epochDay = log.date.toEpochDay()
        if (log.isEmpty) {
            symptomDao.deleteForDate(epochDay)
            logDao.deleteByDate(epochDay)
            return
        }
        logDao.upsert(log.toEntity())
        symptomDao.deleteForDate(epochDay)
        symptomDao.insertAll(log.symptoms.map { it.toEntity(epochDay) })
    }

    override suspend fun deleteLog(date: LocalDate) {
        val epochDay = date.toEpochDay()
        symptomDao.deleteForDate(epochDay)
        logDao.deleteByDate(epochDay)
    }
}

class SettingsRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {
    override fun observeProfile(): Flow<UserProfile> =
        profileDao.observe().map { it?.toDomain() ?: UserProfile() }

    override suspend fun getProfile(): UserProfile = profileDao.get()?.toDomain() ?: UserProfile()
    override suspend fun updateProfile(profile: UserProfile) = profileDao.upsert(profile.toEntity())

    override fun observeSettings(): Flow<AppSettings> = settingsDataStore.settings
    override suspend fun getSettings(): AppSettings = settingsDataStore.settings.first()
    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) =
        settingsDataStore.update(transform)
}
