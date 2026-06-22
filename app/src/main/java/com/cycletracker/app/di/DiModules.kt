package com.cycletracker.app.di

import android.content.Context
import androidx.room.Room
import com.cycletracker.app.core.time.AppClock
import com.cycletracker.app.core.time.SystemClock
import com.cycletracker.app.data.crypto.PassphraseManager
import com.cycletracker.app.data.db.CycleDatabase
import com.cycletracker.app.data.prefs.SettingsDataStore
import com.cycletracker.app.data.repository.CycleRepositoryImpl
import com.cycletracker.app.data.repository.DailyLogRepositoryImpl
import com.cycletracker.app.data.repository.SettingsRepositoryImpl
import com.cycletracker.app.domain.insight.InsightGenerator
import com.cycletracker.app.domain.prediction.DefaultPredictionEngine
import com.cycletracker.app.domain.prediction.PredictionEngine
import com.cycletracker.app.domain.repository.CycleRepository
import com.cycletracker.app.domain.repository.DailyLogRepository
import com.cycletracker.app.domain.repository.SettingsRepository
import com.cycletracker.app.domain.stats.StatisticsCalculator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @IoDispatcher fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @DefaultDispatcher fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {
    @Provides @Singleton
    fun passphraseManager(@ApplicationContext context: Context): PassphraseManager =
        PassphraseManager(context)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(
        @ApplicationContext context: Context,
        passphraseManager: PassphraseManager,
    ): CycleDatabase {
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(passphraseManager.getOrCreatePassphrase())
        return Room.databaseBuilder(context, CycleDatabase::class.java, "cycle.db")
            .openHelperFactory(factory)
            .build()
    }

    @Provides fun periodDao(db: CycleDatabase) = db.periodDao()
    @Provides fun dailyLogDao(db: CycleDatabase) = db.dailyLogDao()
    @Provides fun symptomDao(db: CycleDatabase) = db.symptomDao()
    @Provides fun profileDao(db: CycleDatabase) = db.profileDao()
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun settingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)
}

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides @Singleton fun predictionEngine(): PredictionEngine = DefaultPredictionEngine()
    @Provides @Singleton fun statisticsCalculator(engine: PredictionEngine) = StatisticsCalculator(engine)
    @Provides @Singleton fun insightGenerator(engine: PredictionEngine) = InsightGenerator(engine)
    @Provides @Singleton fun appClock(): AppClock = SystemClock()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun cycleRepository(impl: CycleRepositoryImpl): CycleRepository
    @Binds @Singleton abstract fun dailyLogRepository(impl: DailyLogRepositoryImpl): DailyLogRepository
    @Binds @Singleton abstract fun settingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
