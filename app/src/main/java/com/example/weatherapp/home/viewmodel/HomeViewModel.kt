package com.example.weatherapp.home.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.pojo.WeatherResponseEntity
import com.example.weatherapp.model.pojo.WeatherTimed
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

    private val _savedWeatherResult = MutableLiveData<WeatherResponseEntity?>()
    val savedWeatherResult: LiveData<WeatherResponseEntity?> = _savedWeatherResult

    private val _updateWeatherStatus = MutableLiveData<Boolean>()
    val updateWeatherStatus: LiveData<Boolean> = _updateWeatherStatus

    private val _insertWeatherStatus = MutableLiveData<Boolean>()
    val insertWeatherStatus: LiveData<Boolean> = _insertWeatherStatus

    private val _weatherAlertResult = MutableLiveData<WeatherAlertEntity?>()
    val weatherAlertResult: LiveData<WeatherAlertEntity?> = _weatherAlertResult

    private val _deletedWeatherAlertStatus = MutableLiveData<Boolean>()
    val deletedWeatherAlertStatus: LiveData<Boolean> = _deletedWeatherAlertStatus

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

    fun getSavedWeather(id : Int){
        viewModelScope.launch {
            repository.getWeatherById(id).let {
                result ->
                withContext(Dispatchers.Main) {
                    _savedWeatherResult.postValue(result)
                }
            }
        }
    }

    fun insertWeather(weatherResponseEntity: WeatherResponseEntity){
        viewModelScope.launch {
            repository.insertWeatherData(weatherResponseEntity).let {
                result ->
                withContext(Dispatchers.Main) {
                    if(result >= 0){
                        _insertWeatherStatus.postValue(true)
                    }
                    else{
                        _insertWeatherStatus.postValue(false)
                    }
                }
            }
        }
    }

    fun updateWeather(id : Int, weatherTimed: WeatherTimed) {
        viewModelScope.launch {
            repository.updateWeatherData(id, weatherTimed).let { result ->
                withContext(Dispatchers.Main) {
                    if (result > 0) {
                        _updateWeatherStatus.postValue(true)
                    } else {
                        _updateWeatherStatus.postValue(false)
                    }
                }
            }
        }
    }

    fun getWeatherAlertById(id: Int) {
        viewModelScope.launch {
            repository.getWeatherAlertById(id).let { result ->
                withContext(Dispatchers.Main) {
                    _weatherAlertResult.postValue(result)
                }
            }
        }
    }

    fun deleteWeatherAlertById(id: Int){
        viewModelScope.launch {
            repository.deleteWeatherAlertById(id).let { result->
                withContext(Dispatchers.Main) {
                    if(result>=0){
                        _deletedWeatherAlertStatus.postValue(true)
                    }
                    else{
                        _deletedWeatherAlertStatus.postValue(false)
                    }
                }
            }
        }
    }
}