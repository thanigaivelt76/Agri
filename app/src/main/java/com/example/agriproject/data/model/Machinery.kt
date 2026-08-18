package com.example.agriproject.data.model

import com.google.firebase.firestore.DocumentId

data class Machinery(
    @DocumentId
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val type: String = "", // Tractor, Harvester, etc.
    val name: String = "",
    val registrationNumber: String = "",
    val phoneNumber: String = "",
    val imageUrl: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val price: Double = 0.0,
    val rentalUnit: String = "Per Hour", // Per Hour, Half Day, Full Day
    val description: String = "",
    val isAvailable: Boolean = true,
    val rating: Double = 5.0,
    val createdAt: Long = System.currentTimeMillis()
)
