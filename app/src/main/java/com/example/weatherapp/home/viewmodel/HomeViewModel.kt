package com.example.weatherapp.home.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.repository.weather.WeatherRepository
import com.example.weatherapp.model.repository.weather.WeatherRepository.Coordinates
import com.example.weatherapp.model.repository.weather.WeatherRepository.CurrentWeatherResult
import com.example.weatherapp.model.repository.weather.WeatherRepository.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
            HomeViewModel(repository) as T
        }
        else{
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _currentWeatherResult = MutableLiveData<CurrentWeatherResult>()
    val currentWeatherResult : LiveData<CurrentWeatherResult> = _currentWeatherResult

    private val _weatherResult = MutableLiveData<WeatherRepository.WeatherResult>()
    val weatherResult : LiveData<WeatherRepository.WeatherResult> = _weatherResult


    fun getCurrentWeather(coordinates: WeatherRepository.Coordinates) {
        viewModelScope.launch {
            repository.getCurrentWeatherData(
                coordinates
            ).let { result->
                withContext(Dispatchers.Main) {
                    _currentWeatherResult.postValue(result)
                }
            }
        }
    }

    fun getWeather(
        src : Source,
        coordinates: Coordinates? = null,
        cnt: Int = 96,
        units: String = "metric"
    ) {
        viewModelScope.launch {
            repository.getWeatherData(
                src,
                coordinates,
                cnt,
                units
            ).let { result ->
                withContext(Dispatchers.Main) {
                    _weatherResult.postValue(result)
                }
            }
        }
    }
}