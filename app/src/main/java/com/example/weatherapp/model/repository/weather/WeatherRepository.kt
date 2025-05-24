package com.example.weatherapp.model.repository.weather

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.weatherapp.key.apiKey
import com.example.weatherapp.model.data.source.local.weather.I_WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.I_WeatherRemoteDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.pojo.CurrentWeatherResponse
import com.example.weatherapp.model.pojo.WeatherResponseEntity
import com.example.weatherapp.model.pojo.WeatherTimed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class WeatherRepository private constructor(private val weatherLocalDataSource: I_WeatherLocalDataSource, private val weatherRemoteDataSource: I_WeatherRemoteDataSource) {

    companion object{
        private var instance: WeatherRepository? = null

        fun getInstance(weatherLocalDataSource: I_WeatherLocalDataSource, weatherRemoteDataSource: I_WeatherRemoteDataSource): WeatherRepository {
            return instance ?: synchronized(this) {
                val newInstance = WeatherRepository(weatherLocalDataSource,weatherRemoteDataSource)
                instance = newInstance
                newInstance
            }
        }
    }

    enum class Status{
        SUCCESS,
        ERROR_LOCAL_FETCH,
        ERROR_REMOTE_FETCH,
        ERROR_INVALID_PARAMS,
        ERROR_INVALID_RESPONSE
    }

    enum class Source{
        LOCAL,
        REMOTE
    }

    data class Coordinates(
        var lat: Double,
        var lon: Double
    )

    data class WeatherResult(
        var weatherTimed : WeatherTimed?,
        var status: Status
    )

    data class CurrentWeatherResult(
        var currentWeatherResponse : CurrentWeatherResponse?,
        var status: Status
    )


    suspend fun getWeatherData(
        src : Source,
        coordinates: Coordinates? = null,
        cnt: Int = 96,
        units: String = "metric",
    ) : WeatherResult = withContext(Dispatchers.IO) {
        val status : Status
        var weatherTimed : WeatherTimed? = null
        val weatherResult: WeatherResult
        when(src){
            Source.LOCAL -> {
                val weatherResponseEntity = weatherLocalDataSource.getAllWeather()?.get(0)
                weatherTimed = weatherResponseEntity?.response

                status = if(weatherTimed == null){
                    Status.ERROR_LOCAL_FETCH
                } else{
                    Status.SUCCESS
                }
                weatherResult = WeatherResult(weatherTimed,status)
            }
            Source.REMOTE -> {
                if(coordinates == null || coordinates.lat == 0.0 || coordinates.lon == 0.0){
                    status = Status.ERROR_INVALID_PARAMS
                    weatherResult = WeatherResult(weatherTimed,status)
                }
                else{
                    val _weatherResponse = weatherRemoteDataSource.getWeather(coordinates.lat,coordinates.lon,cnt = cnt,units = units, appId = apiKey)
                    if(_weatherResponse.weatherResponse == null){
                        status = Status.ERROR_REMOTE_FETCH
                        weatherResult = WeatherResult(weatherTimed,status)
                        Log.i("WeatherRepository", "getWeatherData: Remote fetch error: ${_weatherResponse.status}")

                    }
                    else if(_weatherResponse.weatherResponse!!.cod != "200"){
                        status = Status.ERROR_INVALID_RESPONSE
                        weatherResult = WeatherResult(weatherTimed,status)
                        Log.i("WeatherRepository", "getWeatherData: No data found for the given coordinates")

                    }
                    else if(_weatherResponse.status != WeatherRemoteDataSource.ResponseStatus.SUCCESS){
                        status = Status.ERROR_REMOTE_FETCH
                        weatherResult = WeatherResult(weatherTimed,status)
                        Log.i("WeatherRepository", "getWeatherData: Remote fetch error: ${_weatherResponse.status}")

                    }
                    else if(_weatherResponse.weatherResponse!!.list.isEmpty()){
                        status = Status.ERROR_INVALID_RESPONSE
                        weatherResult = WeatherResult(weatherTimed,status)
                        Log.i("WeatherRepository", "getWeatherData: No data found for the given coordinates")
                    }
                    else{
                        weatherTimed = WeatherTimed(
                            _weatherResponse.weatherResponse!!,
                            LocalDateTime.now()
                        )
                        status = Status.SUCCESS
                        weatherResult = WeatherResult(weatherTimed,status)
                    }
                }
            }

        }
        weatherResult
    }

    suspend fun getCurrentWeatherData(
        coorindates: Coordinates
    ) : CurrentWeatherResult = withContext(Dispatchers.IO) {
        val status : Status
        var currentWeatherResponse : CurrentWeatherResponse? = null
        val currentWeatherResult: CurrentWeatherResult

        val weatherResponse = weatherRemoteDataSource.getCurrentWeather(
            coorindates.lat,
            coorindates.lon,
            appId = apiKey
        )

        if(weatherResponse.currentWeatherResponse == null){
            status = Status.ERROR_REMOTE_FETCH
            currentWeatherResult = CurrentWeatherResult(currentWeatherResponse,status)
        }
        else if(weatherResponse.currentWeatherResponse!!.cod.toInt() != 200){
            status = Status.ERROR_INVALID_RESPONSE
            currentWeatherResult = CurrentWeatherResult(currentWeatherResponse,status)
        }
        else if(weatherResponse.status != WeatherRemoteDataSource.ResponseStatus.SUCCESS){
            status = Status.ERROR_REMOTE_FETCH
            currentWeatherResult = CurrentWeatherResult(currentWeatherResponse,status)
        }
        else{
            currentWeatherResponse = weatherResponse.currentWeatherResponse
            status = Status.SUCCESS
            currentWeatherResult = CurrentWeatherResult(currentWeatherResponse,status)
        }
        currentWeatherResult
    }


    suspend fun insertWeatherData(
        weatherResponseEntity: WeatherResponseEntity
    ): Long = withContext(Dispatchers.IO){
        weatherLocalDataSource.insertWeather(weatherResponseEntity)
    }

    suspend fun insertWeatherData(
        weatherTimed: WeatherTimed
    ) : Long = withContext(Dispatchers.IO){
        weatherLocalDataSource.insertWeather(WeatherResponseEntity(response = weatherTimed))
    }

    suspend fun updateWeatherData(
        id : Int,
        weatherTimed: WeatherTimed
    ): Int = withContext(Dispatchers.IO){
        weatherLocalDataSource.updateWeather(WeatherResponseEntity(id,weatherTimed))
    }

    suspend fun deleteWeatherData(
        id: Int
    ): Int = withContext(Dispatchers.IO){
        weatherLocalDataSource.deleteWeatherById(id)
    }

    suspend fun deleteWeatherData(
        weatherTimed: WeatherTimed
    ): Int = withContext(Dispatchers.IO){
        weatherLocalDataSource.deleteWeather(WeatherResponseEntity(response = weatherTimed))
    }

    suspend fun deleteAllWeatherData(): Int = withContext(Dispatchers.IO){
        weatherLocalDataSource.deleteAllWeather()
    }

    suspend fun getAllFavWeather() : List<WeatherResponseEntity>? = withContext(Dispatchers.IO) {
        weatherLocalDataSource.getAllWeather()
    }

    suspend fun getWeatherById(id: Int): WeatherResponseEntity? = withContext(Dispatchers.IO) {
        weatherLocalDataSource.getWeatherById(id)
    }
}