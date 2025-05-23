package com.example.weatherapp.model.data.source.local.weather

import com.example.weatherapp.model.pojo.WeatherResponseEntity

interface I_WeatherLocalDataSource {
    suspend fun insertWeather(weather: WeatherResponseEntity): Long
    suspend fun updateWeather(weather: WeatherResponseEntity): Int
    suspend fun deleteWeather(weather: WeatherResponseEntity): Int
    suspend fun getWeatherById(id: Int): WeatherResponseEntity?
    suspend fun getAllWeather(): List<WeatherResponseEntity>?
    suspend fun deleteAllWeather(): Int
}