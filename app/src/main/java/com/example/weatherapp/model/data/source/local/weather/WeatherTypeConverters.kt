package com.example.weatherapp.model.data.source.local.weather

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.weatherapp.model.pojo.WeatherTimed
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDateTime

class WeatherTypeConverters {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    @TypeConverter
    fun fromWeatherToString(weather: WeatherTimed): String {
        return gson.toJson(weather)
    }

    @TypeConverter
    fun fromStringToWeather(weatherString: String): WeatherTimed {
        return gson.fromJson(weatherString, WeatherTimed::class.java) as WeatherTimed
    }

}