package com.example.agriproject.notifications

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AgriNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false,
    val userId: String = ""
)

data class NotificationState(
    val notifications: List<AgriNotification> = emptyList(),
    val unreadCount: Int = 0
)

class NotificationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(NotificationState())
    val state = _state.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val notifications = snapshot?.toObjects(AgriNotification::class.java) ?: emptyList()
                val unreadCount = notifications.count { !it.isRead }
                _state.value = NotificationState(notifications, unreadCount)
            }
    }

    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.update("isRead", true)
                }
            }
    }

    fun deleteNotification(notificationId: String) {
        firestore.collection("notifications")
            .whereEqualTo("id", notificationId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.firstOrNull()?.reference?.delete()
            }
    }
}
