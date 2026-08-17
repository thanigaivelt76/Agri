package com.example.agriproject.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminStats(
    val totalRevenue: Double = 1254300.0,
    val totalUsers: Int = 1240,
    val activeBookings: Int = 85,
    val pendingComplaints: Int = 12
)

data class AdminState(
    val stats: AdminStats = AdminStats(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "Dashboard"
)

class AdminViewModel : ViewModel() {
    private val _state = MutableStateFlow(AdminState())
    val state = _state.asStateFlow()

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun selectCategory(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun exportReport(type: String) {
        // Mock export logic
    }
}
