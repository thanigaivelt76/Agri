package com.example.agriproject.presentation.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriproject.data.model.User
import com.example.agriproject.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FarmerRegistrationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class FarmerViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userRepository = UserRepository()

    private val _state = MutableStateFlow(FarmerRegistrationState())
    val state = _state.asStateFlow()

    fun registerFarmer(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        stateName: String,
        district: String,
        village: String,
        pincode: String,
        farmSize: String,
        cropType: String,
        location: Pair<Double, Double>?,
        profileImageUri: Uri?
    ) {
        viewModelScope.launch {
            _state.value = FarmerRegistrationState(isLoading = true)
            try {
                // 1. Create Firebase Auth User
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("User creation failed")

                var imageUrl: String? = null
                
                // 2. Upload Profile Image to Firebase Storage
                profileImageUri?.let { uri ->
                    val fileName = "profile_images/$userId.jpg"
                    val ref = storage.reference.child(fileName)
                    ref.putFile(uri).await()
                    imageUrl = ref.downloadUrl.await().toString()
                }

                // 3. Prepare User Object
                val user = User(
                    uid = userId,
                    name = fullName,
                    email = email,
                    phoneNumber = mobileNumber,
                    role = "Farmer",
                    location = "$village, $district, $stateName",
                    profileImageUrl = imageUrl
                )

                // 4. Save using Repository
                userRepository.saveUser(user).onSuccess {
                    _state.value = FarmerRegistrationState(isSuccess = true)
                }.onFailure { e ->
                    _state.value = FarmerRegistrationState(error = e.localizedMessage)
                }

            } catch (e: Exception) {
                _state.value = FarmerRegistrationState(error = e.localizedMessage)
            }
        }
    }
}
