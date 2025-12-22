package com.kaasht.croprecommendation.ui.prediction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaasht.croprecommendation.data.model.*
import com.kaasht.croprecommendation.data.repository.CropRepository
import com.kaasht.croprecommendation.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val cropRepository: CropRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    // UI State
    private val _predictionState = MutableLiveData<Resource<CropPredictionResponse>>()
    val predictionState: LiveData<Resource<CropPredictionResponse>> = _predictionState

    private val _weatherData = MutableLiveData<Resource<WeatherData>>()
    val weatherData: LiveData<Resource<WeatherData>> = _weatherData

    private val _soilData = MutableLiveData<SoilHealth>()
    val soilData: LiveData<SoilHealth> = _soilData

    // Input fields
    val nitrogen = MutableLiveData<String>()
    val phosphorus = MutableLiveData<String>()
    val potassium = MutableLiveData<String>()
    val ph = MutableLiveData<String>()
    val district = MutableLiveData<String>()

    // Auto-fetched fields
    val temperature = MutableLiveData<Double>()
    val humidity = MutableLiveData<Double>()
    val rainfall = MutableLiveData<Double>()

    // Validation
    private val _inputValidation = MutableLiveData<ValidationResult>()
    val inputValidation: LiveData<ValidationResult> = _inputValidation

    /**
     * Fetch weather data for the selected district
     */
    fun fetchWeatherData(districtName: String) {
        viewModelScope.launch {
            _weatherData.value = Resource.Loading()
            
            try {
                val result = weatherRepository.getCurrentWeather(districtName)
                
                result.fold(
                    onSuccess = { weather ->
                        temperature.value = weather.temperature
                        humidity.value = weather.humidity.toDouble()
                        rainfall.value = weather.rainfall
                        
                        _weatherData.value = Resource.Success(weather)
                        Timber.d("Weather fetched: $weather")
                    },
                    onFailure = { error ->
                        _weatherData.value = Resource.Error(error.message ?: "Weather fetch failed")
                        Timber.e(error, "Weather fetch error")
                    }
                )
            } catch (e: Exception) {
                _weatherData.value = Resource.Error(e.message ?: "Unknown error")
                Timber.e(e, "Weather fetch exception")
            }
        }
    }

    /**
     * Update soil data from sensor or manual input
     */
    fun updateSoilData(n: Double, p: Double, k: Double, phValue: Double) {
        nitrogen.value = n.toString()
        phosphorus.value = p.toString()
        potassium.value = k.toString()
        ph.value = phValue.toString()

        // Determine soil health status
        val status = when {
            n >= 50 && p >= 25 && k >= 25 && phValue in 6.0..7.5 -> "Good"
            n >= 30 && p >= 15 && k >= 15 && phValue in 5.5..8.0 -> "Moderate"
            else -> "Poor"
        }

        _soilData.value = SoilHealth(
            nitrogen = n,
            phosphorus = p,
            potassium = k,
            ph = phValue,
            status = status
        )
    }

    /**
     * Validate all input fields before prediction
     */
    fun validateInputs(): Boolean {
        val n = nitrogen.value?.toDoubleOrNull()
        val p = phosphorus.value?.toDoubleOrNull()
        val k = potassium.value?.toDoubleOrNull()
        val phVal = ph.value?.toDoubleOrNull()
        val dist = district.value
        val temp = temperature.value
        val hum = humidity.value
        val rain = rainfall.value

        val errors = mutableListOf<String>()

        if (n == null || n < 0) errors.add("Invalid Nitrogen value")
        if (p == null || p < 0) errors.add("Invalid Phosphorus value")
        if (k == null || k < 0) errors.add("Invalid Potassium value")
        if (phVal == null || phVal !in 0.0..14.0) errors.add("Invalid pH value (0-14)")
        if (dist.isNullOrBlank()) errors.add("District not selected")
        if (temp == null) errors.add("Weather data not loaded")
        if (hum == null) errors.add("Humidity data not loaded")
        if (rain == null) errors.add("Rainfall data not loaded")

        _inputValidation.value = if (errors.isEmpty()) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(isValid = false, errors = errors)
        }

        return errors.isEmpty()
    }

    /**
     * Predict suitable crops using ML model
     */
    fun predictCrops() {
        if (!validateInputs()) {
            Timber.w("Input validation failed")
            return
        }

        viewModelScope.launch {
            _predictionState.value = Resource.Loading()

            try {
                val request = CropPredictionRequest(
                    nitrogen = nitrogen.value!!.toDouble(),
                    phosphorus = phosphorus.value!!.toDouble(),
                    potassium = potassium.value!!.toDouble(),
                    temperature = temperature.value!!,
                    humidity = humidity.value!!,
                    ph = ph.value!!.toDouble(),
                    rainfall = rainfall.value!!,
                    district = district.value!!.lowercase()
                )

                Timber.d("Sending prediction request: $request")

                val result = cropRepository.predictCrop(request)

                result.fold(
                    onSuccess = { response ->
                        // Save prediction to local database
                        savePredictionHistory(request, response)
                        
                        _predictionState.value = Resource.Success(response)
                        Timber.d("Prediction successful: ${response.recommendations.size} crops")
                    },
                    onFailure = { error ->
                        _predictionState.value = Resource.Error(
                            error.message ?: "Prediction failed"
                        )
                        Timber.e(error, "Prediction error")
                    }
                )
            } catch (e: Exception) {
                _predictionState.value = Resource.Error(e.message ?: "Unknown error")
                Timber.e(e, "Prediction exception")
            }
        }
    }

    /**
     * Save prediction to local database for history
     */
    private suspend fun savePredictionHistory(
        request: CropPredictionRequest,
        response: CropPredictionResponse
    ) {
        try {
            val topCrop = response.recommendations.firstOrNull() ?: return
            
            val prediction = PredictionEntity(
                timestamp = System.currentTimeMillis(),
                nitrogen = request.nitrogen,
                phosphorus = request.phosphorus,
                potassium = request.potassium,
                temperature = request.temperature,
                humidity = request.humidity,
                ph = request.ph,
                rainfall = request.rainfall,
                district = request.district,
                predictedCrop = topCrop.crop,
                confidence = topCrop.confidence,
                location = null // Add GPS location if needed
            )

            cropRepository.savePrediction(prediction)
            Timber.d("Prediction saved to history")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save prediction history")
        }
    }

    /**
     * Reset all input fields
     */
    fun resetInputs() {
        nitrogen.value = ""
        phosphorus.value = ""
        potassium.value = ""
        ph.value = ""
        district.value = ""
        temperature.value = null
        humidity.value = null
        rainfall.value = null
        
        _predictionState.value = Resource.Loading()
        _weatherData.value = Resource.Loading()
        _soilData.value = null
    }

    /**
     * Get crop details for display
     */
    fun getCropDetails(cropName: String): CropDetailInfo? {
        val cropDetail = CropInfo.CROP_DETAILS[cropName.lowercase()] ?: return null
        
        return CropDetailInfo(
            name = cropDetail.name,
            confidence = 0.0, // Will be filled from prediction
            rank = 0,
            estimatedYield = cropDetail.estimatedYield,
            waterRequirement = cropDetail.waterRequirement,
            growthDuration = cropDetail.growthDuration,
            description = cropDetail.description
        )
    }
}

/**
 * Validation result data class
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)
