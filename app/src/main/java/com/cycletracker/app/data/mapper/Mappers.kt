package com.cycletracker.app.data.mapper

import com.cycletracker.app.data.db.entity.DailyLogEntity
import com.cycletracker.app.data.db.entity.PeriodEntity
import com.cycletracker.app.data.db.entity.SymptomEntryEntity
import com.cycletracker.app.data.db.entity.UserProfileEntity
import com.cycletracker.app.domain.model.CervicalMucus
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.FlowLevel
import com.cycletracker.app.domain.model.GoalMode
import com.cycletracker.app.domain.model.Intensity
import com.cycletracker.app.domain.model.LhTest
import com.cycletracker.app.domain.model.Period
import com.cycletracker.app.domain.model.SexualActivity
import com.cycletracker.app.domain.model.SymptomEntry
import com.cycletracker.app.domain.model.UserProfile
import kotlinx.datetime.LocalDate

fun LocalDate.toEpochDay(): Long = this.toEpochDays().toLong()
fun Long.toLocalDate(): LocalDate = LocalDate.fromEpochDays(this.toInt())

private inline fun <reified T : Enum<T>> Int.toEnum(default: T): T =
    enumValues<T>().getOrElse(this) { default }

fun PeriodEntity.toDomain(): Period =
    Period(id = id, start = startEpochDay.toLocalDate(), end = endEpochDay?.toLocalDate())

fun Period.toEntity(): PeriodEntity =
    PeriodEntity(id = id, startEpochDay = start.toEpochDay(), endEpochDay = end?.toEpochDay())

fun SymptomEntryEntity.toDomain(): SymptomEntry =
    SymptomEntry(symptomCode = symptomCode, intensity = intensity.toEnum(Intensity.NONE))

fun SymptomEntry.toEntity(dateEpochDay: Long): SymptomEntryEntity =
    SymptomEntryEntity(dateEpochDay = dateEpochDay, symptomCode = symptomCode, intensity = intensity.ordinal)

fun DailyLogEntity.toDomain(symptoms: List<SymptomEntryEntity>): DailyLog =
    DailyLog(
        date = dateEpochDay.toLocalDate(),
        flow = flow.toEnum(FlowLevel.NONE),
        clots = clots,
        cervicalMucus = cervicalMucus.toEnum(CervicalMucus.NONE_DRY),
        lhTest = lhTest.toEnum(LhTest.NOT_TAKEN),
        sexualActivity = sexualActivity.toEnum(SexualActivity.NONE),
        bbtCelsius = bbtCelsius,
        weightKg = weightKg,
        notes = notes,
        symptoms = symptoms.map { it.toDomain() },
    )

fun DailyLog.toEntity(): DailyLogEntity =
    DailyLogEntity(
        dateEpochDay = date.toEpochDay(),
        flow = flow.ordinal,
        clots = clots,
        cervicalMucus = cervicalMucus.ordinal,
        lhTest = lhTest.ordinal,
        sexualActivity = sexualActivity.ordinal,
        bbtCelsius = bbtCelsius,
        weightKg = weightKg,
        notes = notes,
    )

fun UserProfileEntity.toDomain(): UserProfile =
    UserProfile(
        birthYear = birthYear,
        defaultCycleLength = defaultCycleLength,
        avgPeriodLength = avgPeriodLength,
        lutealOffsetDays = lutealOffset,
        predictionWindow = predictionWindow,
        goalMode = goalMode.toEnum(GoalMode.TRACKING_ONLY),
        heightCm = heightCm,
    )

fun UserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        id = 0,
        birthYear = birthYear,
        defaultCycleLength = defaultCycleLength,
        avgPeriodLength = avgPeriodLength,
        lutealOffset = lutealOffsetDays,
        predictionWindow = predictionWindow,
        goalMode = goalMode.ordinal,
        heightCm = heightCm,
    )
