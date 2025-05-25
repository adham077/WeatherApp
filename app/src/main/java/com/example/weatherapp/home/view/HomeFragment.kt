package com.example.weatherapp.home.view
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.example.weatherapp.model.repository.weather.WeatherRepository.Source
import com.example.weatherapp.utils.ForecastItem
import com.example.weatherapp.utils.TempAverages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
    private var fetchedFromLocal : MutableLiveData<Boolean> = MutableLiveData()
    private lateinit var currentWeather : CurrentWeatherResponse
    private lateinit var weatherTimed : WeatherTimed

    data class Units(
        val temperature: String = "Celsius",
        val speed: String = "m/s",
        val pressure: String = "hPa",
        val visibility: String = "km",
        val seaLevel : String = "m",
    )

    private lateinit var units : Units


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*SafeArgs Initialization*/
        lat = arguments?.getFloat("lat")
        long = arguments?.getFloat("long")
        senderId = arguments?.getString("senderID")?: "defaultSender"
        itemId = arguments?.getInt("itemId") ?: 1
        fromGps = arguments?.getBoolean("fromGps") == true
        sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", 0)

        /*Units Initialization*/
        units = Units(
            temperature = sharedPreferences.getString("temperatureUnit", "Celsius") ?: "Celsius",
            speed = sharedPreferences.getString("speedUnit", "m/s") ?: "m/s",
            pressure = sharedPreferences.getString("pressureUnit", "hPa") ?: "hPa",
            visibility = sharedPreferences.getString("visibilityUnit", "km") ?: "km",
            seaLevel = sharedPreferences.getString("seaLevelUnit", "m") ?: "m",
        )

        /*ViewModel Initialization*/
        val viewModelFactory = HomeViewModelFactory(
            WeatherRepository.getInstance(
                WeatherLocalDataSource(requireContext()),
                WeatherRemoteDataSource(requireContext())
            )
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*ViewBinding Initialization*/
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("HomeFragment", "onViewCreated: lat=$lat, long=$long, senderId=$senderId, itemId=$itemId, fromGps=$fromGps")

        if(senderId == "InitialSetupFragment"){
            Log.i("HomeFragment", "${lat} ${long}")
            fetchFromRemote(Pair(lat?: 0.0f,long?: 0.0f))
            updatedFromRemote.observe(viewLifecycleOwner) {
                if(it){
                    updateWeatherData(1, weatherTimed)
                    setupViewOnline(units, weatherTimed, currentWeather)
                }
                else{
                    fetchFromLocal(1)
                    fetchedFromLocal.observe(viewLifecycleOwner) {
                        if(it){
                            setupOfflineView(units, weatherTimed)
                        }
                        else{
                            Toast.makeText(
                                requireContext(),
                                "Failed to fetch weather data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        else if(senderId == "SavedWeatherFragment"){
            Log.i("HomeFragment", "Favorite: itemId=$itemId, lat=$lat, long=$long")
            var hasHandledSavedWeather = false
            fetchFromLocal(itemId)
            fetchedFromLocal.observe(viewLifecycleOwner) {
                if(it && !hasHandledSavedWeather){
                    hasHandledSavedWeather = true
                    val lat = weatherTimed.weatherResponse.city.coord.lat.toFloat()
                    val long = weatherTimed.weatherResponse.city.coord.lon.toFloat()
                    fetchFromRemote(Pair(lat, long))
                    updatedFromRemote.observe(viewLifecycleOwner) { result ->
                        if(result){
                            updateWeatherData(
                                itemId,
                                weatherTimed
                            )
                            setupViewOnline(units, weatherTimed, currentWeather)
                        }
                        else{
                            setupOfflineView(units, weatherTimed)
                        }
                    }
                }
                else if(!it){
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch saved weather data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        else{
            lat = sharedPreferences.getFloat("userLat", 0.0f)
            long = sharedPreferences.getFloat("userLon", 0.0f)
            Log.i("HomeFragment", "User Location: lat=$lat, long=$long")
            if(lat != null && long != null){
                fetchFromRemote(Pair(lat!!, long!!))
                updatedFromRemote.observe(viewLifecycleOwner) { result ->
                    if(result){
                        updateWeatherData(1, weatherTimed)
                        setupViewOnline(units, weatherTimed, currentWeather)
                    }
                    else{
                        fetchFromLocal(1)
                        fetchedFromLocal.observe(viewLifecycleOwner) {
                            if(it){
                                setupOfflineView(units, weatherTimed)
                            }
                            else{
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to fetch weather data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            else{
                Toast.makeText(
                    requireContext(),
                    "Location not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun fetchFromRemote(coordinates: Pair<Float, Float>) {
        Log.i("HomeFragment", "Fetching weather data from remote")
        viewModel.getCurrentWeather(WeatherRepository.Coordinates(
            coordinates.first.toDouble(),
            coordinates.second.toDouble())
        )
        viewModel.currentWeatherResult.observe(viewLifecycleOwner) { result ->
            if(result.status != WeatherRepository.Status.SUCCESS){
                updatedFromRemote.postValue(false)
                Log.i("HomeFragment", "Failed to fetch current weather data from remote")
            }
            else{
                currentWeather = result.currentWeatherResponse!!
                viewModel.getWeather(
                    Source.REMOTE,
                    WeatherRepository.Coordinates(
                        coordinates.first.toDouble(),
                        coordinates.second.toDouble()
                    )
                )
                viewModel.weatherResult.observe(viewLifecycleOwner) {result->
                    if(result.status != WeatherRepository.Status.SUCCESS){
                        updatedFromRemote.postValue(false)
                        Log.i("HomeFragment", "Failed to fetch weather data from remote")
                    }
                    else{
                        weatherTimed = result.weatherTimed!!
                        updatedFromRemote.postValue(true)
                        Log.i("HomeFragment", "Successfully fetched weather data from remote")
                    }
                }
            }
        }
    }

    private fun fetchFromLocal(id : Int){
        viewModel.getSavedWeather(id)
        viewModel.savedWeatherResult.observe(viewLifecycleOwner) {
            result ->
            if(result != null){
                weatherTimed = result.response
                fetchedFromLocal.postValue(true)
            }
            else{
                fetchedFromLocal.postValue(false)
            }
        }
    }

    private fun updateWeatherData(id : Int, weatherTimed: WeatherTimed) {
        viewModel.getSavedWeather(id)
        viewModel.savedWeatherResult.observe(viewLifecycleOwner) {
            result ->
            if(result == null){
                viewModel.insertWeather(
                    WeatherResponseEntity(
                        response = weatherTimed,
                        id = id
                    )
                )
            }
            else{
                viewModel.updateWeather(
                    id = id,
                    weatherTimed = weatherTimed
                )
            }
        }
    }

    private fun setupOfflineView(units: Units,weatherTimed: WeatherTimed){
        lifecycleScope.launch(Dispatchers.Default) {
            val lists = setupLists(weatherTimed)
            val hourlyForecastList = lists.first
            val fiveDayForecastMap = lists.second
            val maxTemp = hourlyForecastList.maxOf { it.main.tempMax }
            val minTemp = hourlyForecastList.minOf { it.main.tempMin }
            val currentTemp = hourlyForecastList.get(0).main.temp
            val currentPressure = hourlyForecastList.get(0).main.pressure
            val currentVisibility = hourlyForecastList.get(0).visibility
            val currentSeaLevel = hourlyForecastList.get(0).main.seaLevel
            val currentGroundLevel = hourlyForecastList.get(0).main.grndLevel
            val zoneId = getZoneId(weatherTimed.weatherResponse.city.timezone)
            val sunsetTime_ = Instant.ofEpochSecond(weatherTimed.weatherResponse.city.sunset)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId)
                .toLocalDateTime()

            val sunriseTime_ = Instant.ofEpochSecond(weatherTimed.weatherResponse.city.sunrise)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId)
                .toLocalDateTime()
            withContext(Dispatchers.Main) {
                binding.apply {
                    sunsetTime.text = sunsetTime_.format(DateTimeFormatter.ofPattern("HH:mm"))
                    sunriseTime.text = sunriseTime_.format(DateTimeFormatter.ofPattern("HH:mm"))
                    lastUpdate.text = "Last Update: ${weatherTimed.localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"
                    cityName.text = "${weatherTimed.weatherResponse.city.name}, ${weatherTimed.weatherResponse.city.country}"
                    if(units.temperature == "Celsius") {
                        currentTemperature.text = "${currentTemp.toInt()}°C"
                        lowTemp.text = "Low: ${minTemp.toInt()}°C"
                        highTemp.text = "High: ${maxTemp.toInt()}°C"
                    }
                    if(units.pressure == "hPa") {
                        pressure.text = "$currentPressure hPa"
                    }
                    if(units.speed == "m/s") {
                        windSpeed.text = "${weatherTimed.weatherResponse.list[0].wind.speed} m/s"
                    }
                    if(units.visibility == "km") {
                        visibility.text = "${currentVisibility / 1000} km"
                    }
                    if(units.seaLevel == "m") {
                        seaLevel.text = "${currentSeaLevel} m"
                        groundLevel.text = "${currentGroundLevel} m"
                    }
                    setup5DayRecycler(fiveDayForecastMap)
                    setupHourlyRecycler(hourlyForecastList, getZoneId(weatherTimed.weatherResponse.city.timezone))
                }
            }
        }
    }


    private fun setupViewOnline(units: Units,weatherTimed: WeatherTimed,currentWeather: CurrentWeatherResponse){
        lifecycleScope.launch(Dispatchers.Default) {
            val (hourlyForecastList, fiveDayForecastMap) = setupLists(weatherTimed)

            val zoneID = getZoneId(weatherTimed.weatherResponse.city.timezone)
            val sunsetTime_ = Instant.ofEpochSecond(weatherTimed.weatherResponse.city.sunset)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneID)
                .toLocalDateTime()

            val sunriseTime_ = Instant.ofEpochSecond(weatherTimed.weatherResponse.city.sunrise)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneID)
                .toLocalDateTime()

            withContext(Dispatchers.Main) {
                binding.apply {
                    lastUpdate.text = "Last Update: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"
                    cityName.text = "${currentWeather.name}, ${currentWeather.sys.country}"
                    sunsetTime.text = sunsetTime_.format(DateTimeFormatter.ofPattern("HH:mm"))
                    sunriseTime.text = sunriseTime_.format(DateTimeFormatter.ofPattern("HH:mm"))
                    if(units.temperature == "Celsius") {
                        currentTemperature.text = "${currentWeather.main.temp.toInt()}°C"
                        lowTemp.text = "Low: ${currentWeather.main.tempMin.toInt()}°C"
                        highTemp.text = "High: ${currentWeather.main.tempMax.toInt()}°C"
                    }
                    if(units.pressure == "hPa") {
                        pressure.text = "${currentWeather.main.pressure} hPa"
                    }
                    if(units.speed == "m/s") {
                        windSpeed.text = "${currentWeather.wind.speed} m/s"
                    }
                    if(units.visibility == "km") {
                        visibility.text = "${currentWeather.visibility / 1000} km"
                    }
                    if(units.seaLevel == "m") {
                        seaLevel.text = "${currentWeather.main.seaLevel} m"
                        groundLevel.text = "${currentWeather.main.grndLevel} m"
                    }
                }
                setup5DayRecycler(fiveDayForecastMap)
                setupHourlyRecycler(hourlyForecastList, getZoneId(weatherTimed.weatherResponse.city.timezone))
            }
        }
    }

    private fun setup5DayRecycler(weatherMap :Map<String, ForecastItem>){
        val dailyForecastAdapter = DailyForecastAdapter(weatherMap)
        val layoutMan = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.dailyForecastRecycler.apply {
            adapter = dailyForecastAdapter
            layoutManager = layoutMan
            setHasFixedSize(true)
        }
    }

    private fun setupHourlyRecycler(weatherList: List<WeatherList>,zoneId : ZoneId){
        val hourlyForecastAdapter = HourlyForecastAdapter(weatherList,zoneId)
        val layoutMan = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.hourlyForecastRecycler.apply {
            adapter = hourlyForecastAdapter
            layoutManager = layoutMan
            setHasFixedSize(true)
        }
    }

    private suspend fun setupLists(weatherTimed: WeatherTimed) : Pair<List<WeatherList>,Map<String, ForecastItem>> {
        val zoneId = getZoneId(weatherTimed.weatherResponse.city.timezone)
        val localTimeInZone = getLocalTimeFromZoneOffset(weatherTimed.weatherResponse.city.timezone)
        var hourlyForecastList : MutableList<WeatherList> = mutableListOf()
        var fiveDayForecastMap : MutableMap<String, ForecastItem> = mutableMapOf()
        val _fiveDayForecastMap : MutableMap<String, MutableList<WeatherList>> = mutableMapOf()
        for(i in weatherTimed.weatherResponse.list.indices){
            if(weatherTimed.weatherResponse.list[i].dtTxt.isNotEmpty()){
                val weatherList = weatherTimed.weatherResponse.list[i]
                val dateTime = LocalDateTime.parse(
                    weatherList.dtTxt,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime()

                if(dateTime.isAfter(localTimeInZone) && dateTime.isBefore(localTimeInZone.plusDays(1))){
                    hourlyForecastList.add(weatherList)
                }
                else if(dateTime.isAfter(localTimeInZone.plusDays(1)) && dateTime.isBefore(localTimeInZone.plusDays(6))){
                    val dayOfWeek = dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    if(!_fiveDayForecastMap.containsKey(dayOfWeek)){
                        _fiveDayForecastMap[dayOfWeek] = mutableListOf()
                    }
                    _fiveDayForecastMap[dayOfWeek]!!.add(weatherList)
                }
            }
        }

        val forecastItem = calculateAverages(hourlyForecastList)
        fiveDayForecastMap["Today"] = forecastItem

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

    private fun getZoneId(timezoneOffsetSeconds: Long): ZoneId {
        val offset = ZoneOffset.ofTotalSeconds(timezoneOffsetSeconds.toInt())
        return ZoneId.ofOffset("UTC", offset)
    }

    private fun getLocalTimeFromZoneOffset(timeZoneOffsetSeconds : Long): LocalDateTime{
        val offset = ZoneOffset.ofTotalSeconds(timeZoneOffsetSeconds.toInt())
        return ZonedDateTime.now(offset).toLocalDateTime()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            hourlyForecastRecycler.adapter = null
            dailyForecastRecycler.adapter = null
        }
    }
}