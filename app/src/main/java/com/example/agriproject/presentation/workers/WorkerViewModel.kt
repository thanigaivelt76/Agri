package com.example.agriproject.presentation.workers

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

data class WorkerRegistrationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class WorkerViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _state = MutableStateFlow(WorkerRegistrationState())
    val state = _state.asStateFlow()

    fun registerWorker(
        name: String,
        age: String,
        experience: String,
        skills: List<String>,
        languages: List<String>,
        dailyWage: String,
        availableDays: List<String>,
        location: String,
        photoUri: Uri?,
        aadhaarUri: Uri?
    ) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = WorkerRegistrationState(isLoading = true)
            try {
                var photoUrl: String? = null
                var aadhaarUrl: String? = null

                photoUri?.let {
                    val ref = storage.reference.child("worker_photos/$userId.jpg")
                    ref.putFile(it).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                aadhaarUri?.let {
                    val ref = storage.reference.child("worker_aadhaar/$userId.pdf")
                    ref.putFile(it).await()
                    aadhaarUrl = ref.downloadUrl.await().toString()
                }

                val workerData = hashMapOf(
                    "uid" to userId,
                    "name" to name,
                    "age" to age,
                    "experience" to experience,
                    "skills" to skills,
                    "languages" to languages,
                    "dailyWage" to dailyWage,
                    "availableDays" to availableDays,
                    "location" to location,
                    "photoUrl" to photoUrl,
                    "aadhaarUrl" to aadhaarUrl,
                    "role" to "Worker",
                    "isVerified" to false,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("workers").document(userId).set(workerData).await()
                firestore.collection("users").document(userId).update("role", "Worker")

                _state.value = WorkerRegistrationState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = WorkerRegistrationState(error = e.localizedMessage)
            }
        }
    }
}
