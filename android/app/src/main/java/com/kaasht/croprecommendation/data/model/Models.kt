package com.kaasht.croprecommendation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// ==================== API REQUEST/RESPONSE ====================

data class CropPredictionRequest(
    @SerializedName("N") val nitrogen: Double,
    @SerializedName("P") val phosphorus: Double,
    @SerializedName("K") val potassium: Double,
    val temperature: Double,
    val humidity: Double,
    val ph: Double,
    val rainfall: Double,
    val district: String
)

data class CropRecommendation(
    val crop: String,
    val confidence: Double,
    val rank: Int
)

data class CropPredictionResponse(
    val success: Boolean,
    val recommendations: List<CropRecommendation>,
    @SerializedName("input_summary") val inputSummary: InputSummary
)

data class InputSummary(
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double,
    val temperature: Double,
    val humidity: Double,
    val ph: Double,
    val rainfall: Double,
    val district: String
)

// ==================== WEATHER DATA ====================

data class WeatherResponse(
    val main: WeatherMain,
    val weather: List<WeatherCondition>,
    val wind: Wind,
    val rain: Rain?,
    val name: String
)

data class WeatherMain(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class WeatherCondition(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)

data class Rain(
    @SerializedName("1h") val oneHour: Double?
)

// ==================== LOCAL DATABASE ENTITIES ====================

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double,
    val temperature: Double,
    val humidity: Double,
    val ph: Double,
    val rainfall: Double,
    val district: String,
    val predictedCrop: String,
    val confidence: Double,
    val location: String?
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val district: String,
    val temperature: Double,
    val humidity: Int,
    val rainfall: Double,
    val windSpeed: Double,
    val description: String,
    val timestamp: Long
)

@Entity(tableName = "soil_readings")
data class SoilReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double,
    val ph: Double,
    val deviceId: String?,
    val location: String?
)

// ==================== USER DATA ====================

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val userId: String,
    val email: String,
    val name: String,
    val contact: String?,
    val district: String,
    val location: String?,
    val createdAt: Long
)

// ==================== UI MODELS ====================

data class CropDetailInfo(
    val name: String,
    val confidence: Double,
    val rank: Int,
    val estimatedYield: String? = null,
    val waterRequirement: String? = null,
    val growthDuration: String? = null,
    val description: String? = null
)

data class DashboardData(
    val currentWeather: WeatherData,
    val soilHealth: SoilHealth,
    val recentPredictions: List<PredictionSummary>,
    val alerts: List<String>
)

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val rainfall: Double,
    val condition: String,
    val lastUpdated: Long
)

data class SoilHealth(
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double,
    val ph: Double,
    val status: String // "Good", "Moderate", "Poor"
)

data class PredictionSummary(
    val id: Long,
    val date: Long,
    val crop: String,
    val confidence: Double,
    val district: String
)

// ==================== SEALED CLASSES FOR STATE MANAGEMENT ====================

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

// ==================== CONSTANTS ====================

object CropInfo {
    val CROP_DETAILS = mapOf(
        "rice" to CropDetail(
            name = "Rice",
            estimatedYield = "3-4 tons/hectare",
            waterRequirement = "1200-1500 mm",
            growthDuration = "120-150 days",
            optimalTemp = "20-35°C",
            description = "Best suited for areas with high rainfall and warm temperatures"
        ),
        "wheat" to CropDetail(
            name = "Wheat",
            estimatedYield = "2.5-3.5 tons/hectare",
            waterRequirement = "400-500 mm",
            growthDuration = "110-130 days",
            optimalTemp = "15-25°C",
            description = "Ideal for cooler seasons with moderate water availability"
        ),
        "maize" to CropDetail(
            name = "Maize",
            estimatedYield = "4-6 tons/hectare",
            waterRequirement = "500-800 mm",
            growthDuration = "90-120 days",
            optimalTemp = "18-32°C",
            description = "Versatile crop suitable for both rainy and irrigated conditions"
        ),
        "cotton" to CropDetail(
            name = "Cotton",
            estimatedYield = "1.5-2.5 tons/hectare",
            waterRequirement = "700-1300 mm",
            growthDuration = "180-210 days",
            optimalTemp = "21-35°C",
            description = "Requires warm weather and moderate to high rainfall"
        ),
        "sugarcane" to CropDetail(
            name = "Sugarcane",
            estimatedYield = "60-80 tons/hectare",
            waterRequirement = "1500-2500 mm",
            growthDuration = "12-18 months",
            optimalTemp = "26-32°C",
            description = "High water-consuming crop, best for tropical regions"
        )
    )
}

data class CropDetail(
    val name: String,
    val estimatedYield: String,
    val waterRequirement: String,
    val growthDuration: String,
    val optimalTemp: String,
    val description: String
)
