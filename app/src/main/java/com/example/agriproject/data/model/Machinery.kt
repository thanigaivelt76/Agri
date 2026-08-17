package com.example.agriproject.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Machinery(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val pricePerHour: Double = 0.0,
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerContact: String = "",
    val imageUrl: String? = null,
    val location: GeoPoint? = null,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
