package com.cycletracker.app.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cycletracker.app.notification.NotificationHelper

/** Posts the resolved per-phase reminder notification supplied by [ReminderScheduler]. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.notify(
            applicationContext,
            NotificationHelper.CHANNEL_CYCLE,
            inputData.getString(KEY_TITLE).orEmpty(),
            inputData.getString(KEY_BODY).orEmpty(),
            inputData.getInt(KEY_ID, BASE_ID),
        )
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_ID = "id"
        const val BASE_ID = 1000
    }
}
