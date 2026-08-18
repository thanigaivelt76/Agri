package com.example.agriproject.presentation.machinery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.Machinery
import com.example.agriproject.data.model.User
import com.example.agriproject.data.repository.MachineryRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MachineryMapState(
    val nearbyMachinery: List<Machinery> = emptyList(),
    val userRole: String = "Farmer",
    val isLoading: Boolean = false,
    val error: String? = null,
    val userLocation: Pair<Double, Double>? = null,
    val radius: Float = 5f,
    val selectedCategory: String = "All"
)

class MachineryMapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MachineryRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _state = MutableStateFlow(MachineryMapState())
    val state = _state.asStateFlow()

    init {
        fetchUserRole()
        fetchLocationAndMachinery()
    }

    private fun fetchUserRole() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java)
                _state.update { it.copy(userRole = user?.role ?: "Farmer") }
            } catch (e: Exception) {
                // Default to Farmer
            }
        }
    }

    fun updateRadius(newRadius: Float) {
        _state.update { it.copy(radius = newRadius) }
        fetchLocationAndMachinery()
    }

    fun updateCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        fetchLocationAndMachinery()
    }

    fun fetchLocationAndMachinery() {
        _state.update { it.copy(isLoading = true) }
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { loc ->
                    val userLoc = loc.latitude to loc.longitude
                    _state.update { it.copy(userLocation = userLoc) }
                    loadMachinery(userLoc)
                } ?: run {
                    _state.update { it.copy(isLoading = false, error = "Location not available") }
                }
            }
        } catch (e: SecurityException) {
            _state.update { it.copy(isLoading = false, error = "Permission denied") }
        }
    }

    private fun loadMachinery(userLoc: Pair<Double, Double>) {
        viewModelScope.launch {
            repository.getMachineryList().collect { allMachinery ->
                val filtered = allMachinery.filter { machine ->
                    val distance = repository.calculateDistance(
                        userLoc.first, userLoc.second,
                        machine.latitude, machine.longitude
                    )
                    val matchesCategory = _state.value.selectedCategory == "All" || machine.type == _state.value.selectedCategory
                    distance <= _state.value.radius && matchesCategory
                }.sortedBy { machine ->
                    repository.calculateDistance(
                        userLoc.first, userLoc.second,
                        machine.latitude, machine.longitude
                    )
                }
                _state.update { it.copy(nearbyMachinery = filtered, isLoading = false) }
            }
        }
    }
}
