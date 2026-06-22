package com.cycletracker.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cycletracker.app.data.db.dao.DailyLogDao
import com.cycletracker.app.data.db.dao.PeriodDao
import com.cycletracker.app.data.db.dao.ProfileDao
import com.cycletracker.app.data.db.dao.SymptomDao
import com.cycletracker.app.data.db.entity.DailyLogEntity
import com.cycletracker.app.data.db.entity.PeriodEntity
import com.cycletracker.app.data.db.entity.SymptomEntryEntity
import com.cycletracker.app.data.db.entity.UserProfileEntity

@Database(
    entities = [
        PeriodEntity::class,
        DailyLogEntity::class,
        SymptomEntryEntity::class,
        UserProfileEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class CycleDatabase : RoomDatabase() {
    abstract fun periodDao(): PeriodDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun symptomDao(): SymptomDao
    abstract fun profileDao(): ProfileDao
}
