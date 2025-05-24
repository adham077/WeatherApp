package com.example.weatherapp.favorites.viewmodel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.pojo.WeatherResponseEntity
import com.example.weatherapp.model.repository.weather.WeatherRepository
import kotlinx.coroutines.launch

class FavoriteViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)){
            FavoriteViewModel(repository) as T
        }
        else{
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


class FavoriteViewModel(private val repository: WeatherRepository) : ViewModel(){

    enum class OperationStatus{
        SUCCESS,
        ERROR
    }

    data class FavoriteLocationsResponse(
        val operationStatus : OperationStatus,
        val favoriteLocationsWeather : List<WeatherResponseEntity>?
    )

    private val _saveLocationLiveData = MutableLiveData<Boolean>()
    val saveLocationLiveData : LiveData<Boolean> = _saveLocationLiveData

    private val _deleteLocationLiveData = MutableLiveData<Boolean>()
    val deleteLocationLiveData : LiveData<Boolean> = _deleteLocationLiveData

    private val _favoriteLocationsLiveData = MutableLiveData<FavoriteLocationsResponse>()
    val favoriteLocationsLiveData : LiveData<FavoriteLocationsResponse> = _favoriteLocationsLiveData

    fun saveLocationWeather(lat : Double, lon: Double) {
        viewModelScope.launch {
            val result = repository.getWeatherData(
                WeatherRepository.Source.REMOTE,
                WeatherRepository.Coordinates(lat, lon)
            )
            Log.i("FavoriteViewModel", "saveLocationWeather: lat = $lat, lon = $lon")

            Log.i("FavoriteViewModel", "saveLocationWeather: ${result.status}")

            if(result.status == WeatherRepository.Status.SUCCESS){
                val weather = result.weatherTimed
                if (weather != null  ) {
                    if(repository.insertWeatherData(weather) > 0){
                        _saveLocationLiveData.postValue(true)
                    }
                    else {
                        _saveLocationLiveData.postValue(false)
                    }
                }
                else {
                    _saveLocationLiveData.postValue(false)
                }
            }
            else {
                _saveLocationLiveData.postValue(false)
            }
        }
    }

    fun getFavoriteLocationsWeather(){
        viewModelScope.launch {
            val result = repository.getAllFavWeather()
            val factoryLocationsResponse: FavoriteLocationsResponse = if(result != null){
                FavoriteLocationsResponse(
                    operationStatus = OperationStatus.SUCCESS,
                    favoriteLocationsWeather = result
                )
            }
            else{
                FavoriteLocationsResponse(
                    operationStatus = OperationStatus.ERROR,
                    favoriteLocationsWeather = null
                )
            }
            _favoriteLocationsLiveData.postValue(factoryLocationsResponse)
        }
    }

    fun deleteLocationWeather(id : Int){
        viewModelScope.launch {
            val result = repository.deleteWeatherData(id)
            if (result>= 0) {
                _deleteLocationLiveData.postValue(true)
            } else {
                _deleteLocationLiveData.postValue(false)
            }
        }
    }

}