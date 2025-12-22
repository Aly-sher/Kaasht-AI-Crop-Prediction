package com.kaasht.croprecommendation.data.repository

import com.kaasht.croprecommendation.data.local.PredictionDao
import com.kaasht.croprecommendation.data.model.CropPredictionRequest
import com.kaasht.croprecommendation.data.model.CropPredictionResponse
import com.kaasht.croprecommendation.data.model.PredictionEntity
import com.kaasht.croprecommendation.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropRepository @Inject constructor(
    private val apiService: ApiService,
    private val predictionDao: PredictionDao,
    private val firestoreSyncRepository: FirestoreSyncRepository
) {

    // --- API Calls ---
    
    suspend fun predictCrop(request: CropPredictionRequest): Result<CropPredictionResponse> {
        return try {
            val response = apiService.predictCrop(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Local Database Operations ---

    fun getPredictionHistory(): Flow<List<PredictionEntity>> = predictionDao.getAllPredictions()

    suspend fun savePrediction(prediction: PredictionEntity) {
        predictionDao.insertPrediction(prediction)
        // Auto-sync to cloud if possible
        firestoreSyncRepository.syncPrediction(prediction)
    }

    suspend fun deletePrediction(prediction: PredictionEntity) {
        predictionDao.deletePrediction(prediction)
    }

    suspend fun clearHistory() {
        predictionDao.clearAll()
    }
}
