package com.cycletracker.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "periods", indices = [Index(value = ["startEpochDay"], unique = true)])
data class PeriodEntity(
    @PrimaryKey val id: String,
    val startEpochDay: Long,
    val endEpochDay: Long?,
)

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val dateEpochDay: Long,
    val flow: Int,
    val clots: Boolean,
    val cervicalMucus: Int,
    val lhTest: Int,
    val sexualActivity: Int,
    val bbtCelsius: Double?,
    val weightKg: Double?,
    val notes: String?,
)

@Entity(
    tableName = "symptom_entries",
    primaryKeys = ["dateEpochDay", "symptomCode"],
    foreignKeys = [
        ForeignKey(
            entity = DailyLogEntity::class,
            parentColumns = ["dateEpochDay"],
            childColumns = ["dateEpochDay"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dateEpochDay")],
)
data class SymptomEntryEntity(
    val dateEpochDay: Long,
    val symptomCode: String,
    val intensity: Int,
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val birthYear: Int?,
    val defaultCycleLength: Int,
    val avgPeriodLength: Int,
    val lutealOffset: Int,
    val predictionWindow: Int,
    val goalMode: Int,
    val heightCm: Int?,
)
