package com.example.weatherapp.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.LayoutDirection
import com.example.weatherapp.R
import com.example.weatherapp.model.pojo.WeatherList
import java.util.Locale

val languagesMap = mapOf<String, String>(
    "English" to "en",
    "Arabic" to "ar",
    "German" to "de"
)

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

fun convertCelsiusToKelvin(celsius: Double): Double {
    return celsius + 273.15
}

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

fun covertHpaToMmHg(hpa: Long): Double {
    return hpa * 0.750062
}

fun convertHpaToInHg(hpa: Long): Double {
    return hpa * 0.02953
}

fun convertKmToMiles(km: Double): Double {
    return km * 0.621371
}

fun convertMetertkiloM(meter: Long): Double {
    return meter / 1000.0
}

fun convertMeterToFeet(meter: Long): Double {
    return meter * 3.28084
}

fun updateLocale(languageCode: String, context: Context) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val resources = context.resources
    val config = Configuration(resources.configuration)
    config.setLocale(locale)

    if (languageCode == "ar") {
        //config.layoutDirection = LayoutDirection.RTL
    }

    context.applicationContext.resources.updateConfiguration(config,
        context.applicationContext.resources.displayMetrics)

    resources.updateConfiguration(config, resources.displayMetrics)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.createConfigurationContext(config)
    }
}