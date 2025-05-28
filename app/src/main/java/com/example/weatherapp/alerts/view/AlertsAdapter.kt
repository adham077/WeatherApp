package com.example.weatherapp.alerts.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemAlertBinding
import com.example.weatherapp.model.pojo.WeatherAlertEntity

class AlertsAdapter(items: List<WeatherAlertEntity>?, val onItemDeleted : (Int) -> Unit) : RecyclerView.Adapter<AlertsAdapter.AlertsViewHolder>(){

    class AlertsViewHolder(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)

    var _items : MutableList<WeatherAlertEntity> = items?.toMutableList()?: mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlertsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemAlertBinding.inflate(layoutInflater,parent,false)
        return AlertsViewHolder(binding)
    }

    private fun removeItem(pos : Int){
        _items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun addItem(item: WeatherAlertEntity){
        _items.add(item)
        notifyItemInserted(_items.lastIndex)
    }

    override fun onBindViewHolder(holder: AlertsViewHolder, position: Int) {
        val item = _items[position]
        holder.binding.tvDateTime.text = item.timeAndDate
        holder.binding.tvLocation.text = item.response.weatherResponse.city.name

        holder.binding.btnDelete.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentItem = _items[currentPosition]
                onItemDeleted(currentItem.id)
                Log.i("AlertsAdapter","id: ${currentItem.id}")
                removeItem(currentPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return _items.size
    }

}