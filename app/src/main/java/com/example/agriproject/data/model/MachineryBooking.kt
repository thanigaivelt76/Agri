package com.example.agriproject.data.model

import com.google.firebase.firestore.DocumentId

data class MachineryBooking(
    @DocumentId
    val bookingId: String = "",
    val machineId: String = "",
    val ownerId: String = "",
    val farmerId: String = "",
    val machineName: String = "",
    val bookingDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val farmLatitude: Double = 0.0,
    val farmLongitude: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentStatus: String = "Pending",
    val bookingStatus: String = "Pending", // Pending, Accepted, Confirmed, Machine On The Way, Service Started, Completed
    val createdAt: Long = System.currentTimeMillis()
)
