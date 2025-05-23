package com.example.weatherapp.home.view

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ScaleDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.weatherapp.databinding.ItemDailyForecastBinding
import com.example.weatherapp.model.pojo.WeatherList

class DailyForecastAdapter(private var dailyForecastList : List<WeatherList>?) : RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder>() {

    class DailyViewHolder(val binding: ItemDailyForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDailyForecastBinding.inflate(layoutInflater, parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {

        val tempMin = dailyForecastList?.get(position)?.main?.tempMin ?: 0.0
        val tempMax = dailyForecastList?.get(position)?.main?.tempMax ?: 0.0

        holder.binding.dailyLowTemp.text = "L: %.0f°C".format(tempMin)
        holder.binding.dailyHighTemp.text = "H: %.0f°C".format(tempMax)

    }


    override fun getItemCount(): Int {
        return dailyForecastList?.size ?: 0
    }


    companion object{
        private const val MIN_POSSIBLE_TEMP = 23.0
        private const val MAX_POSSIBLE_TEMP = 40.0
    }
}