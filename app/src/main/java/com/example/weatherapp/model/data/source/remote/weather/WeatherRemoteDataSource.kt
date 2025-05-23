package com.example.weatherapp.model.data.source.remote.weather

import android.content.Context
import com.example.weatherapp.model.pojo.CurrentWeatherResponse
import com.example.weatherapp.model.pojo.WeatherResponse
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRemoteDataSource(private val context : Context) : I_WeatherRemoteDataSource {

    enum class ResponseStatus {
        SUCCESS,
        ERROR
    }

    data class WeatherResult(
        var weatherResponse : WeatherResponse? = null,
        var weatherJsonString : String? = null,
        var status: ResponseStatus?
    )

    data class CurrentWeatherResult(
        var currentWeatherResponse : CurrentWeatherResponse? = null,
        var currentWeatherJsonString : String? = null,
        var status: ResponseStatus?
    )

    private val BASE_URL = "https://pro.openweathermap.org/"
    private val weatherService: WeatherService

    init {
        val cacheSize = 10 * 1024 * 1024
        val cache = Cache(context.cacheDir, cacheSize.toLong())
        val okHttpClient = OkHttpClient.Builder().cache(cache).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherService = retrofit.create(WeatherService::class.java)
    }

    override suspend fun getWeather(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String,
        appId: String
    ): WeatherResult {
        val result = WeatherResult(status = ResponseStatus.SUCCESS)
        try {
            val response = weatherService.getWeather(lat, lon, cnt, units, lang, appId)
            if (response.isSuccessful) {
                result.weatherJsonString = response.body()?.string()
                val gson = Gson()
                result.weatherResponse = gson.fromJson(result.weatherJsonString, WeatherResponse::class.java)
                result.status = ResponseStatus.SUCCESS
            } else {
                result.status = ResponseStatus.ERROR
            }
        } catch (e: Exception) {
            result.status = ResponseStatus.ERROR
        }
        return result
    }

    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String,
        appId: String
    ): CurrentWeatherResult {
        val result = CurrentWeatherResult(status = ResponseStatus.SUCCESS)
        try {
            val response = weatherService.getCurrentWeather(lat, lon, units, lang, appId)
            if (response.isSuccessful) {
                result.currentWeatherJsonString = response.body()?.string()
                val gson = Gson()
                result.currentWeatherResponse = gson.fromJson(result.currentWeatherJsonString, CurrentWeatherResponse::class.java)
                result.status = ResponseStatus.SUCCESS
            } else {
                result.status = ResponseStatus.ERROR
            }
        } catch (e: Exception) {
            result.status = ResponseStatus.ERROR
        }
        return result
    }

}