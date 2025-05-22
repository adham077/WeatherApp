package com.example.weatherapp.model.data.source.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.weatherapp.model.pojo.WeatherTimed
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDateTime

class WeatherTypeConverters {
    @RequiresApi(Build.VERSION_CODES.O)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromWeatherToString(weather: WeatherTimed): String {
        return gson.toJson(weather)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromStringToWeather(weatherString: String): WeatherTimed {
        return gson.fromJson(weatherString, WeatherTimed::class.java) as WeatherTimed
    }

}