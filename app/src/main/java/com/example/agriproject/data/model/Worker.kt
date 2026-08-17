package com.example.agriproject.data.model

import com.google.firebase.firestore.DocumentId

data class Worker(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val skill: String = "",
    val experience: String = "",
    val contact: String = "",
    val location: String = "",
    val dailyWage: Double = 0.0,
    val isAvailable: Boolean = true,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
