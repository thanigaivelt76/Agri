package com.example.agriproject.presentation.marketplace

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.Crop
import com.example.agriproject.data.repository.CropRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class SellCropState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class SellCropViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val cropRepository = CropRepository()

    private val _state = MutableStateFlow(SellCropState())
    val state = _state.asStateFlow()

    fun uploadCrop(
        cropName: String,
        category: String,
        quantity: String,
        unit: String,
        price: String,
        harvestDate: String,
        location: String,
        description: String,
        quality: String,
        isOrganic: Boolean,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _state.value = SellCropState(isLoading = true)
            try {
                var imageUrl: String? = null
                
                // 1. Upload Image to Firebase Storage
                imageUri?.let { uri ->
                    val fileName = "crop_images/${UUID.randomUUID()}.jpg"
                    val ref = storage.reference.child(fileName)
                    ref.putFile(uri).await()
                    imageUrl = ref.downloadUrl.await().toString()
                }

                // 2. Prepare Crop Object
                val crop = Crop(
                    name = cropName,
                    category = category,
                    quantity = quantity,
                    unit = unit,
                    price = price,
                    harvestDate = harvestDate,
                    location = location,
                    description = description,
                    quality = quality,
                    isOrganic = isOrganic,
                    imageUrl = imageUrl,
                    farmerId = auth.currentUser?.uid ?: "",
                    farmerName = auth.currentUser?.displayName ?: "Farmer"
                )

                // 3. Save using Repository
                cropRepository.addCrop(crop).onSuccess {
                    _state.value = SellCropState(isSuccess = true)
                }.onFailure { e ->
                    _state.value = SellCropState(error = e.localizedMessage)
                }
            } catch (e: Exception) {
                _state.value = SellCropState(error = e.localizedMessage)
            }
        }
    }
    
    fun resetState() {
        _state.value = SellCropState()
    }
}
