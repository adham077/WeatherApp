package com.example.weatherapp.alerts.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.alerts.receiver.AlertsReceiver
import com.example.weatherapp.notifications.AlarmBroadCastReceiver

class AlertsWorker(val context: Context, val workerParams: WorkerParameters) : Worker(context,workerParams) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val result = Result.success()
        val itemId = inputData.getInt("ITEM_ID", 0)
        val notificationId = inputData.getInt("NOTIFICATION_ID", 0)

        createNotificationChannel(context)

        val openActionIntent = createBroadcastIntent(
            context = context,
            action = "ACTION_OPEN",
            itemId = itemId,
            notificationId = notificationId
        )

        val dismissActionIntent = createBroadcastIntent(
            context = context,
            action = "ACTION_DISMISS",
            itemId = itemId,
            notificationId = notificationId
        )

        val notification = NotificationCompat.Builder(context, "weather_alerts_channel")
            .setContentTitle("Weather Alert")
            .setContentText("weather alert for your area!")
            .setSmallIcon(R.drawable.ic_add)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .addAction(
                R.drawable.ic_add,
                "Open Weather App",
                PendingIntent.getBroadcast(
                    context,
                    0,
                    openActionIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                R.drawable.ic_add,
                "Dismiss",
                PendingIntent.getBroadcast(
                    context,
                    1,
                    dismissActionIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
        return result
    }


    private fun createBroadcastIntent(
        context: Context,
        action: String,
        itemId: Int,
        notificationId: Int
    ): Intent {
        return Intent(context, AlarmBroadCastReceiver::class.java).apply {
            this.action = action
            putExtra("ITEM_ID", itemId)
            putExtra("NOTIFICATION_ID", notificationId)
        }
    }

    private fun createNotificationChannel(context: Context) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val channel = NotificationChannel(
            "weather_alerts_channel",
            "Weather Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for weather alerts"
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

}