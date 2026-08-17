package com.example.agriproject.presentation.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetectionResult(
    val diseaseName: String,
    val confidence: Float,
    val treatment: String,
    val suggestedFertilizer: String,
    val nearbyShop: String
)

data class AiAssistantState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: DetectionResult? = null,
    val capturedImage: Bitmap? = null
)

class AiAssistantViewModel : ViewModel() {
    private val _state = MutableStateFlow(AiAssistantState())
    val state = _state.asStateFlow()

    fun analyzeImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, result = null)
            
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                
                _state.value = _state.value.copy(capturedImage = bitmap)
                
                // Simulate TFLite Inference delay
                delay(2000)
                
                // Mock Prediction logic
                // In a real app, you'd use: 
                // val model = MyModel.newInstance(context)
                // val outputs = model.process(inputBuffer)
                
                val mockResults = listOf(
                    DetectionResult("Healthy", 0.98f, "No treatment required. Maintain regular watering.", "Standard NPK 10-10-10", "Green Earth Organics"),
                    DetectionResult("Leaf Spot", 0.85f, "Remove infected leaves. Apply copper-based fungicide.", "Bio-Fungicide Plus", "Agro Care Center"),
                    DetectionResult("Rust", 0.92f, "Improve air circulation. Use sulfur or neem oil spray.", "Neem Gold", "Farmers Friend Hub"),
                    DetectionResult("Blast", 0.78f, "Avoid excessive nitrogen. Use tricyclazole spray.", "Blast-Off 500", "Rural Agri Store"),
                    DetectionResult("Wilt", 0.88f, "Check for soil drainage issues. Use carbendazim soil drench.", "Soil-Safe", "Modern Farm Supplies")
                )
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    result = mockResults.random()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Failed to process image: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _state.value = AiAssistantState()
    }
}
