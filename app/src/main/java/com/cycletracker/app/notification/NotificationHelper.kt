package com.cycletracker.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cycletracker.app.R

object NotificationHelper {
    const val CHANNEL_CYCLE = "cycle_reminders"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_CYCLE, context.getString(R.string.channel_cycle), NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    /** Branded custom notification (RemoteViews + DecoratedCustomViewStyle), tappable to open the app. */
    fun notify(context: Context, channel: String, title: String, body: String, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val custom = RemoteViews(context.packageName, R.layout.notification_reminder).apply {
            setTextViewText(R.id.notif_title, title)
            setTextViewText(R.id.notif_body, body)
        }
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = launch?.let {
            PendingIntent.getActivity(context, id, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand_primary))
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(custom)
            .setCustomBigContentView(custom)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        pendingIntent?.let {
            builder.setContentIntent(it)
            builder.addAction(0, context.getString(R.string.notif_action_open), it)
        }
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
