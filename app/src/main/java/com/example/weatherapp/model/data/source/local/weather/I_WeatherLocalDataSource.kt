package com.example.weatherapp.model.data.source.local.weather

import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.pojo.WeatherResponseEntity

interface I_WeatherLocalDataSource {
    suspend fun insertWeather(weather: WeatherResponseEntity): Long
    suspend fun updateWeather(weather: WeatherResponseEntity): Int
    suspend fun deleteWeather(weather: WeatherResponseEntity): Int
    suspend fun getWeatherById(id: Int): WeatherResponseEntity?
    suspend fun getAllWeather(): List<WeatherResponseEntity>?
    suspend fun deleteAllWeather(): Int
    suspend fun deleteWeatherById(id: Int): Int
    suspend fun insertWeatherAlert(weather: WeatherAlertEntity): Long
    suspend fun updateWeatherAlert(weather: WeatherAlertEntity): Int
    suspend fun deleteWeatherAlert(weather: WeatherAlertEntity): Int
    suspend fun getWeatherAlertById(id: Int): WeatherAlertEntity?
    suspend fun getAllWeatherAlerts(): List<WeatherAlertEntity>?
    suspend fun deleteAllWeatherAlerts(): Int
    suspend fun deleteWeatherAlertById(id: Int): Int
    suspend fun getLastALertID(): Int?
}