package com.example.weatherapp.model.data.source.remote.weather

interface I_WeatherRemoteDataSource {
    suspend fun getWeather(
        lat: Double,
        lon: Double,
        cnt: Int = 120,
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