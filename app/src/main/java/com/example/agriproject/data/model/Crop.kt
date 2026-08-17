package com.example.agriproject.data.model

import com.google.firebase.firestore.DocumentId

data class Crop(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val quantity: String = "",
    val price: String = "",
    val unit: String = "kg",
    val quality: String = "",
    val description: String = "",
    val location: String = "",
    val harvestDate: String = "",
    val isOrganic: Boolean = false,
    val imageUrl: String? = null,
    val farmerId: String = "",
    val farmerName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
