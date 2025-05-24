package com.example.weatherapp.favorites.view
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemFavoriteBinding
import com.example.weatherapp.model.pojo.WeatherResponseEntity

typealias OnFavoriteItemDetailsCLicked = (itemID : Int) -> Unit
typealias OnFavoriteDeleteCLicked = (itemId : Int) -> Unit

class FavoritesAdapter(
    private var favoritesWeatherList : List<WeatherResponseEntity>?,
    private var onFavoriteItemDetailsCLicked: OnFavoriteItemDetailsCLicked,
    private var onFavoriteItemDeleteCLicked: OnFavoriteDeleteCLicked,
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavoriteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFavoriteBinding.inflate(layoutInflater, parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FavoriteViewHolder,
        position: Int
    ) {
        holder.binding.btnRemove.setOnClickListener {
            onFavoriteItemDeleteCLicked(favoritesWeatherList!!.get(0).id)
        }
        holder.binding.btnSelectDetails.setOnClickListener {
            onFavoriteItemDetailsCLicked(favoritesWeatherList!!.get(0).id)
        }
        holder.binding.locationName.text = favoritesWeatherList?.get(0)?.response?.weatherResponse?.city?.name
    }

    override fun getItemCount(): Int {
        return favoritesWeatherList?.size?: 0
    }
}