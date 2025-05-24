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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.home.viewmodel.HomeViewModel
import com.example.weatherapp.home.viewmodel.HomeViewModelFactory
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.pojo.CurrentWeatherResponse
import com.example.weatherapp.model.pojo.WeatherList
import com.example.weatherapp.model.pojo.WeatherResponseEntity
import com.example.weatherapp.model.pojo.WeatherTimed
import com.example.weatherapp.model.repository.weather.WeatherRepository
import com.example.weatherapp.utils.ForecastItem
import com.example.weatherapp.utils.TempAverages
import com.example.weatherapp.utils.convertKelvinToCelsius
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var viewModel: HomeViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private var lat: Float? = null
    private var long: Float? = null
    private var senderId: String? = null
    private var itemId: Int = 0
    private var fromGps: Boolean = false
    private var updatedFromRemote : MutableLiveData<Boolean> = MutableLiveData()
    private lateinit var currentWeather : CurrentWeatherResponse
    private lateinit var weatherTimed : WeatherTimed

    private enum class Units{
        METRIC,
        IMPERIAL,
        STANDARD
    }


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

        val safeArgs = arguments?.let { HomeFragmentArgs.fromBundle(it) }

        lat = safeArgs?.lat
        long = safeArgs?.long
        senderId = safeArgs?.senderID
        itemId = safeArgs?.itemId ?: 0
        fromGps = safeArgs?.fromGps ?: false

        val factory = HomeViewModelFactory(
            WeatherRepository.getInstance(
                WeatherLocalDataSource(requireContext()),
                WeatherRemoteDataSource(requireContext())
            )
        )
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        if(senderId == "InitialSetupFragment"){
            updateWeatherData(0)
        }

        updatedFromRemote.observe(viewLifecycleOwner) {
            if(it){
                setupMainCard(currentWeather, Units.METRIC)
                lifecycleScope.launch(Dispatchers.Default) {
                    val lists = setupLists()
                    val hourlyForecastList = lists.first
                    val fiveDayForecastMap = lists.second
                    /*
                    * Setup hourly & 5day recyclers
                    * */
                }
            }
        }
    }

    private fun updateWeatherData(id : Int){
        viewModel.getWeather(
            WeatherRepository.Source.REMOTE,
            WeatherRepository.Coordinates(lat!!.toDouble(),long!!.toDouble())
        )
        viewModel.weatherResult.observe(viewLifecycleOwner){
            if(it.status == WeatherRepository.Status.SUCCESS){
                viewModel.getSavedWeather(id)
                viewModel.savedWeatherResult.observe(viewLifecycleOwner){ savedWeather ->
                    if(savedWeather == null){
                        viewModel.insertWeather(
                            WeatherResponseEntity(
                                id = id,
                                response = it.weatherTimed!!
                            )
                        )
                    }
                    else{
                        viewModel.updateWeather(
                            savedWeather.id,
                            it.weatherTimed!!
                        )
                    }
                    weatherTimed = it.weatherTimed!!
                    viewModel.getCurrentWeather(WeatherRepository.Coordinates(lat!!.toDouble(),long!!.toDouble()))
                    viewModel.currentWeatherResult.observe(viewLifecycleOwner) {
                        if(it.status == WeatherRepository.Status.SUCCESS){
                            currentWeather = it.currentWeatherResponse!!
                            updatedFromRemote.postValue(true)
                        }
                        else {
                            updatedFromRemote.postValue(false)
                        }
                    }
                }
            }
            else{

            }
        }
    }

    private fun setupMainCard(currentWeather : CurrentWeatherResponse,units : Units){
        binding.cityName.text = "${currentWeather.name}, ${currentWeather.sys.country}"
        var temp : Double = 0.0
        var highTemp : Double = 0.0
        var lowTemp : Double = 0.0
        var humidity : Long = 0
        var pressure : Long = 0
        var windSpeed : Double = 0.0
        var visibility : Long = 0
        var seaLevel = currentWeather.main.seaLevel
        var grndLevel = currentWeather.main.grndLevel
        var sunset  = currentWeather.sys.sunset
        var sunrise = currentWeather.sys.sunrise
        if(units == Units.METRIC){
            temp = (currentWeather.main.temp)
            highTemp = (currentWeather.main.tempMax)
            lowTemp = (currentWeather.main.tempMin)
            visibility = currentWeather.visibility / 1000
            pressure = currentWeather.main.pressure
            windSpeed = currentWeather.wind.speed
            humidity = currentWeather.main.humidity
        }

        val localDateTime = weatherTimed.localDateTime
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss")
        val formatted = localDateTime.format(formatter)

        binding.lastUpdate.text = "Last Update: " + formatted
        binding.currentTemperature.text = "${temp.toInt()} °C"
        binding.weatherDescription.text = currentWeather.weather[0].description
        binding.highTemp.text = "High: ${highTemp.toInt()} °C"
        binding.lowTemp.text = "Low: ${lowTemp.toInt()} °C"
        binding.cityName.text = "${currentWeather.name}, ${currentWeather.sys.country}"
        binding.humidity.text = "${humidity}%"
        binding.pressure.text = "${pressure} hPa"
        binding.windSpeed.text = "${windSpeed} m/s"
        binding.seaLevel.text = "${seaLevel} m"
        binding.groundLevel.text = "${grndLevel} m"
    }

    private fun setup5DayRecycler(){

    }

    private fun setupHourlyRecycler(){

    }

    private suspend fun setupLists() : Pair<List<WeatherList>,Map<String, ForecastItem>> {
        var hourlyForecastList : MutableList<WeatherList> = mutableListOf()
        var fiveDayForecastMap : MutableMap<String, ForecastItem> = mutableMapOf()

        val _fiveDayForecastMap : MutableMap<String, MutableList<WeatherList>> = mutableMapOf()

        val currentTime = LocalDateTime.now()

        for(i in weatherTimed.weatherResponse.list.indices){
            if(weatherTimed.weatherResponse.list[i].dtTxt.isNotEmpty()){
                val weatherList = weatherTimed.weatherResponse.list[i]
                val dateTime = LocalDateTime.parse(weatherList.dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                if(dateTime.isAfter(currentTime) && dateTime.isBefore(currentTime.plusDays(1))){
                    hourlyForecastList.add(weatherList)
                }
                else if(dateTime.isAfter(currentTime.plusDays(1)) && dateTime.isBefore(currentTime.plusDays(6))){
                    val dayOfWeek = dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    if(!_fiveDayForecastMap.containsKey(dayOfWeek)){
                        _fiveDayForecastMap[dayOfWeek] = mutableListOf()
                    }
                    else{
                        _fiveDayForecastMap[dayOfWeek]!!.add(weatherList)
                    }
                }
            }
        }

        for((day, weatherList) in _fiveDayForecastMap){
            val forecastItem = calculateAverages(weatherList)
            fiveDayForecastMap[day] = forecastItem
        }

        return Pair(hourlyForecastList, fiveDayForecastMap)
    }

    private suspend fun calculateAverages(weatherList: List<WeatherList>) : ForecastItem {
        var max : Double
        var min : Double
        var avg : Double
        if(weatherList.isNotEmpty()){
            max = weatherList.maxOf { it.main.tempMax }
            min = weatherList.minOf { it.main.tempMin }
            avg = weatherList.map { it.main.temp }.average()
        } else {
            max = 0.0
            min = 0.0
            avg = 0.0
        }
        return ForecastItem(
            tempAverages =  TempAverages(
                min = min,
                max = max,
                avg = avg,
            ),
            weatherList = weatherList
        )
    }

}