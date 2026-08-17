package com.example.agriproject.presentation.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("onecall")
    suspend fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely,hourly",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherResponse
}
