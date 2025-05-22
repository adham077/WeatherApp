package com.example.weatherapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.key.apiKey
import com.example.weatherapp.model.data.source.local.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.WeatherRemoteDataSource
import com.example.weatherapp.model.pojo.WeatherResponseEntity
import com.example.weatherapp.model.pojo.WeatherTimed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        val drawerLayout: DrawerLayout = binding.drawerLayout

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        val weatherRemoteDataSource = WeatherRemoteDataSource(
            this
        )
        val weatherLocalDataSource = WeatherLocalDataSource(
            this
        )
        GlobalScope.launch(Dispatchers.IO) {
            val response = weatherRemoteDataSource.getWeather(
                30.010543,
                31.425506,
                appId = apiKey
            )

            Log.i("localDateTime", LocalDateTime.now().toString())

            val timedWeather = WeatherTimed(response.weatherResponse!!, LocalDateTime.now())
            weatherLocalDataSource.insertWeather(
                WeatherResponseEntity(
                    response = timedWeather
                )
            )
            /*val weather = weatherLocalDataSource.deleteAllWeather()*/
        }


        /*navigation between fragments*/

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}