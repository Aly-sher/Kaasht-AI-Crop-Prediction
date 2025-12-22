package com.kaasht.croprecommendation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaasht.croprecommendation.data.model.PredictionEntity
import com.kaasht.croprecommendation.data.model.SoilReadingEntity
import com.kaasht.croprecommendation.data.model.WeatherCacheEntity

@Database(
    entities = [PredictionEntity::class, WeatherCacheEntity::class, SoilReadingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao
}
