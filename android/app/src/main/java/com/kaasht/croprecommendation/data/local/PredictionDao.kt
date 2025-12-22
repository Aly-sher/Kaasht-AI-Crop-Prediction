package com.kaasht.croprecommendation.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaasht.croprecommendation.data.model.PredictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<PredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity)

    @Delete
    suspend fun deletePrediction(prediction: PredictionEntity)

    @Query("DELETE FROM predictions")
    suspend fun clearAll()
}
