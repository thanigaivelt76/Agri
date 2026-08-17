package com.example.agriproject.data.repository

import com.example.agriproject.data.model.Crop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CropRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val cropCollection = firestore.collection("crops")

    suspend fun addCrop(crop: Crop): Result<Unit> = try {
        cropCollection.add(crop).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getCrops(): Flow<List<Crop>> = callbackFlow {
        val subscription = cropCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val crops = snapshot.toObjects(Crop::class.java)
                    trySend(crops)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getCropById(cropId: String): Crop? = try {
        cropCollection.document(cropId).get().await().toObject(Crop::class.java)
    } catch (e: Exception) {
        null
    }
}
