package com.example.agriproject.presentation.machinery

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.Machinery
import com.example.agriproject.data.repository.MachineryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

data class AddMachineryState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val uploadProgress: Float = 0f
)

class AddMachineryViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val repository = MachineryRepository()

    private val _state = MutableStateFlow(AddMachineryState())
    val state = _state.asStateFlow()

    private val TAG = "AddMachineryViewModel"

    fun addMachinery(
        type: String,
        name: String,
        regNumber: String,
        price: String,
        unit: String,
        description: String,
        imageUri: Uri?,
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        val user = auth.currentUser
        if (user == null) {
            _state.value = _state.value.copy(error = "User not authenticated. Please login again.")
            return
        }

        if (imageUri == null) {
            _state.value = _state.value.copy(error = "Please select a machine photo.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, uploadProgress = 0f)
            try {
                val userId = user.uid
                val machineId = UUID.randomUUID().toString()
                
                // 1. Process and Compress Image
                val imageData = withContext(Dispatchers.IO) {
                    compressImage(imageUri)
                } ?: throw Exception("Unable to process image. Please choose another photo.")

                // 2. Upload to Firebase Storage: machinery/{userId}/{machineId}.jpg
                val storagePath = "machinery/$userId/$machineId.jpg"
                val ref = storage.reference.child(storagePath)
                
                Log.d(TAG, "Uploading image to: $storagePath")
                
                val uploadTask = ref.putBytes(imageData)
                
                // Optional: Track progress
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                    _state.value = _state.value.copy(uploadProgress = progress)
                }

                uploadTask.await()
                
                val imageUrl = ref.downloadUrl.await().toString()
                Log.d(TAG, "Upload success. URL: $imageUrl")

                // 3. Prepare Machinery Object
                val machinery = Machinery(
                    id = machineId,
                    ownerId = userId,
                    ownerName = user.displayName ?: "Owner",
                    type = type,
                    name = name,
                    registrationNumber = regNumber,
                    phoneNumber = user.phoneNumber ?: "",
                    imageUrl = imageUrl,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    price = price.toDoubleOrNull() ?: 0.0,
                    rentalUnit = unit,
                    description = description,
                    isAvailable = true,
                    createdAt = System.currentTimeMillis()
                )

                // 4. Save to Firestore
                Log.d(TAG, "Saving machinery to Firestore")
                repository.addMachinery(machinery).onSuccess {
                    Log.d(TAG, "Save success")
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }.onFailure { e ->
                    Log.e(TAG, "Firestore error", e)
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to save details: ${e.localizedMessage}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in addMachinery", e)
                _state.value = _state.value.copy(
                    isLoading = false, 
                    error = "Unable to upload image: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Resize if too large (Max 1200px side)
            val maxSize = 1200
            val width = originalBitmap.width
            val height = originalBitmap.height
            
            val finalBitmap = if (width > maxSize || height > maxSize) {
                val ratio = width.toFloat() / height.toFloat()
                val newWidth: Int
                val newHeight: Int
                if (ratio > 1) {
                    newWidth = maxSize
                    newHeight = (maxSize / ratio).toInt()
                } else {
                    newHeight = maxSize
                    newWidth = (maxSize * ratio).toInt()
                }
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Compression error", e)
            null
        }
    }

    fun resetState() {
        _state.value = AddMachineryState()
    }
}
