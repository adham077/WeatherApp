package com.example.weatherapp.model.data.source.local.weather
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherapp.model.pojo.WeatherAlertEntity

@Dao
interface WeatherAlertsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherAlertEntity) : Long

    @Update
    suspend fun updateWeather(weather: WeatherAlertEntity) : Int

    @Delete
    suspend fun deleteWeather(weather: WeatherAlertEntity) : Int

    @Query("SELECT * FROM weather_alerts_table WHERE id = :id")
    suspend fun getWeatherById(id: Int): WeatherAlertEntity?

    @Query("SELECT * FROM weather_alerts_table")
    suspend fun getAllWeather(): List<WeatherAlertEntity>?

    @Query("DELETE FROM weather_alerts_table")
    suspend fun deleteAllWeather() : Int

    @Query("DELETE FROM weather_alerts_table WHERE id = :id")
    suspend fun deleteWeatherById(id: Int): Int
}