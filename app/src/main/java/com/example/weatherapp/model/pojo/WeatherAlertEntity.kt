package com.example.weatherapp.model.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherapp.model.data.source.local.weather.WeatherTypeConverters

@Entity(tableName = "weather_alerts_table")
data class WeatherAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @TypeConverters(WeatherTypeConverters::class)
    var response : WeatherTimed,

    var timeAndDate : String
)