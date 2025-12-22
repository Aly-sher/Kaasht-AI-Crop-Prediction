package com.kaasht.croprecommendation.data.repository

import com.kaasht.croprecommendation.data.model.WeatherData
import com.kaasht.croprecommendation.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: ApiService
) {
    // NOTE: Replace with your actual OpenWeatherMap API Key
    private val apiKey = "YOUR_OPENWEATHER_API_KEY" 

    suspend fun getCurrentWeather(city: String): Result<WeatherData> {
        return try {
            // Check if we need to switch URLs for OpenWeather vs Python backend
            // Ideally, ApiService should handle both via @Url or separate Retrofit instances.
            // For simplicity, assuming ApiService points to OpenWeatherMap or has correct full URL.
            
            // NOTE: Since the NetworkModule points to localhost/Python, 
            // you need a separate Retrofit instance for OpenWeather OR 
            // use the @Url annotation in ApiService to override the base URL.
            // Here, we assume you update ApiService to handle the full URL for weather.
            
            val response = apiService.getWeather(city, apiKey)
            
            if (response.isSuccessful && response.body() != null) {
                val raw = response.body()!!
                // Map API response to our Domain Model
                val weatherData = WeatherData(
                    temperature = raw.main.temp,
                    humidity = raw.main.humidity,
                    rainfall = raw.rain?.oneHour ?: 0.0, // OpenWeather puts rain in 'rain' object
                    condition = raw.weather.firstOrNull()?.description ?: "Unknown",
                    lastUpdated = System.currentTimeMillis()
                )
                Result.success(weatherData)
            } else {
                Result.failure(Exception("Weather API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWeatherByLocation(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            val response = apiService.getWeatherByCoords(lat, lon, apiKey)
            if (response.isSuccessful && response.body() != null) {
                val raw = response.body()!!
                val weatherData = WeatherData(
                    temperature = raw.main.temp,
                    humidity = raw.main.humidity,
                    rainfall = raw.rain?.oneHour ?: 0.0,
                    condition = raw.weather.firstOrNull()?.description ?: "Unknown",
                    lastUpdated = System.currentTimeMillis()
                )
                Result.success(weatherData)
            } else {
                Result.failure(Exception("Weather API Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWeatherForecast(city: String): Result<List<WeatherData>> {
        // Mocking forecast for now as OpenWeather Free tier has limits on daily forecast
        return Result.success(emptyList()) 
    }
}
