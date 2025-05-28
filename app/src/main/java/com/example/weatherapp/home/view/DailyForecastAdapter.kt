package com.example.weatherapp.home.view

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ScaleDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.weatherapp.databinding.ItemDailyForecastBinding
import com.example.weatherapp.model.pojo.WeatherList
import com.example.weatherapp.utils.ForecastItem
import com.example.weatherapp.utils.convertCelsiusToFahrenheit
import com.example.weatherapp.utils.convertCelsiusToKelvin
import com.example.weatherapp.utils.daysList
import java.time.LocalDateTime

class DailyForecastAdapter(private var dailyForecastList : Map<String, ForecastItem>?,private val units: HomeFragment.Units) : RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder>() {

    private val orderedKeys: List<String> = dailyForecastList?.keys?.toList() ?: listOf()


    class DailyViewHolder(val binding: ItemDailyForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDailyForecastBinding.inflate(layoutInflater, parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        var forecastItem : ForecastItem? = null
        var day = ""
        day = if(position == 0){
             "Today"
        }
        else{
            orderedKeys[position]
        }
        forecastItem = dailyForecastList?.get(day)

        holder.binding.dailyDate.text = day

        if(units.temperature == "Celsius") {
            holder.binding.dailyLowTemp.text = "L: ${forecastItem?.tempAverages?.min?.toInt()} °C"
            holder.binding.dailyHighTemp.text = "H: ${forecastItem?.tempAverages?.max?.toInt()} °C"
        }
        else if(units.temperature == "Fahrenheit"){
            holder.binding.dailyLowTemp.text = "L: ${convertCelsiusToFahrenheit(forecastItem?.tempAverages?.min!!).toInt()} °F"
            holder.binding.dailyHighTemp.text = "H: ${convertCelsiusToFahrenheit(forecastItem?.tempAverages?.max!!).toInt()} °F"
        }
        else{
            holder.binding.dailyLowTemp.text = "L: ${convertCelsiusToKelvin(forecastItem?.tempAverages?.min!!).toInt()} °K"
            holder.binding.dailyHighTemp.text = "H: ${convertCelsiusToKelvin(forecastItem?.tempAverages?.max!!).toInt()} °K"
        }
        val tempAverages = forecastItem?.tempAverages

        if(tempAverages!=null){
            val min = tempAverages.min
            val max = tempAverages.max
            val avg = tempAverages.avg
            val range = max - min
            val minToAvgPercent = if (range != 0.0) {
                ((avg - min) / range) * 100
            } else {
                0.0
            }

            val avgToMaxPercent = if(range != 0.0){
                ((max - avg)/range) * 100
            }
            else{
                0.0
            }

            holder.binding.temperatureRangeBar.progress = avgToMaxPercent.toInt()

            holder.binding.root.post {
                val barWidth = holder.binding.temperatureRangeBar.width
                if (barWidth > 0) {
                    val newWidth = (barWidth * minToAvgPercent / 100).toInt()
                    val params = holder.binding.startMask.layoutParams
                    params.width = newWidth
                    holder.binding.startMask.layoutParams = params
                }
            }
        }

    }


    override fun getItemCount(): Int {
        return orderedKeys.size
    }

}