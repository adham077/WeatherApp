package com.example.weatherapp.utils

import com.example.weatherapp.R
import com.example.weatherapp.model.pojo.WeatherList

val weatherIconsMap = mapOf<String, Int>(
    "01d" to R.drawable.ic_oned,
    "01n" to R.drawable.ic_onen,
    "02d" to R.drawable.ic_twod,
    "02n" to R.drawable.ic_twon,
    "03d" to R.drawable.ic_threed,
    "03n" to R.drawable.ic_threen,
    "04d" to R.drawable.ic_fourd,
    "09d" to R.drawable.ic_nined,
    "09n" to R.drawable.ic_ninen,
    "10d" to R.drawable.ic_tend,
    "10n" to R.drawable.ic_tenn,
    "11d" to R.drawable.ic_elevend,
    "11n" to R.drawable.ic_elevenn,
    "13d" to R.drawable.ic_thirteend,
    "13n" to R.drawable.ic_thirteenn,
)

val daysList : List<String> = listOf(
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
)

data class TempAverages(
    val min: Double,
    val max: Double,
    val avg: Double
)

data class ForecastItem(
    var tempAverages: TempAverages? = null,
    var weatherList: List<WeatherList>? = null
)

fun convertKelvinToCelsius(kelvin: Double): Double {
    return kelvin - 273.15
}

fun convertKelvinToFahrenheit(kelvin: Double): Double {
    return (kelvin - 273.15) * 9 / 5 + 32
}

fun convertCelsiusToFahrenheit(celsius: Double): Double {
    return (celsius * 9 / 5) + 32
}

fun convertFahrenheitToCelsius(fahrenheit: Double): Double {
    return (fahrenheit - 32) * 5 / 9
}