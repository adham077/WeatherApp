package com.example.weatherapp.model.data.source.remote

import com.example.weatherapp.model.pojo.WeatherResponse

interface I_WeatherRemoteDataSource {
    suspend fun getWeather(
        lat: Double,
        lon: Double,
        cnt: Int = 7,
        units: String = "metric",
        lang: String = "en",
        appId: String
    ): WeatherRemoteDataSource.WeatherResult

    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en",
        appId: String
    ): WeatherRemoteDataSource.CurrentWeatherResult

}