package com.cycletracker.app.data.backup

import kotlinx.serialization.Serializable

const val CURRENT_BACKUP_SCHEMA: Int = 1

@Serializable
data class BackupEnvelope(
    val schemaVersion: Int = CURRENT_BACKUP_SCHEMA,
    val exportedAtEpochMillis: Long = 0,
    val profile: BackupProfile? = null,
    val periods: List<BackupPeriod> = emptyList(),
    val dailyLogs: List<BackupDailyLog> = emptyList(),
)

@Serializable
data class BackupProfile(
    val birthYear: Int? = null,
    val defaultCycleLength: Int = 28,
    val avgPeriodLength: Int = 5,
    val lutealOffset: Int = 13,
    val predictionWindow: Int = 6,
    val goalMode: Int = 0,
    val heightCm: Int? = null,
)

@Serializable
data class BackupPeriod(val id: String, val startEpochDay: Long, val endEpochDay: Long? = null)

@Serializable
data class BackupDailyLog(
    val dateEpochDay: Long,
    val flow: Int = 0,
    val clots: Boolean = false,
    val cervicalMucus: Int = 0,
    val lhTest: Int = 0,
    val sexualActivity: Int = 0,
    val bbtCelsius: Double? = null,
    val weightKg: Double? = null,
    val notes: String? = null,
    val symptoms: List<BackupSymptom> = emptyList(),
)

@Serializable
data class BackupSymptom(val symptomCode: String, val intensity: Int)

sealed interface ImportResult {
    data class Success(val periods: Int, val dailyLogs: Int) : ImportResult
    data object Invalid : ImportResult
    data object UnsupportedVersion : ImportResult
}
