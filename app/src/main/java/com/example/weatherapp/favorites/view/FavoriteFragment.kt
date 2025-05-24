package com.example.weatherapp.favorites.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.databinding.FragmentFavoriteBinding
import com.example.weatherapp.favorites.viewmodel.FavoriteViewModel
import com.example.weatherapp.favorites.viewmodel.FavoriteViewModelFactory
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.repository.weather.WeatherRepository
import com.google.android.material.snackbar.Snackbar

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var viewModel : FavoriteViewModel
    private lateinit var adapter: FavoritesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = FavoriteViewModelFactory(
            WeatherRepository.getInstance(
                WeatherLocalDataSource(requireContext()),
                WeatherRemoteDataSource(requireContext())
            )
        )
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        viewModel.getFavoriteLocationsWeather()

        viewModel.favoriteLocationsLiveData.observe(viewLifecycleOwner) { weatherResponse ->
            if(weatherResponse.operationStatus == FavoriteViewModel.OperationStatus.SUCCESS){
                binding.favoritesRecycler.layoutManager = LinearLayoutManager(requireContext())
                adapter = FavoritesAdapter(
                    weatherResponse.favoriteLocationsWeather,
                    onFavoriteItemDeleteCLicked = { itemId, itemIndex ->
                        viewModel.deleteLocationWeather(itemId)
                        viewModel.deleteLocationLiveData.observe(viewLifecycleOwner) {
                            if (it == true) {
                                adapter.removeItem(itemId, itemIndex)
                            }
                            else{
                                Snackbar.make(binding.root, "Error deleting item", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onFavoriteItemDetailsCLicked = {

                    }
                )
                binding.favoritesRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.favoritesRecycler.adapter = adapter
            }
        }
    }
}