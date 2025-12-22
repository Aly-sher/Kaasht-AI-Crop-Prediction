package com.kaasht.croprecommendation.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaasht.croprecommendation.data.model.PredictionEntity
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    
    /**
     * Sync prediction to Firestore
     */
    suspend fun syncPrediction(prediction: PredictionEntity): Result<Unit> {
        return try {
            val userId = authRepository.currentUser?.uid 
                ?: throw Exception("User not authenticated")
            
            firestore.collection("users")
                .document(userId)
                .collection("predictions")
                .add(prediction)
                .await()
            
            Timber.d("Prediction synced to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync prediction")
            Result.failure(e)
        }
    }
    
    /**
     * Get all predictions from Firestore
     */
    suspend fun getAllPredictions(): Result<List<PredictionEntity>> {
        return try {
            val userId = authRepository.currentUser?.uid 
                ?: throw Exception("User not authenticated")
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("predictions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val predictions = snapshot.documents.mapNotNull { 
                it.toObject(PredictionEntity::class.java) 
            }
            
            Timber.d("Retrieved ${predictions.size} predictions from Firestore")
            Result.success(predictions)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get predictions")
            Result.failure(e)
        }
    }
    
    /**
     * Delete prediction from Firestore
     */
    suspend fun deletePrediction(predictionId: String): Result<Unit> {
        return try {
            val userId = authRepository.currentUser?.uid 
                ?: throw Exception("User not authenticated")
            
            firestore.collection("users")
                .document(userId)
                .collection("predictions")
                .document(predictionId)
                .delete()
                .await()
            
            Timber.d("Prediction deleted from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete prediction")
            Result.failure(e)
        }
    }
}
