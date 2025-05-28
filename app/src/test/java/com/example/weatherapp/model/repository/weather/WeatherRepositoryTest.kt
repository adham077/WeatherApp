package com.example.weatherapp.model.repository.weather

import com.example.weatherapp.model.local.DummyLocalDataSource
import com.example.weatherapp.model.pojo.City
import com.example.weatherapp.model.pojo.Coord
import com.example.weatherapp.model.pojo.CurrentCoord
import com.example.weatherapp.model.pojo.CurrentMain
import com.example.weatherapp.model.pojo.CurrentSys
import com.example.weatherapp.model.pojo.CurrentWeatherResponse
import com.example.weatherapp.model.pojo.CurrentWind
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.remote.FakeRemoteDataSource
import com.example.weatherapp.model.repository.weather.WeatherRepository.Coordinates
import com.example.weatherapp.model.repository.weather.WeatherRepository.Status
import org.junit.Before
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Test



class WeatherRepositoryTest {

    fun copyWeatherResponseWithCoord(lat: Double, lon: Double): WeatherResponse {
        return WeatherResponse(
            cod = mockWeatherResponse.cod,
            message = mockWeatherResponse.message,
            cnt = mockWeatherResponse.cnt,
            list = mockWeatherResponse.list,
            city = mockWeatherResponse.city.copy(
                coord = mockWeatherResponse.city.coord.copy(lat = lat, lon = lon)
            )
        )
    }

    fun copyCurrentWeatherResponseWithCoord(lat: Double, lon: Double): CurrentWeatherResponse {
        return CurrentWeatherResponse(
            coord = mockCurrentWeatherResponse.coord.copy(lat = lat, lon = lon),
            weather = mockCurrentWeatherResponse.weather,
            base = mockCurrentWeatherResponse.base,
            main = mockCurrentWeatherResponse.main.copy(
                temp = mockCurrentWeatherResponse.main.temp,
                feelsLike = mockCurrentWeatherResponse.main.feelsLike,
                tempMin = mockCurrentWeatherResponse.main.tempMin,
                tempMax = mockCurrentWeatherResponse.main.tempMax,
                pressure = mockCurrentWeatherResponse.main.pressure,
                humidity = mockCurrentWeatherResponse.main.humidity,
                seaLevel = mockCurrentWeatherResponse.main.seaLevel,
                grndLevel = mockCurrentWeatherResponse.main.grndLevel
            ),
            visibility = mockCurrentWeatherResponse.visibility,
            wind = mockCurrentWeatherResponse.wind.copy(
                speed = mockCurrentWeatherResponse.wind.speed,
                deg = mockCurrentWeatherResponse.wind.deg
            ),
            clouds = mockCurrentWeatherResponse.clouds.copy(
                speed = mockCurrentWeatherResponse.clouds.speed,
                deg = mockCurrentWeatherResponse.clouds.deg
            ),
            dt = mockCurrentWeatherResponse.dt,
            sys = mockCurrentWeatherResponse.sys.copy(
                type = mockCurrentWeatherResponse.sys.type,
                id = mockCurrentWeatherResponse.sys.id,
                country = mockCurrentWeatherResponse.sys.country,
                sunrise = mockCurrentWeatherResponse.sys.sunrise,
                sunset = mockCurrentWeatherResponse.sys.sunset
            ),
            timezone = mockCurrentWeatherResponse.timezone,
            id = mockCurrentWeatherResponse.id,
            name = mockCurrentWeatherResponse.name,
            cod = mockCurrentWeatherResponse.cod
        )
    }


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


    private val dummyLocalDataSource = DummyLocalDataSource()
    private lateinit var weatherRepository: WeatherRepository

    private val weatherList = listOf<WeatherResponse>(
        copyWeatherResponseWithCoord(37.422, -122.084),
        copyWeatherResponseWithCoord(0.0, 0.0),
        copyWeatherResponseWithCoord(30.0, 30.0),
    )

    private val currentWeatherList = listOf<CurrentWeatherResponse>(
        copyCurrentWeatherResponseWithCoord(37.422, -122.084),
        copyCurrentWeatherResponseWithCoord(0.0,0.0),
        copyCurrentWeatherResponseWithCoord(30.0,30.0)
    )


    @Before
    fun setup() {
        WeatherRepository.resetInstance()
        weatherRepository = WeatherRepository.getInstance(
            dummyLocalDataSource,
            FakeRemoteDataSource(
                weatherList,
                currentWeatherList
            )
        )
    }

    @After
    fun close(){
        WeatherRepository.resetInstance()
    }

    @Test
    fun getWeatherData_StatusSuccess_SrcRemotelat30lon30(){
        runTest {
            val result = weatherRepository.getWeatherData(
                WeatherRepository.Source.REMOTE,
                Coordinates(30.0,30.0)
            )
            assertThat(result.status,`is`(Status.SUCCESS))
        }
    }

    @Test
    fun getWeatherData_StatusError_Remote_Fetch_SrcRemotelat3lon3(){
        runTest {
            val result = weatherRepository.getWeatherData(
                WeatherRepository.Source.REMOTE,
                Coordinates(3.0,3.0)
            )
            assertThat(result.status,`is`(Status.ERROR_REMOTE_FETCH))
        }
    }

    @Test
    fun getCurrentWeatherData_StatusSuccess_lat30lon30(){
        runTest {
            val result = weatherRepository.getCurrentWeatherData(Coordinates(30.0,30.0))
            assertThat(result.status,`is`(Status.SUCCESS))
        }
    }

    @Test
    fun getCurrentWeatherData_StatusError_Remote_Fetch_lat3lon3(){
        runTest {
            val result = weatherRepository.getCurrentWeatherData(Coordinates(3.0,3.0))
            assertThat(result.status,`is`(Status.ERROR_REMOTE_FETCH))
        }
    }
}