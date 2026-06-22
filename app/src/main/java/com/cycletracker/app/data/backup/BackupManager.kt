package com.cycletracker.app.data.backup

import androidx.room.withTransaction
import com.cycletracker.app.data.db.CycleDatabase
import com.cycletracker.app.data.db.dao.DailyLogDao
import com.cycletracker.app.data.db.dao.PeriodDao
import com.cycletracker.app.data.db.dao.ProfileDao
import com.cycletracker.app.data.db.dao.SymptomDao
import com.cycletracker.app.data.db.entity.DailyLogEntity
import com.cycletracker.app.data.db.entity.PeriodEntity
import com.cycletracker.app.data.db.entity.SymptomEntryEntity
import com.cycletracker.app.data.db.entity.UserProfileEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Fully-offline JSON backup/restore. The caller supplies the file stream via the Storage Access Framework. */
class BackupManager @Inject constructor(
    private val db: CycleDatabase,
    private val periodDao: PeriodDao,
    private val dailyLogDao: DailyLogDao,
    private val symptomDao: SymptomDao,
    private val profileDao: ProfileDao,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportToJson(nowEpochMillis: Long): String {
        val symptomsByDate = symptomDao.getAll().groupBy { it.dateEpochDay }
        val envelope = BackupEnvelope(
            schemaVersion = CURRENT_BACKUP_SCHEMA,
            exportedAtEpochMillis = nowEpochMillis,
            profile = profileDao.get()?.let {
                BackupProfile(
                    it.birthYear, it.defaultCycleLength, it.avgPeriodLength,
                    it.lutealOffset, it.predictionWindow, it.goalMode, it.heightCm,
                )
            },
            periods = periodDao.getAll().map { BackupPeriod(it.id, it.startEpochDay, it.endEpochDay) },
            dailyLogs = dailyLogDao.getAll().map { l ->
                BackupDailyLog(
                    dateEpochDay = l.dateEpochDay, flow = l.flow, clots = l.clots,
                    cervicalMucus = l.cervicalMucus, lhTest = l.lhTest, sexualActivity = l.sexualActivity,
                    bbtCelsius = l.bbtCelsius, weightKg = l.weightKg, notes = l.notes,
                    symptoms = symptomsByDate[l.dateEpochDay].orEmpty()
                        .map { BackupSymptom(it.symptomCode, it.intensity) },
                )
            },
        )
        return json.encodeToString(envelope)
    }

    suspend fun importFromJson(content: String, replace: Boolean): ImportResult {
        val envelope = runCatching { json.decodeFromString<BackupEnvelope>(content) }
            .getOrElse { return ImportResult.Invalid }
        if (envelope.schemaVersion > CURRENT_BACKUP_SCHEMA) return ImportResult.UnsupportedVersion

        if (replace) db.clearAllTables()
        db.withTransaction {
            envelope.profile?.let { p ->
                profileDao.upsert(
                    UserProfileEntity(
                        0, p.birthYear, p.defaultCycleLength, p.avgPeriodLength,
                        p.lutealOffset, p.predictionWindow, p.goalMode, p.heightCm,
                    ),
                )
            }
            envelope.periods.forEach { periodDao.upsert(PeriodEntity(it.id, it.startEpochDay, it.endEpochDay)) }
            envelope.dailyLogs.forEach { l ->
                dailyLogDao.upsert(
                    DailyLogEntity(
                        l.dateEpochDay, l.flow, l.clots, l.cervicalMucus, l.lhTest,
                        l.sexualActivity, l.bbtCelsius, l.weightKg, l.notes,
                    ),
                )
                symptomDao.deleteForDate(l.dateEpochDay)
                symptomDao.insertAll(l.symptoms.map { SymptomEntryEntity(l.dateEpochDay, it.symptomCode, it.intensity) })
            }
        }
        return ImportResult.Success(envelope.periods.size, envelope.dailyLogs.size)
    }
}
