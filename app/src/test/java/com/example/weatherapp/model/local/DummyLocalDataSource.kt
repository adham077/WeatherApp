package com.example.weatherapp.model.local

import com.example.weatherapp.model.data.source.local.weather.I_WeatherLocalDataSource
import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.pojo.WeatherResponseEntity

class DummyLocalDataSource : I_WeatherLocalDataSource {
    override suspend fun insertWeather(weather: WeatherResponseEntity): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateWeather(weather: WeatherResponseEntity): Int {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeather(weather: WeatherResponseEntity): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getWeatherById(id: Int): WeatherResponseEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllWeather(): List<WeatherResponseEntity>? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllWeather(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeatherById(id: Int): Int {
        TODO("Not yet implemented")
    }

    override suspend fun insertWeatherAlert(weather: WeatherAlertEntity): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateWeatherAlert(weather: WeatherAlertEntity): Int {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeatherAlert(weather: WeatherAlertEntity): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getWeatherAlertById(id: Int): WeatherAlertEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllWeatherAlerts(): List<WeatherAlertEntity>? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllWeatherAlerts(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeatherAlertById(id: Int): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getLastALertID(): Int? {
        TODO("Not yet implemented")
    }
}