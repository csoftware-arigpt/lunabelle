package com.cycletracker.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.cycletracker.app.data.db.entity.DailyLogEntity
import com.cycletracker.app.data.db.entity.PeriodEntity
import com.cycletracker.app.data.db.entity.SymptomEntryEntity
import com.cycletracker.app.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDao {
    @Query("SELECT * FROM periods ORDER BY startEpochDay ASC")
    fun observeAll(): Flow<List<PeriodEntity>>

    @Query("SELECT * FROM periods ORDER BY startEpochDay ASC")
    suspend fun getAll(): List<PeriodEntity>

    @Upsert
    suspend fun upsert(period: PeriodEntity)

    @Query("DELETE FROM periods WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM periods")
    suspend fun deleteAll()
}

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :dateEpochDay")
    fun observeByDate(dateEpochDay: Long): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay BETWEEN :start AND :end ORDER BY dateEpochDay ASC")
    fun observeBetween(start: Long, end: Long): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay BETWEEN :start AND :end ORDER BY dateEpochDay ASC")
    suspend fun getBetween(start: Long, end: Long): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs")
    suspend fun getAll(): List<DailyLogEntity>

    @Upsert
    suspend fun upsert(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteByDate(dateEpochDay: Long)
}

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptom_entries WHERE dateEpochDay = :dateEpochDay")
    fun observeByDate(dateEpochDay: Long): Flow<List<SymptomEntryEntity>>

    @Query("SELECT * FROM symptom_entries WHERE dateEpochDay BETWEEN :start AND :end")
    fun observeBetween(start: Long, end: Long): Flow<List<SymptomEntryEntity>>

    @Query("SELECT * FROM symptom_entries WHERE dateEpochDay BETWEEN :start AND :end")
    suspend fun getBetween(start: Long, end: Long): List<SymptomEntryEntity>

    @Query("SELECT * FROM symptom_entries")
    suspend fun getAll(): List<SymptomEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SymptomEntryEntity>)

    @Query("DELETE FROM symptom_entries WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteForDate(dateEpochDay: Long)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun observe(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun get(): UserProfileEntity?

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)
}
