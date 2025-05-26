package com.example.weatherapp.model.data.source.local.weather

import android.content.Context
import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.pojo.WeatherResponseEntity

class WeatherLocalDataSource(private val context: Context) : I_WeatherLocalDataSource {

    private val weatherDao: WeatherDAO by lazy {
        WeatherDataBase.getInstance(context).weatherDao()
    }

    private val weatherAlertsDao : WeatherAlertsDao by lazy {
        WeatherDataBase.getInstance(context).weatherAlertsDao()
    }

    override suspend fun insertWeather(weather: WeatherResponseEntity): Long {
        return weatherDao.insertWeather(weather)
    }

    override suspend fun updateWeather(weather: WeatherResponseEntity): Int {
        return weatherDao.updateWeather(weather)
    }

    override suspend fun deleteWeather(weather: WeatherResponseEntity): Int {
        return weatherDao.deleteWeather(weather)
    }

    override suspend fun getWeatherById(id: Int): WeatherResponseEntity? {
        return weatherDao.getWeatherById(id)
    }

    override suspend fun getAllWeather(): List<WeatherResponseEntity>? {
        return weatherDao.getAllWeather()
    }

    override suspend fun deleteAllWeather(): Int {
        return weatherDao.deleteAllWeather()
    }

    override suspend fun deleteWeatherById(id: Int): Int {
        return weatherDao.deleteWeatherById(id)
    }

    override suspend fun insertWeatherAlert(weather: WeatherAlertEntity): Long {
        return weatherAlertsDao.insertWeather(weather)
    }

    override suspend fun updateWeatherAlert(weather: WeatherAlertEntity): Int {
        return weatherAlertsDao.updateWeather(weather)
    }

    override suspend fun deleteWeatherAlert(weather: WeatherAlertEntity): Int {
        return weatherAlertsDao.deleteWeather(weather)
    }

    override suspend fun getWeatherAlertById(id: Int): WeatherAlertEntity? {
        return weatherAlertsDao.getWeatherById(id)
    }

    override suspend fun getAllWeatherAlerts(): List<WeatherAlertEntity>? {
        return weatherAlertsDao.getAllWeather()
    }

    override suspend fun deleteAllWeatherAlerts(): Int {
        return weatherAlertsDao.deleteAllWeather()
    }

    override suspend fun deleteWeatherAlertById(id: Int): Int {
        return weatherAlertsDao.deleteWeatherById(id)
    }
}