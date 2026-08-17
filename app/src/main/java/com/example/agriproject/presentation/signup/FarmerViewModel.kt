package com.example.agriproject.presentation.signup

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

data class FarmerRegistrationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class FarmerViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _state = MutableStateFlow(FarmerRegistrationState())
    val state = _state.asStateFlow()

    fun registerFarmer(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        state: String,
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

                // 3. Prepare Farmer Data
                val farmerData = hashMapOf(
                    "uid" to userId,
                    "fullName" to fullName,
                    "mobileNumber" to mobileNumber,
                    "email" to email,
                    "state" to state,
                    "district" to district,
                    "village" to village,
                    "pincode" to pincode,
                    "farmSize" to farmSize,
                    "cropType" to cropType,
                    "latitude" to location?.first,
                    "longitude" to location?.second,
                    "profileImageUrl" to imageUrl,
                    "role" to "Farmer",
                    "createdAt" to System.currentTimeMillis()
                )

                // 4. Save to Firestore
                firestore.collection("users").document(userId).set(farmerData).await()

                _state.value = FarmerRegistrationState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = FarmerRegistrationState(error = e.localizedMessage)
            }
        }
    }
}
