package com.example.weatherapp.utils

import com.example.weatherapp.model.pojo.WeatherList

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