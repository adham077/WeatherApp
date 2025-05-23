package com.example.weatherapp.model.data.source.local.weather

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.model.pojo.WeatherResponseEntity

@Database(entities = [WeatherResponseEntity::class], version = 1)
@TypeConverters(WeatherTypeConverters::class)
abstract class WeatherDataBase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDAO

    companion object{
        @Volatile
        private var instance: WeatherDataBase? = null

        fun getInstance(context: Context): WeatherDataBase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDataBase::class.java,
                    "weather_database"
                ).build()
                instance = newInstance
                newInstance
            }
        }
    }


}