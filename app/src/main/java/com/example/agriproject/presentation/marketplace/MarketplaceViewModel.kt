package com.example.agriproject.presentation.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.Crop
import com.example.agriproject.data.repository.CropRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class MarketplaceState(
    val crops: List<Crop> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MarketplaceViewModel : ViewModel() {
    private val cropRepository = CropRepository()

    private val _state = MutableStateFlow(MarketplaceState())
    val state = _state.asStateFlow()

    init {
        loadCrops()
    }

    private fun loadCrops() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            cropRepository.getCrops()
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
                }
                .collect { crops ->
                    _state.value = _state.value.copy(crops = crops, isLoading = false)
                }
        }
    }
}
