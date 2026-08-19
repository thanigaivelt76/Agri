package com.example.agriproject.presentation.machinery

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.Machinery
import com.example.agriproject.data.repository.MachineryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
    private val contentResolver = application.contentResolver

    private val _state = MutableStateFlow(AddMachineryState())
    val state = _state.asStateFlow()

    private val TAG = "MachineryUpload"

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
            _state.value = _state.value.copy(error = "Please login before adding machinery.")
            return
        }

        if (imageUri == null) {
            _state.value = _state.value.copy(error = "Please select a machine photo.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, uploadProgress = 0f)
            try {
                // 1. Validate Uri and open stream
                val inputStream = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(imageUri)
                }
                if (inputStream == null) {
                    throw Exception("Unable to open image file. Please try selecting it again.")
                }
                inputStream.close()

                val userId = user.uid
                val machineId = UUID.randomUUID().toString()
                
                // 2. Detect MIME type and extension
                val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

                // 3. Process and Compress Image if needed
                // We'll compress to a temporary file to use putFile as requested
                val tempFile = withContext(Dispatchers.IO) {
                    compressImageToTempFile(imageUri, machineId, extension)
                } ?: throw Exception("Image processing failed.")

                // 4. Create unique Storage path: machinery_images/{userId}/{machineId}.jpg
                val storagePath = "machinery_images/$userId/$machineId.$extension"
                val storageRef = storage.reference.child(storagePath)
                
                val metadata = StorageMetadata.Builder()
                    .setContentType(mimeType)
                    .build()

                Log.d(TAG, "Starting upload to: $storagePath")
                
                // 5. Upload using putFile
                val uploadTask = storageRef.putFile(Uri.fromFile(tempFile), metadata)
                
                uploadTask.addOnProgressListener { taskSnapshot ->
                    if (taskSnapshot.totalByteCount > 0) {
                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                        _state.value = _state.value.copy(uploadProgress = progress)
                    }
                }

                try {
                    uploadTask.await()
                    Log.d(TAG, "Upload completed successfully")
                } catch (e: Exception) {
                    if (e is StorageException) {
                        Log.e(TAG, "Firebase Storage Error: Code ${e.errorCode}, Message: ${e.message}", e)
                    } else {
                        Log.e(TAG, "Upload task failed", e)
                    }
                    throw e
                }
                
                // 6. Get download URL ONLY after success
                val downloadUrl = storageRef.downloadUrl.await().toString()
                Log.d(TAG, "Download URL retrieved: $downloadUrl")

                // 7. Prepare Machinery Object
                val machinery = Machinery(
                    id = machineId,
                    ownerId = userId,
                    ownerName = user.displayName ?: "Owner",
                    type = type,
                    name = name,
                    registrationNumber = regNumber,
                    phoneNumber = user.phoneNumber ?: "",
                    imageUrl = downloadUrl,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    price = price.toDoubleOrNull() ?: 0.0,
                    rentalUnit = unit,
                    description = description,
                    isAvailable = true,
                    createdAt = System.currentTimeMillis()
                )

                // 8. Save to Firestore
                Log.d(TAG, "Saving machinery metadata to Firestore")
                repository.addMachinery(machinery).onSuccess {
                    Log.d(TAG, "Firestore save success")
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                    tempFile.delete() // Clean up
                }.onFailure { e ->
                    Log.e(TAG, "Firestore save error", e)
                    _state.value = _state.value.copy(isLoading = false, error = "Unable to save machinery details. Please try again.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Add machinery failed", e)
                val userFriendlyError = when {
                    e is StorageException && e.errorCode == StorageException.ERROR_NOT_AUTHORIZED -> 
                        "Permission denied. Please check your storage rules."
                    else -> "Unable to upload image. Please try again."
                }
                _state.value = _state.value.copy(isLoading = false, error = userFriendlyError)
            }
        }
    }

    private suspend fun compressImageToTempFile(uri: Uri, machineId: String, extension: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) return@withContext null

                // Resize if too large (Max 1280px side)
                val maxSize = 1280
                var width = originalBitmap.width
                var height = originalBitmap.height
                
                if (width > maxSize || height > maxSize) {
                    val ratio = width.toFloat() / height.toFloat()
                    if (ratio > 1) {
                        width = maxSize
                        height = (maxSize / ratio).toInt()
                    } else {
                        height = maxSize
                        width = (maxSize * ratio).toInt()
                    }
                }
                
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                
                val tempFile = File(getApplication<Application>().cacheDir, "temp_$machineId.$extension")
                val outputStream = FileOutputStream(tempFile)
                
                val format = if (extension.lowercase() == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                scaledBitmap.compress(format, 85, outputStream)
                outputStream.close()
                
                tempFile
            } catch (e: Exception) {
                Log.e(TAG, "Compression error", e)
                null
            }
        }
    }

    fun resetState() {
        _state.value = AddMachineryState()
    }
}
