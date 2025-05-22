package com.example.weatherapp.model.pojo

import com.google.gson.annotations.SerializedName


data class CurrentWeatherResponse(
    val coord: CurrentCoord,
    val weather: List<CurrentWeather>,
    val base: String,
    val main: CurrentMain,
    val visibility: Long,
    val wind: CurrentWind,
    val clouds: CurrentWind,
    val dt: Long,
    val sys: CurrentSys,
    val timezone: Long,
    val id: Long,
    val name: String,
    val cod: Long,
)

data class CurrentCoord(
    val lon: Double,
    val lat: Double,
)

data class CurrentWeather(
    val id: Long,
    val main: String,
    val description: String,
    val icon: String,
)

data class CurrentMain(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    val pressure: Long,
    val humidity: Long,
    @SerializedName("sea_level")
    val seaLevel: Long,
    @SerializedName("grnd_level")
    val grndLevel: Long,
)

data class CurrentWind(
    val speed: Double,
    val deg: Long,
)

data class CurrentClouds(
    val all: Long,
)

data class CurrentSys(
    val type: Long,
    val id: Long,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
)
