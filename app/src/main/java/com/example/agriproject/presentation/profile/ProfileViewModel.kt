package com.example.agriproject.presentation.profile

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

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val location: String = "",
    val farmSize: String = "",
    val cropTypes: String = "",
    val language: String = "English",
    val isDarkMode: Boolean = false
)

data class ProfileState(
    val user: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                val profile = doc.toObject(UserProfile::class.java) ?: UserProfile(
                    uid = uid,
                    name = auth.currentUser?.displayName ?: "Farmer",
                    email = auth.currentUser?.email ?: ""
                )
                _state.value = _state.value.copy(user = profile, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun updateProfile(name: String, location: String, farmSize: String, cropTypes: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val updates = mapOf(
                    "name" to name,
                    "location" to location,
                    "farmSize" to farmSize,
                    "cropTypes" to cropTypes
                )
                firestore.collection("users").document(uid).update(updates).await()
                _state.value = _state.value.copy(
                    user = _state.value.user.copy(name = name, location = location, farmSize = farmSize, cropTypes = cropTypes),
                    isLoading = false,
                    isUpdateSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val ref = storage.reference.child("profile_images/$uid.jpg")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                firestore.collection("users").document(uid).update("profileImageUrl", url).await()
                _state.value = _state.value.copy(
                    user = _state.value.user.copy(profileImageUrl = url),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(uid).update("isDarkMode", enabled)
            _state.value = _state.value.copy(user = _state.value.user.copy(isDarkMode = enabled))
        }
    }

    fun setLanguage(lang: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(uid).update("language", lang)
            _state.value = _state.value.copy(user = _state.value.user.copy(language = lang))
        }
    }

    fun logout() {
        auth.signOut()
        _state.value = _state.value.copy(isLoggedOut = true)
    }

    fun deleteAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                firestore.collection("users").document(uid).delete().await()
                user.delete().await()
                _state.value = _state.value.copy(isLoggedOut = true, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }
    
    fun resetUpdateSuccess() {
        _state.value = _state.value.copy(isUpdateSuccess = false)
    }
}
