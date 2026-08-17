package com.example.agriproject.presentation.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.Crop
import com.example.agriproject.data.repository.CropRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CropDetailState(
    val crop: Crop? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CropDetailViewModel : ViewModel() {
    private val cropRepository = CropRepository()

    private val _state = MutableStateFlow(CropDetailState())
    val state = _state.asStateFlow()

    fun loadCrop(cropId: String) {
        viewModelScope.launch {
            _state.value = CropDetailState(isLoading = true)
            val crop = cropRepository.getCropById(cropId)
            if (crop != null) {
                _state.value = CropDetailState(crop = crop)
            } else {
                _state.value = CropDetailState(error = "Crop not found")
            }
        }
    }
}
