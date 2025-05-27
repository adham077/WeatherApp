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
import com.example.weatherapp.R
import com.example.weatherapp.notifications.AlarmBroadCastReceiver

class AlertsWorker(val context: Context, val workerParams: WorkerParameters) : Worker(context,workerParams) {
    companion object{
        private const val ACTION_TAG = "com.example.weatherapp.alerts.receiver.AlertsReceiver"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {


        return Result.success()
    }

    private fun sendBroad(notificationID : Int, itemID: Int,dismiss: Boolean) {
        val intent = Intent()
        intent.action = ACTION_TAG
        intent.setPackage("com.example.weatherapp")
        intent.putExtra("DISMISS",dismiss)
        intent.putExtra("NOTIFICATION_ID", notificationID)
        intent.putExtra("ITEM_ID",itemID)
        context.sendBroadcast(intent)
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