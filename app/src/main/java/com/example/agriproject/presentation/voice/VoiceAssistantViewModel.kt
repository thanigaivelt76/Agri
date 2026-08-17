package com.example.agriproject.presentation.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class VoiceState(
    val isListening: Boolean = false,
    val text: String = "",
    val response: String = "",
    val isSpeaking: Boolean = false
)

class VoiceAssistantViewModel : ViewModel() {
    private val _state = MutableStateFlow(VoiceState())
    val state = _state.asStateFlow()

    private var tts: TextToSpeech? = null

    fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                }
            }
        }
    }

    fun onSpeechResult(result: String) {
        _state.value = _state.value.copy(text = result, isListening = false)
        processQuery(result)
    }

    private fun processQuery(query: String) {
        val lowerQuery = query.lowercase()
        val response = when {
            lowerQuery.contains("weather") -> "The current temperature in Coimbatore is 28 degrees Celsius with mostly sunny skies."
            lowerQuery.contains("crop") || lowerQuery.contains("disease") -> "Please use the AI Crop Assistant to scan your leaf for disease detection."
            lowerQuery.contains("price") || lowerQuery.contains("market") -> "Tomato prices are currently 30 rupees per kg, and Rice is 55 rupees per kg."
            lowerQuery.contains("machinery") || lowerQuery.contains("tractor") -> "There are 3 tractors available for rent near your location."
            lowerQuery.contains("hello") || lowerQuery.contains("hi") -> "Hello Farmer! How can I help you today with your farming needs?"
            else -> "I'm not sure about that. Try asking about weather, crop prices, or nearby machinery."
        }
        _state.value = _state.value.copy(response = response)
        speak(response)
    }

    private fun speak(text: String) {
        _state.value = _state.value.copy(isSpeaking = true)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun startListening() {
        _state.value = _state.value.copy(isListening = true, text = "", response = "")
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
