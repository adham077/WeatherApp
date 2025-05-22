package com.example.weatherapp.utils

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