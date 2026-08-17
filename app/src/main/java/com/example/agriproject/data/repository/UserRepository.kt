package com.example.agriproject.data.repository

import com.example.agriproject.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")

    suspend fun saveUser(user: User): Result<Unit> = try {
        userCollection.document(user.uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUser(uid: String): User? = try {
        userCollection.document(uid).get().await().toObject(User::class.java)
    } catch (e: Exception) {
        null
    }

    suspend fun updateUserRole(uid: String, role: String): Result<Unit> = try {
        userCollection.document(uid).update("role", role).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
