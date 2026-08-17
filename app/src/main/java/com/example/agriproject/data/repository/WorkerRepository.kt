package com.example.agriproject.data.repository

import com.example.agriproject.data.model.Worker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkerRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val workerCollection = firestore.collection("workers")

    suspend fun registerWorker(worker: Worker): Result<Unit> = try {
        workerCollection.add(worker).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getWorkers(): Flow<List<Worker>> = callbackFlow {
        val subscription = workerCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val workers = snapshot.toObjects(Worker::class.java)
                    trySend(workers)
                }
            }
        awaitClose { subscription.remove() }
    }
}
