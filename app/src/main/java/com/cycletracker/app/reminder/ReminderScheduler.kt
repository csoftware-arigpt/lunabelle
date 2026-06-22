package com.cycletracker.app.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.cycletracker.app.R
import com.cycletracker.app.core.time.AppClock
import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.model.ReminderPhase
import com.cycletracker.app.domain.prediction.PredictionEngine
import com.cycletracker.app.domain.repository.CycleRepository
import com.cycletracker.app.domain.repository.SettingsRepository
import com.cycletracker.app.ui.common.formatMedium
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Token users can place in custom reminder text; replaced with the predicted date. */
const val DATE_TOKEN = "{date}"

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cycleRepository: CycleRepository,
    private val settingsRepository: SettingsRepository,
    private val engine: PredictionEngine,
    private val clock: AppClock,
) {
    suspend fun reschedule() {
        val wm = WorkManager.getInstance(context)
        ReminderPhase.entries.forEach { wm.cancelUniqueWork(workName(it)) }

        val settings = settingsRepository.getSettings()
        val periods = cycleRepository.getPeriods()
        val today = clock.today()

        ReminderPhase.entries.forEach { phase ->
            val reminder = settings.reminderFor(phase)
            if (!reminder.enabled) return@forEach
            val start = engine.nextPhaseStart(CyclePhase.valueOf(phase.name), periods, today) ?: return@forEach
            val fireDate = if (phase == ReminderPhase.MENSTRUAL) {
                start.minus(DatePeriod(days = settings.periodReminderLeadDays))
            } else {
                start
            }
            val dateText = start.formatMedium()
            val title = reminder.title?.ifBlank { null }?.replace(DATE_TOKEN, dateText)
                ?: context.getString(defaultTitleRes(phase))
            val body = reminder.body?.ifBlank { null }?.replace(DATE_TOKEN, dateText)
                ?: context.getString(defaultBodyRes(phase), dateText)
            enqueue(wm, workName(phase), phase, today.daysUntil(fireDate), title, body)
        }
    }

    private fun enqueue(wm: WorkManager, name: String, phase: ReminderPhase, daysFromNow: Int, title: String, body: String) {
        if (daysFromNow < 0) return
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(daysFromNow.toLong(), TimeUnit.DAYS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_TITLE to title,
                    ReminderWorker.KEY_BODY to body,
                    ReminderWorker.KEY_ID to ReminderWorker.BASE_ID + phase.ordinal,
                ),
            )
            .build()
        wm.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private fun workName(phase: ReminderPhase) = "reminder_${phase.name}"

    private fun defaultTitleRes(phase: ReminderPhase): Int = when (phase) {
        ReminderPhase.MENSTRUAL -> R.string.notif_menstrual_title
        ReminderPhase.FOLLICULAR -> R.string.notif_follicular_title
        ReminderPhase.OVULATORY -> R.string.notif_ovulatory_title
        ReminderPhase.LUTEAL -> R.string.notif_luteal_title
    }

    private fun defaultBodyRes(phase: ReminderPhase): Int = when (phase) {
        ReminderPhase.MENSTRUAL -> R.string.notif_menstrual_body
        ReminderPhase.FOLLICULAR -> R.string.notif_follicular_body
        ReminderPhase.OVULATORY -> R.string.notif_ovulatory_body
        ReminderPhase.LUTEAL -> R.string.notif_luteal_body
    }
}
