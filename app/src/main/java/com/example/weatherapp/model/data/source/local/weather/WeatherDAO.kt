package com.example.weatherapp.model.data.source.local.weather

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherapp.model.pojo.WeatherResponseEntity

@Dao
interface WeatherDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherResponseEntity) : Long

    @Update
    suspend fun updateWeather(weather: WeatherResponseEntity) : Int

    @Delete
    suspend fun deleteWeather(weather: WeatherResponseEntity) : Int

    @Query("SELECT * FROM weather_table WHERE id = :id")
    suspend fun getWeatherById(id: Int): WeatherResponseEntity?

    @Query("SELECT * FROM weather_table")
    suspend fun getAllWeather(): List<WeatherResponseEntity>?

    @Query("DELETE FROM weather_table")
    suspend fun deleteAllWeather() : Int

}