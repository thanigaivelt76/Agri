package com.example.agriproject.presentation.marketplace

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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

                // 2. Prepare Crop Data
                val cropData = hashMapOf(
                    "name" to cropName,
                    "category" to category,
                    "quantity" to quantity,
                    "unit" to unit,
                    "price" to price,
                    "harvestDate" to harvestDate,
                    "location" to location,
                    "description" to description,
                    "quality" to quality,
                    "isOrganic" to isOrganic,
                    "imageUrl" to imageUrl,
                    "farmerId" to auth.currentUser?.uid,
                    "farmerName" to (auth.currentUser?.displayName ?: "Farmer"),
                    "createdAt" to System.currentTimeMillis()
                )

                // 3. Save to Firestore
                firestore.collection("crops").add(cropData).await()

                _state.value = SellCropState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = SellCropState(error = e.localizedMessage)
            }
        }
    }
    
    fun resetState() {
        _state.value = SellCropState()
    }
}
