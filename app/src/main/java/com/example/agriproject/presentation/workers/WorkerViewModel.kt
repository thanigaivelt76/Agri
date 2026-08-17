package com.example.agriproject.presentation.workers

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.Worker
import com.example.agriproject.data.repository.UserRepository
import com.example.agriproject.data.repository.WorkerRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class WorkerRegistrationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class WorkerViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val workerRepository = WorkerRepository()
    private val userRepository = UserRepository()

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

                val worker = Worker(
                    id = userId,
                    name = name,
                    skill = skills.joinToString(", "),
                    experience = experience,
                    contact = auth.currentUser?.phoneNumber ?: "",
                    location = location,
                    dailyWage = dailyWage.toDoubleOrNull() ?: 0.0,
                    profileImageUrl = photoUrl
                )

                workerRepository.registerWorker(worker).onSuccess {
                    userRepository.updateUserRole(userId, "Worker")
                    _state.value = WorkerRegistrationState(isSuccess = true)
                }.onFailure { e ->
                    _state.value = WorkerRegistrationState(error = e.localizedMessage)
                }
            } catch (e: Exception) {
                _state.value = WorkerRegistrationState(error = e.localizedMessage)
            }
        }
    }
}
