package com.example.agriproject.presentation.dashboard

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.User
import com.example.agriproject.presentation.weather.WeatherResponse
import com.example.agriproject.presentation.weather.WeatherViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class DashboardState(
    val userName: String = "Farmer",
    val locationName: String = "Detecting location...",
    val weather: WeatherResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val weatherViewModel = WeatherViewModel() // Using existing weather logic

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            fetchUserName()
            fetchLocationAndWeather()
        }
    }

    private suspend fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)
            _state.value = _state.value.copy(userName = user?.name ?: auth.currentUser?.displayName ?: "Farmer")
        } catch (e: Exception) {
            _state.value = _state.value.copy(userName = auth.currentUser?.displayName ?: "Farmer")
        }
    }

    private fun fetchLocationAndWeather() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    updateLocationName(it.latitude, it.longitude)
                    weatherViewModel.fetchWeather(it.latitude, it.longitude)
                    
                    // Observe weather from WeatherViewModel
                    viewModelScope.launch {
                        weatherViewModel.state.collect { weatherState ->
                            _state.value = _state.value.copy(
                                weather = weatherState.weather,
                                isLoading = weatherState.isLoading,
                                error = weatherState.error
                            )
                        }
                    }
                } ?: run {
                    _state.value = _state.value.copy(locationName = "Location not found", isLoading = false)
                }
            }
        } catch (e: SecurityException) {
            _state.value = _state.value.copy(locationName = "Permission denied", isLoading = false)
        }
    }

    private fun updateLocationName(lat: Double, lon: Double) {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: "Unknown City"
                val state = address.adminArea ?: "Unknown State"
                _state.value = _state.value.copy(locationName = "$city, $state")
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(locationName = "Unknown Location")
        }
    }
}
