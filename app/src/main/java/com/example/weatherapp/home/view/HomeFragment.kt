package com.example.weatherapp.home.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.home.viewmodel.HomeViewModel
import com.example.weatherapp.home.viewmodel.HomeViewModelFactory
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.pojo.WeatherResponseEntity
import com.example.weatherapp.model.repository.weather.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority



class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var viewModel: HomeViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val args : HomeFragmentArgs by navArgs()
    val lat = args.lat
    val long = args.long
    val senderId = args.senderID
    val itemId = args.itemId
    val fromGps = args.fromGps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = HomeViewModelFactory(
            WeatherRepository.getInstance(
                WeatherLocalDataSource(requireContext()),
                WeatherRemoteDataSource(requireContext())
            )
        )
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        if(senderId == "InitialSetupFragment"){
            viewModel.getWeather(
                WeatherRepository.Source.REMOTE,
                WeatherRepository.Coordinates(lat.toDouble(),long.toDouble())
            )
            viewModel.weatherResult.observe(viewLifecycleOwner){
                if(it.status == WeatherRepository.Status.SUCCESS){
                    viewModel.getSavedWeather(0)
                    viewModel.savedWeatherResult.observe(viewLifecycleOwner){ savedWeather ->
                        if(savedWeather == null){
                            viewModel.insertWeather(
                                WeatherResponseEntity(
                                    id = 0,
                                    response = it.weatherTimed!!
                                )
                            )
                        }
                        else{

                        }
                    }
                }
                else{

                }
            }
        }
    }

}