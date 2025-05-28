package com.example.weatherapp.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ItemHourlyForecastBinding
import com.example.weatherapp.model.pojo.WeatherList
import com.example.weatherapp.utils.convertCelsiusToFahrenheit
import com.example.weatherapp.utils.convertCelsiusToKelvin
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HourlyForecastAdapter(private var hourlyForecastList : List<WeatherList>?,private val zoneId: ZoneId,private val units : HomeFragment.Units) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    class HourlyViewHolder(val binding: ItemHourlyForecastBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemHourlyForecastBinding.inflate(layoutInflater, parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {

        val weatherList = hourlyForecastList?.get(position)

        val dateTime = LocalDateTime.parse(
            weatherList!!.dtTxt,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        ).atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime()

        holder.binding.hourlyTime.text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        if(units.temperature == "Celsius") {
            holder.binding.hourlyTemperature.text = hourlyForecastList?.get(position)?.main?.temp?.toInt().toString() + "°C"
        }
        else if(units.temperature == "Fahrenheit"){
            holder.binding.hourlyTemperature.text = convertCelsiusToFahrenheit(hourlyForecastList?.get(position)?.main?.temp!!).toInt().toString() + "F"

        }
        else{
            holder.binding.hourlyTemperature.text = convertCelsiusToKelvin(hourlyForecastList?.get(position)?.main?.temp!!).toInt().toString() + "K"
        }

        val drawableId = hourlyForecastList?.get(position)?.weather?.get(0)?.icon?.let { icon ->
            com.example.weatherapp.utils.weatherIconsMap[icon]
        } ?: 0
        Glide.with(holder.binding.root.context).load(drawableId).into(holder.binding.hourlyWeatherIcon)
    }

    override fun getItemCount(): Int {
        return hourlyForecastList?.size?: 0
    }

}