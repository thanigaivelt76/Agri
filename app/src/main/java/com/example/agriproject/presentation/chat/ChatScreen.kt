package com.example.agriproject.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    otherUserId: String,
    otherUserName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var messageText by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.sendImage(it) }
    }

    LaunchedEffect(otherUserId) {
        viewModel.startChat(otherUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.LightGray) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(otherUserName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (state.isOtherUserTyping) {
                                Text("typing...", fontSize = 12.sp, color = GreenPrimary)
                            } else if (state.isOtherUserOnline) {
                                Text("Online", fontSize = 12.sp, color = GreenPrimary)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFE5DDD5)) // WhatsApp-like background
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                items(state.messages) { message ->
                    MessageBubble(
                        message = message,
                        isFromMe = message.senderId == currentUserId
                    )
                }
            }

            // Input Bar
            ChatInputBar(
                messageText = messageText,
                onMessageChange = {
                    messageText = it
                    viewModel.setTyping(it.isNotEmpty())
                },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                        viewModel.setTyping(false)
                    }
                },
                onImageClick = { launcher.launch("image/*") }
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isFromMe: Boolean) {
    val bubbleColor = if (isFromMe) Color(0xFFDCF8C6) else Color.White
    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isFromMe) {
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(8.dp)
        ) {
            if (message.type == MessageType.IMAGE && message.imageUrl != null) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (message.type == MessageType.VOICE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, tint = GreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = 0f,
                        modifier = Modifier.weight(1f),
                        color = GreenPrimary
                    )
                }
            } else {
                Text(text = message.text, fontSize = 15.sp)
            }
            
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                Text(
                    text = sdf.format(Date(message.timestamp)),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                if (isFromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (message.isRead) Color(0xFF34B7F1) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onImageClick) {
                Icon(Icons.Default.Add, null, tint = Color.Gray)
            }
            
            TextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = { Text("Message") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = { if (messageText.isBlank()) { /* Record Voice */ } else onSendClick() },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                containerColor = GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = if (messageText.isBlank()) Icons.Default.Mic else Icons.Default.Send,
                    contentDescription = null
                )
            }
        }
    }
}
