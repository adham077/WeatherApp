package com.example.weatherapp.alerts.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.model.repository.weather.WeatherRepository
import kotlinx.coroutines.DelicateCoroutinesApi

class AlertsReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val channel = NotificationChannel(
            "alarm_channel",
            "Alarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val itemID = intent.getIntExtra("ITEM_ID", 0)
        val alarmID =itemID

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmID)
            putExtra("ITEM_ID", itemID)
            putExtra("SENDER_ID", "ALERTS_RECEIVER")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, itemID, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dismissIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ALARM_DISMISSED"
            putExtra("ITEM_ID", itemID)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, itemID * 10 + 1, dismissIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val actionIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ALARM_ACTION"
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context, itemID * 10 + 2, actionIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Weather Alarm!")
            .setContentText("Your alarm is going off.")
            .setContentIntent(mainPendingIntent)
            .setDeleteIntent(dismissPendingIntent)
            .addAction(R.drawable.ic_close, "Stop", actionPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(itemID, notification)
    }
}