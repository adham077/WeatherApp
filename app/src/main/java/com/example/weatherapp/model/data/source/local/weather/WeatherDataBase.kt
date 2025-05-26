package com.example.weatherapp.model.data.source.local.weather

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.pojo.WeatherResponseEntity

@Database(entities = [WeatherResponseEntity::class, WeatherAlertEntity::class], version = 2)
@TypeConverters(WeatherTypeConverters::class)
abstract class WeatherDataBase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDAO
    abstract fun weatherAlertsDao(): WeatherAlertsDao

    companion object{
        @Volatile
        private var instance: WeatherDataBase? = null

        fun getInstance(context: Context): WeatherDataBase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                                            context.applicationContext,
                                            WeatherDataBase::class.java,
                                            "weather_database"
                                        ).fallbackToDestructiveMigration(false).build()
                instance = newInstance
                instance?.openHelper?.writableDatabase
                newInstance
            }
        }
    }


}