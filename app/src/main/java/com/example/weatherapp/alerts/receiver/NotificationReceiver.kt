package com.example.weatherapp.alerts.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.repository.weather.WeatherRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val alarmID = intent.getIntExtra("ITEM_ID", 0)
        intent.getIntExtra("ALARM_ID", 0)

        when(intent.action){
            "ALARM_DISMISSED" -> {
                GlobalScope.launch(Dispatchers.IO) {
                    WeatherRepository.getInstance(
                        WeatherLocalDataSource(context),
                        WeatherRemoteDataSource(context)
                    ).deleteWeatherAlertById(alarmID)
                }
            }
            "ALARM_ACTION" -> {
                cancelAlarm(alarmID,context)
                GlobalScope.launch(Dispatchers.IO) {
                    WeatherRepository.getInstance(
                        WeatherLocalDataSource(context),
                        WeatherRemoteDataSource(context)
                    ).deleteWeatherAlertById(alarmID)
                }
            }
            else -> {
            }
        }
    }

    private fun cancelAlarm(alarmID : Int,context: Context){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ALARM_ACTION"
            putExtra("ALARM_ID", alarmID)
            putExtra("ITEM_ID", alarmID)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmID,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}