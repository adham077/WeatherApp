package com.example.weatherapp.model.pojo

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class WeatherTimed(
    var weatherResponse: WeatherResponse,
    var localDateTime: LocalDateTime
)

data class WeatherResponse(
    val cod: String,
    val message: Long,
    val cnt: Long,
    val list: List<WeatherList>,
    val city: City,
)

data class WeatherList(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Long,
    val pop: Double,
    val sys: Sys,
    @SerializedName("dt_txt")
    val dtTxt: String,

)

data class Main(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    val pressure: Long,
    @SerializedName("sea_level")
    val seaLevel: Long,
    @SerializedName("grnd_level")
    val grndLevel: Long,
    val humidity: Long,
    @SerializedName("temp_kf")
    val tempKf: Double,
)

data class Weather(
    val id: Long,
    val main: String,
    val description: String,
    val icon: String,
)

data class Clouds(
    val all: Long,
)

data class Wind(
    val speed: Double,
    val deg: Long,
    val gust: Double,
)

data class Sys(
    val pod: String,
)

data class City(
    val id: Long,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Long,
    val timezone: Long,
    val sunrise: Long,
    val sunset: Long,
)

data class Coord(
    val lat: Double,
    val lon: Double,
)