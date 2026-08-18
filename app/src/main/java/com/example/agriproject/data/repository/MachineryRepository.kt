package com.example.agriproject.data.repository

import com.example.agriproject.data.model.Machinery
import com.example.agriproject.data.model.MachineryBooking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class MachineryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val machineryCollection = firestore.collection("machinery")
    private val bookingCollection = firestore.collection("machineryBookings")

    suspend fun addMachinery(machinery: Machinery): Result<Unit> = try {
        machineryCollection.add(machinery).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getMachineryList(): Flow<List<Machinery>> = callbackFlow {
        val subscription = machineryCollection
            .whereEqualTo("isAvailable", true)
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

    suspend fun getMachineryById(id: String): Machinery? = try {
        machineryCollection.document(id).get().await().toObject(Machinery::class.java)
    } catch (e: Exception) {
        null
    }

    suspend fun createBooking(booking: MachineryBooking): Result<Unit> = try {
        bookingCollection.add(booking).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getFarmerBookings(farmerId: String): Flow<List<MachineryBooking>> = callbackFlow {
        val subscription = bookingCollection
            .whereEqualTo("farmerId", farmerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(MachineryBooking::class.java)
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getOwnerBookings(ownerId: String): Flow<List<MachineryBooking>> = callbackFlow {
        val subscription = bookingCollection
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(MachineryBooking::class.java)
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = try {
        bookingCollection.document(bookingId).update("bookingStatus", status).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Helper for distance calculation
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
