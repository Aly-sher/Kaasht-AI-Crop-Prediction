package com.kaasht.croprecommendation.data.remote

import com.kaasht.croprecommendation.data.model.CropPredictionRequest
import com.kaasht.croprecommendation.data.model.CropPredictionResponse
import com.kaasht.croprecommendation.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/predict")
    suspend fun predictCrop(@Body request: CropPredictionRequest): Response<CropPredictionResponse>

    @GET("weather")
    suspend fun getWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @GET("weather")
    suspend fun getWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
    
    @GET("forecast/daily")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("cnt") days: Int = 7,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
}
