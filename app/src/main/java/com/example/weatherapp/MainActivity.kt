package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.weatherapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

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
            setOf(
                R.id.homeFragment,
                R.id.favoriteFragment,
                R.id.settingsFragment,
                R.id.alertsFragment,
                R.id.addFavoriteFragment
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.favoriteFragment -> {
                    navController.navigate(R.id.favoriteFragment, null, NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, false)
                        .build())
                    true
                }
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment, null, NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, false)
                        .build())
                    true
                }
                R.id.settingsFragment -> {
                    navController.navigate(R.id.settingsFragment, null, NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, false)
                        .build())
                    true
                }
                R.id.addFavoriteFragment -> {
                    navController.navigate(R.id.addFavoriteFragment, null, NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, false)
                        .build())
                    true
                }
                R.id.alertsFragment -> {
                    navController.navigate(R.id.alertsFragment, null, NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, false)
                        .build())
                    true
                }
                else -> false
            }.also {
                if (it) drawerLayout.closeDrawers()
            }
        }

        if (intent != null) {
            val senderID = intent.getStringExtra("SENDER_ID")
            val itemId = intent.getIntExtra("ITEM_ID", 0)
            Log.i("MAIN_Activity","ItemID: ${itemId}")
            if (senderID != null && senderID == "ALERTS_RECEIVER") {
                val sharedPreferences = getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply{
                    putBoolean("FromAlarm",true)
                    putInt("ITEM_ID",itemId)
                    apply()
                }
            }
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}