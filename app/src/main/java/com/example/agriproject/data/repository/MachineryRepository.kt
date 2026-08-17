package com.example.agriproject.data.repository

import com.example.agriproject.data.model.Machinery
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MachineryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val machineryCollection = firestore.collection("machinery")

    suspend fun addMachinery(machinery: Machinery): Result<Unit> = try {
        machineryCollection.add(machinery).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getMachineryList(): Flow<List<Machinery>> = callbackFlow {
        val subscription = machineryCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Machinery::class.java)
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }
}
