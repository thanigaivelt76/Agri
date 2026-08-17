package com.example.agriproject.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

data class WeatherState(
    val isLoading: Boolean = false,
    val weather: WeatherResponse? = null,
    val error: String? = null
)

class WeatherViewModel : ViewModel() {
    private val _state = MutableStateFlow(WeatherState())
    val state = _state.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .client(client)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    private val api = retrofit.create(WeatherApiService::class.java)

    init {
        // Coimbatore coordinates as default
        fetchWeather(11.0168, 76.9558)
    }

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Replace with your real API key
                val apiKey = "YOUR_OPENWEATHER_API_KEY"
                if (apiKey == "YOUR_OPENWEATHER_API_KEY") {
                    throw Exception("Please provide a valid OpenWeather API key")
                }
                val response = api.getWeatherData(lat, lon, apiKey = apiKey)
                _state.value = _state.value.copy(isLoading = false, weather = response)
            } catch (e: Exception) {
                // Load mock data on failure or missing key
                _state.value = _state.value.copy(
                    isLoading = false,
                    weather = getMockWeather(),
                    error = if (e.message?.contains("API key") == true) e.message else "Loaded offline data"
                )
            }
        }
    }

    private fun getMockWeather(): WeatherResponse {
        val current = CurrentWeather(
            temp = 28.5,
            humidity = 65,
            wind_speed = 12.4,
            weather = listOf(WeatherInfo("Clouds", "broken clouds", "04d")),
            uvi = 8.0
        )
        val daily = (0..6).map { i ->
            DailyWeather(
                dt = System.currentTimeMillis() / 1000 + (i * 86400),
                temp = TempInfo(day = 27.0 + i, min = 22.0, max = 31.0),
                weather = listOf(WeatherInfo("Rain", "light rain", "10d")),
                pop = 0.45
            )
        }
        val alerts = listOf(
            WeatherAlert("Heat Wave", "Extremely high temperatures expected between 12 PM to 4 PM.", 1690000000, 1690020000)
        )
        return WeatherResponse(current, daily, alerts)
    }
}
