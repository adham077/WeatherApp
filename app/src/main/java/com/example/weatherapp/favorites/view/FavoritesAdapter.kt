package com.example.weatherapp.favorites.view
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemFavoriteBinding
import com.example.weatherapp.model.pojo.WeatherResponseEntity

typealias OnFavoriteItemDetailsCLicked = (itemID : Int) -> Unit
typealias OnFavoriteDeleteCLicked = (itemId : Int,itemIndex : Int) -> Unit

class FavoritesAdapter(
    private var favoritesWeatherList : List<WeatherResponseEntity>?,
    private var onFavoriteItemDetailsCLicked: OnFavoriteItemDetailsCLicked,
    private var onFavoriteItemDeleteCLicked: OnFavoriteDeleteCLicked,
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)

    fun changeDataSet(newFavoritesWeatherList: List<WeatherResponseEntity>?) {
        favoritesWeatherList = newFavoritesWeatherList
        notifyDataSetChanged()
    }

    fun removeItem(itemId: Int, itemIndex: Int)  {
        if (favoritesWeatherList == null || itemIndex < 0 || itemIndex >= favoritesWeatherList!!.size) {
            return
        }
        if (favoritesWeatherList!![itemIndex].id != itemId) {
            return
        }
        if(itemIndex < 0 || itemIndex >= favoritesWeatherList!!.size) {
            return
        }
        val mutableList = favoritesWeatherList?.toMutableList() ?: return
        mutableList.removeAt(itemIndex)
        favoritesWeatherList = mutableList
        notifyItemRangeChanged(itemIndex, favoritesWeatherList!!.size - itemIndex)
        notifyItemRemoved(itemIndex)
    }

    fun addItem(item: WeatherResponseEntity) {
        favoritesWeatherList = favoritesWeatherList?.toMutableList()?.apply { add(item) }
        notifyItemInserted(favoritesWeatherList!!.size - 1)
    }

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

        val item = favoritesWeatherList?.get(position)
        holder.binding.locationName.text = item?.response?.weatherResponse?.city?.name

        holder.binding.btnRemove.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentItem = favoritesWeatherList?.get(currentPosition)
                currentItem?.let {
                    onFavoriteItemDeleteCLicked(it.id, currentPosition)
                }
            }
        }

        holder.binding.btnSelectDetails.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentItem = favoritesWeatherList?.get(currentPosition)
                currentItem?.let {
                    onFavoriteItemDetailsCLicked(it.id)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return favoritesWeatherList?.size?: 0
    }
}