package com.example.weatherapp.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weatherapp.databinding.ItemHourlyForecastBinding
import com.example.weatherapp.model.pojo.WeatherList

class HourlyForecastAdapter(private var hourlyForecastList : List<WeatherList>?) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    class HourlyViewHolder(val binding: ItemHourlyForecastBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemHourlyForecastBinding.inflate(layoutInflater, parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.binding.hourlyTime.text = hourlyForecastList?.get(position)?.dtTxt
        holder.binding.hourlyTemperature.text = hourlyForecastList?.get(position)?.main?.temp.toString()
    }

    override fun getItemCount(): Int {
        return hourlyForecastList?.size?: 0
    }
}