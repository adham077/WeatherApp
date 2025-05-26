package com.example.weatherapp.alerts.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.example.weatherapp.MainActivity
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.repository.weather.WeatherRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlertsReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val itemId = intent.getIntExtra("ITEM_ID", 0)

        NotificationManagerCompat.from(context).cancel(notificationId)

        when (intent.action) {
            "ACTION_OPEN" -> {
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("SenderID", "AlertsReceiver")
                    putExtra("ITEM_ID", itemId)
                    context.startActivity(this)
                }
            }
            "ACTION_DISMISS" -> {
                GlobalScope.launch(Dispatchers.IO) {
                    WeatherRepository.getInstance(
                        WeatherLocalDataSource(context),
                        WeatherRemoteDataSource(context)
                    ).deleteWeatherAlertById(
                        id = itemId
                    )
                }
            }

            "ACTION_DELETE" ->{
                WorkManager.getInstance(context)
                    .cancelAllWorkByTag("alert_$itemId")
            }
        }
    }
}