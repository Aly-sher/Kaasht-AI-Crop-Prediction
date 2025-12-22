package com.kaasht.croprecommendation.ui.weather

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaasht.croprecommendation.data.model.Resource
import com.kaasht.croprecommendation.data.model.WeatherData
import com.kaasht.croprecommendation.data.repository.WeatherRepository
import com.kaasht.croprecommendation.utils.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.util.Locale

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {
    
    private val _currentWeather = MutableLiveData<Resource<WeatherData>>()
    val currentWeather: LiveData<Resource<WeatherData>> = _currentWeather
    
    private val _weatherForecast = MutableLiveData<Resource<List<WeatherData>>>()
    val weatherForecast: LiveData<Resource<List<WeatherData>>> = _weatherForecast
    
    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> = _locationName
    
    private val _lastUpdated = MutableLiveData<Long>()
    val lastUpdated: LiveData<Long> = _lastUpdated
    
    /**
     * Fetch weather for current GPS location
     */
    fun fetchCurrentLocationWeather() {
        viewModelScope.launch {
            try {
                _currentWeather.value = Resource.Loading()
                
                val location = locationHelper.getCurrentLocation()
                location?.let {
                    val result = weatherRepository.getCurrentWeatherByLocation(
                        it.latitude,
                        it.longitude
                    )
                    
                    result.fold(
                        onSuccess = { weather ->
                            _currentWeather.value = Resource.Success(weather)
                            _lastUpdated.value = System.currentTimeMillis()
                            _locationName.value = getCityName(it)
                            Timber.d("Weather fetched for current location")
                        },
                        onFailure = { error ->
                            _currentWeather.value = Resource.Error(
                                error.message ?: "Failed to fetch weather"
                            )
                        }
                    )
                } ?: run {
                    _currentWeather.value = Resource.Error("Location not available")
                }
            } catch (e: Exception) {
                _currentWeather.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Fetch weather for specific district
     */
    fun fetchWeatherForDistrict(district: String) {
        viewModelScope.launch {
            try {
                _currentWeather.value = Resource.Loading()
                
                val result = weatherRepository.getCurrentWeather(district)
                
                result.fold(
                    onSuccess = { weather ->
                        _currentWeather.value = Resource.Success(weather)
                        _lastUpdated.value = System.currentTimeMillis()
                        _locationName.value = district.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        Timber.d("Weather fetched for $district")
                    },
                    onFailure = { error ->
                        _currentWeather.value = Resource.Error(
                            error.message ?: "Failed to fetch weather"
                        )
                    }
                )
            } catch (e: Exception) {
                _currentWeather.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Fetch 7-day weather forecast
     */
    fun fetchWeatherForecast() {
        viewModelScope.launch {
            try {
                _weatherForecast.value = Resource.Loading()
                
                val result = weatherRepository.getWeatherForecast(
                    locationName.value ?: "lahore"
                )
                
                result.fold(
                    onSuccess = { forecast ->
                        _weatherForecast.value = Resource.Success(forecast)
                        Timber.d("Forecast fetched: ${forecast.size} days")
                    },
                    onFailure = { error ->
                        _weatherForecast.value = Resource.Error(
                            error.message ?: "Failed to fetch forecast"
                        )
                    }
                )
            } catch (e: Exception) {
                _weatherForecast.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun getCityName(location: Location): String {
        // Use Geocoder to get city name from coordinates
        return try {
            val geocoder = android.location.Geocoder(
                locationHelper.getContext(),
                Locale.getDefault()
            )
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            addresses?.firstOrNull()?.locality ?: "Unknown Location"
        } catch (e: Exception) {
            "Unknown Location"
        }
    }
}
