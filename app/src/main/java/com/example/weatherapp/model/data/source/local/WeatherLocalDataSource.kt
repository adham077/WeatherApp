package com.example.weatherapp.model.data.source.local

import android.content.Context
import com.example.weatherapp.model.pojo.WeatherResponseEntity

class WeatherLocalDataSource(private val context: Context) : I_WeatherLocalDataSource{

    private val weatherDao: WeatherDAO by lazy {
        WeatherDataBase.getInstance(context).weatherDao()
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
}