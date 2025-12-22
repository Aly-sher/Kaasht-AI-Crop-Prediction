package com.kaasht.croprecommendation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationHelper @Inject constructor(
    private val context: Context
) {
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    /**
     * Get current location using FusedLocationProviderClient
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val cancellationToken = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                Timber.d("Location obtained: $location")
                continuation.resume(location)
            }.addOnFailureListener { exception ->
                Timber.e(exception, "Failed to get location")
                continuation.resume(null)
            }
            
            continuation.invokeOnCancellation {
                cancellationToken.cancel()
            }
        } catch (e: Exception) {
            Timber.e(e, "Location error")
            continuation.resume(null)
        }
    }
    
    /**
     * Get last known location (faster but may be outdated)
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    Timber.d("Last known location: $location")
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to get last location")
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            Timber.e(e, "Location error")
            continuation.resume(null)
        }
    }
    
    /**
     * Calculate distance between two locations in kilometers
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000f // Convert to kilometers
    }
    
    fun getContext(): Context = context
}
