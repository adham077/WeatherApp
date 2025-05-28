package com.example.weatherapp.alerts.viewmodel

import androidx.core.view.ContentInfoCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.pojo.WeatherTimed
import com.example.weatherapp.model.repository.weather.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AlertViewModel::class.java)){
            AlertViewModel(repository) as T
        }
        else{
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


class AlertViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _insertedWeatherAlertStatus = MutableLiveData<Boolean>()
    val insertedWeatherAlertStatus: LiveData<Boolean> = _insertedWeatherAlertStatus

    private val _weatherTimed  = MutableLiveData<WeatherTimed?>()
    val weatherTimed: LiveData<WeatherTimed?> = _weatherTimed

    private val _lastAlertId = MutableLiveData<Int?>()
    val lastAlertId : LiveData<Int?> = _lastAlertId

    private val _allWeatherAlerts = MutableLiveData<List<WeatherAlertEntity>?>()
    val allWeatherAlerts : LiveData<List<WeatherAlertEntity>?> = _allWeatherAlerts


    fun getWeather(lat: Double,long: Double){
        viewModelScope.launch {
            repository.getWeatherData(
                WeatherRepository.Source.REMOTE,
                WeatherRepository.Coordinates(lat, long)
            ).let { result ->
                withContext(Dispatchers.Main){
                    when(result.status) {
                        WeatherRepository.Status.SUCCESS -> {
                            _weatherTimed.postValue(result.weatherTimed)
                        }
                        WeatherRepository.Status.ERROR_LOCAL_FETCH -> {
                            _weatherTimed.postValue(null)
                        }
                        WeatherRepository.Status.ERROR_REMOTE_FETCH -> {
                            _weatherTimed.postValue(null)
                        }
                        WeatherRepository.Status.ERROR_INVALID_PARAMS -> {
                            _weatherTimed.postValue(null)
                        }
                        WeatherRepository.Status.ERROR_INVALID_RESPONSE -> {
                            _weatherTimed.postValue(null)
                        }
                    }
                }
            }
        }
    }

    fun insertWeatherAlert(alert: WeatherAlertEntity) {
        viewModelScope.launch {
            repository.insertWeatherAlert(alert).let { result->
                withContext(Dispatchers.Main) {
                    if(result >= 0) {
                        _insertedWeatherAlertStatus.postValue(true)
                    }
                    else {
                        _insertedWeatherAlertStatus.postValue(false)
                    }
                }
            }
        }
    }

    fun getLastWeatherAlertId(){
        viewModelScope.launch {
            repository.getLastWeatherAlertID().let {result->
                withContext(Dispatchers.Main) {
                    _lastAlertId.postValue(result)
                }
            }
        }
    }

    fun getAllAlerts(){
        viewModelScope.launch {
            repository.getAllWeatherAlerts().let { result->
                withContext(Dispatchers.Main) {
                    _allWeatherAlerts.postValue(result)
                }
            }
        }
    }

    fun deleteWeatherAlert(id : Int){
        viewModelScope.launch {
            repository.deleteWeatherAlertById(id)
        }
    }
}