package com.example.agriproject.presentation.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class MessageType { TEXT, IMAGE, VOICE }

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val voiceUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: MessageType = MessageType.TEXT
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isOtherUserOnline: Boolean = false,
    val isOtherUserTyping: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    private var currentChatId: String? = null

    fun startChat(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        // Simple chat ID generation: sorted IDs joined
        val chatId = if (currentUserId < otherUserId) "${currentUserId}_${otherUserId}" else "${otherUserId}_${currentUserId}"
        currentChatId = chatId
        
        observeMessages(chatId)
        observeUserStatus(otherUserId)
        observeTypingStatus(chatId, otherUserId)
    }

    private fun observeMessages(chatId: String) {
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _state.value = _state.value.copy(error = e.localizedMessage)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                _state.value = _state.value.copy(messages = messages)
                
                // Mark as read
                markMessagesAsRead(chatId)
            }
    }

    private fun observeUserStatus(userId: String) {
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val isOnline = snapshot?.getBoolean("isOnline") ?: false
                _state.value = _state.value.copy(isOtherUserOnline = isOnline)
            }
    }

    private fun observeTypingStatus(chatId: String, otherUserId: String) {
        firestore.collection("chats").document(chatId)
            .collection("typing")
            .document(otherUserId)
            .addSnapshotListener { snapshot, _ ->
                val isTyping = snapshot?.getBoolean("isTyping") ?: false
                _state.value = _state.value.copy(isOtherUserTyping = isTyping)
            }
    }

    fun sendMessage(text: String) {
        val chatId = currentChatId ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = currentUserId,
            text = text,
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT
        )
        
        firestore.collection("chats").document(chatId)
            .collection("messages").document(message.id).set(message)
    }

    fun sendImage(uri: Uri) {
        viewModelScope.launch {
            val chatId = currentChatId ?: return@launch
            val currentUserId = auth.currentUser?.uid ?: return@launch
            
            try {
                val fileName = "chat_images/${UUID.randomUUID()}.jpg"
                val ref = storage.reference.child(fileName)
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                
                val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = currentUserId,
                    imageUrl = url,
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.IMAGE
                )
                
                firestore.collection("chats").document(chatId)
                    .collection("messages").document(message.id).set(message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.localizedMessage)
            }
        }
    }

    fun setTyping(isTyping: Boolean) {
        val chatId = currentChatId ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        
        firestore.collection("chats").document(chatId)
            .collection("typing").document(currentUserId)
            .set(mapOf("isTyping" to isTyping))
    }

    private fun markMessagesAsRead(chatId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    if (doc.getString("senderId") != currentUserId) {
                        doc.reference.update("isRead", true)
                    }
                }
            }
    }
}
