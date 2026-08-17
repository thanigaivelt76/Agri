package com.example.agriproject.presentation.weather

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val current: CurrentWeather,
    val daily: List<DailyWeather>,
    val alerts: List<WeatherAlert>? = null
)

@Serializable
data class CurrentWeather(
    val temp: Double,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<WeatherInfo>,
    val uvi: Double? = null
)

@Serializable
data class DailyWeather(
    val dt: Long,
    val temp: TempInfo,
    val weather: List<WeatherInfo>,
    val pop: Double // Probability of precipitation
)

@Serializable
data class TempInfo(
    val day: Double,
    val min: Double,
    val max: Double
)

@Serializable
data class WeatherInfo(
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class WeatherAlert(
    val event: String,
    val description: String,
    val start: Long,
    val end: Long
)
