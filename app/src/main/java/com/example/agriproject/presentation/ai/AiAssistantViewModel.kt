package com.example.agriproject.presentation.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class DetectionResult(
    val diseaseName: String,
    val status: String,
    val confidence: Int,
    val symptoms: String,
    val treatment: String,
    val prevention: String
)

data class AiAssistantState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val error: String? = null,
    val result: DetectionResult? = null,
    val capturedImage: Bitmap? = null
)

class AiAssistantViewModel : ViewModel() {
    private val _state = MutableStateFlow(AiAssistantState())
    val state = _state.asStateFlow()

    private val TAG = "AiAssistantViewModel"

    fun analyzeImage(context: Context, uri: Uri?) {
        if (uri == null) {
            _state.value = _state.value.copy(error = "Please select a valid image.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true, 
                loadingMessage = "Analyzing leaf...", 
                error = null, 
                result = null
            )
            
            try {
                val processedBitmap = withContext(Dispatchers.IO) {
                    processImageUri(context, uri)
                }

                if (processedBitmap == null) {
                    throw Exception("Unable to decode image")
                }

                _state.value = _state.value.copy(capturedImage = processedBitmap)
                
                // --- Start API/Model Request Simulation ---
                Log.d(TAG, "Requesting AI analysis for image: $uri")
                
                // Simulate network/inference delay
                delay(3000)
                
                // Mock Response based on the request requirements
                val mockResult = DetectionResult(
                    diseaseName = "Leaf Spot",
                    status = "Disease Detected",
                    confidence = 92,
                    symptoms = "Brown spots and yellowing observed on the leaf surface. Necrotic lesions with yellow halos.",
                    treatment = "Remove infected leaves. Apply copper-based fungicide or Neem oil. Avoid overhead irrigation.",
                    prevention = "Use disease-resistant varieties. Ensure proper spacing for air circulation. Practice crop rotation."
                )

                Log.d(TAG, "AI analysis successful: ${mockResult.diseaseName}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    result = mockResult
                )
                // --- End API/Model Request Simulation ---

            } catch (e: Exception) {
                Log.e(TAG, "Error processing image", e)
                val userError = if (e.localizedMessage?.contains("incomplete") == true || e.message?.contains("decode") == true) {
                    "Unable to process this image. Please select a clear JPG or PNG image of a crop leaf."
                } else {
                    e.localizedMessage ?: "An unexpected error occurred."
                }
                _state.value = _state.value.copy(isLoading = false, error = userError)
            }
        }
    }

    private fun processImageUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            Log.d(TAG, "Processing image - Uri: $uri, MIME: $mimeType")

            // 1. Read complete stream to avoid "input was incomplete" errors
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: throw Exception("Empty image data")
            inputStream?.close()

            Log.d(TAG, "Image size: ${bytes.size / 1024} KB")

            // 2. Decode with bounds to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            Log.d(TAG, "Original dimensions: ${options.outWidth}x${options.outHeight}")

            // 3. Calculate scaling to prevent OOM and huge uploads (target max 1080p)
            val targetWidth = 1080
            val targetHeight = 1080
            var inSampleSize = 1
            if (options.outHeight > targetHeight || options.outWidth > targetWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= targetHeight && halfWidth / inSampleSize >= targetWidth) {
                    inSampleSize *= 2
                }
            }

            // 4. Decode full bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)

            // 5. Compress and convert to format for API (if needed)
            // Here we just return the bitmap, but in a real API call you'd return the ByteArray
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Internal image processing failed", e)
            null
        }
    }

    fun resetState() {
        _state.value = AiAssistantState()
    }
}
