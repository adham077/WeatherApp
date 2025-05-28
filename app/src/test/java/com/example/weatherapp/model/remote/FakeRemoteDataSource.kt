package com.example.weatherapp.model.remote

import com.example.weatherapp.model.data.source.remote.weather.I_WeatherRemoteDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource.ResponseStatus
import com.example.weatherapp.model.pojo.City
import com.example.weatherapp.model.pojo.Coord
import com.example.weatherapp.model.pojo.CurrentClouds
import com.example.weatherapp.model.pojo.CurrentCoord
import com.example.weatherapp.model.pojo.CurrentMain
import com.example.weatherapp.model.pojo.CurrentSys
import com.example.weatherapp.model.pojo.CurrentWeatherResponse
import com.example.weatherapp.model.pojo.CurrentWind
import com.example.weatherapp.model.pojo.WeatherResponse
import io.mockk.mockk

class FakeRemoteDataSource(
    private val weatherList : List<WeatherResponse>,
    private val currentWeatherList : List<CurrentWeatherResponse>
    ): I_WeatherRemoteDataSource {

    val mockWeatherResponse = WeatherResponse(
        cod = "200",
        message = 0,
        cnt = 0,
        list = emptyList(),
        city = City(
            id = 0,
            name = "",
            coord = Coord(lat = 37.422, lon = -122.084), // Target coordinates
            country = "",
            population = 0,
            timezone = 0,
            sunrise = 0,
            sunset = 0
        )
    )

    val mockCurrentWeatherResponse = CurrentWeatherResponse(
        coord = CurrentCoord(lon = -122.084, lat = 37.422), // Target coordinates
        weather = emptyList(),
        base = "",
        main = CurrentMain(
            temp = 0.0,
            feelsLike = 0.0,
            tempMin = 0.0,
            tempMax = 0.0,
            pressure = 0,
            humidity = 0,
            seaLevel = 0,
            grndLevel = 0
        ),
        visibility = 0,
        wind = CurrentWind(speed = 0.0, deg = 0),
        clouds = CurrentWind(speed = 0.0, deg = 0),
        dt = 0,
        sys = CurrentSys(
            type = 0,
            id = 0,
            country = "",
            sunrise = 0,
            sunset = 0
        ),
        timezone = 0,
        id = 0,
        name = "",
        cod = 200
    )



    override suspend fun getWeather(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String,
        appId: String
    ): WeatherRemoteDataSource.WeatherResult {
        var weatherResult : WeatherRemoteDataSource.WeatherResult  =
            WeatherRemoteDataSource.WeatherResult(
                weatherResponse = mockWeatherResponse,
                weatherJsonString = "",
                status = ResponseStatus.ERROR
            )
        weatherList.let {
            it.forEach {element->
                if(element.city.coord.lat == lat && element.city.coord.lon == lon){
                    weatherResult.status = ResponseStatus.SUCCESS
                }
            }
        }
        return weatherResult
    }


    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String,
        appId: String
    ): WeatherRemoteDataSource.CurrentWeatherResult {
        var currentWeatherResult: WeatherRemoteDataSource.CurrentWeatherResult  =
            WeatherRemoteDataSource.CurrentWeatherResult(
                currentWeatherResponse = mockCurrentWeatherResponse,
                currentWeatherJsonString = "",
                status = ResponseStatus.ERROR
            )
        currentWeatherList.let {
            it.forEach { element->
                if(element.coord.lat == lat && element.coord.lon == lon){
                    currentWeatherResult.status = ResponseStatus.SUCCESS
                }
            }
        }
        return currentWeatherResult
    }
}