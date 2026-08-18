package com.example.agriproject.presentation.dashboard

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agriproject.ui.theme.GreenPrimary
import com.example.agriproject.notifications.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onMachineryClick: () -> Unit = {},
    onWorkersClick: () -> Unit = {},
    onMarketplaceClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAiAssistantClick: () -> Unit = {},
    onVoiceAssistantClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    notificationViewModel: NotificationViewModel? = null,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val state by dashboardViewModel.state.collectAsState()
    val notificationState by notificationViewModel?.state?.collectAsState() ?: remember { mutableStateOf(null) }
    val context = LocalContext.current

    // Permissions for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            dashboardViewModel.loadData()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Welcome, ${state.userName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                            Text(state.locationName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (notificationState != null && notificationState!!.unreadCount > 0) {
                                Badge { Text(notificationState!!.unreadCount.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null)
                        }
                    }
                    IconButton(onClick = onProfileClick) {
                        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color.LightGray) {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Agriculture, null) },
                    label = { Text("Machinery") },
                    selected = false,
                    onClick = onMachineryClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Groups, null) },
                    label = { Text("Workers") },
                    selected = false,
                    onClick = onWorkersClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Storefront, null) },
                    label = { Text("Market") },
                    selected = false,
                    onClick = onMarketplaceClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Weather Widget
                item {
                    WeatherCard(
                        temp = state.weather?.current?.temp?.let { "${it.toInt()}°C" } ?: "28°C",
                        condition = state.weather?.current?.weather?.firstOrNull()?.main ?: "Mostly Sunny",
                        humidity = "${state.weather?.current?.humidity ?: 65}%",
                        windSpeed = "${state.weather?.current?.wind_speed ?: 12} km/h",
                        onClick = onWeatherClick
                    )
                }

                // AI Crop Assistant Card
                item {
                    AICropAssistantCard(onClick = onAiAssistantClick)
                }

                // Voice Assistant Card
                item {
                    VoiceAssistantCard()
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
        }
    }
}

@Composable
fun WeatherCard(
    temp: String,
    condition: String,
    humidity: String,
    windSpeed: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(temp, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(condition, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    WeatherInfo(Icons.Default.WaterDrop, humidity)
                    Spacer(modifier = Modifier.width(16.dp))
                    WeatherInfo(Icons.Default.Air, windSpeed)
                }
            }
            Icon(
                imageVector = when (condition.lowercase()) {
                    "rain" -> Icons.Default.WaterDrop
                    "clouds" -> Icons.Default.Cloud
                    else -> Icons.Default.WbSunny
                },
                contentDescription = null,
                tint = Color(0xFFFFD600),
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
fun WeatherInfo(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
fun AICropAssistantCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = GreenPrimary.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.Psychology, null, tint = GreenPrimary, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("AI Crop Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Get smart assistance for your crops", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Ask about crops, diseases, fertilizers and farming methods.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ask AI")
            }
        }
    }
}

@Composable
fun VoiceAssistantCard() {
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            speechRecognizer.startListening(speechIntent)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Voice Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Speak to Uzhavu Thozhan", color = Color.Gray, fontSize = 14.sp)

            if (recognizedText.isNotEmpty()) {
                Surface(
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "\"$recognizedText\"",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    recognizedText = ""
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color.Red else GreenPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(if (isListening) Icons.Default.Stop else Icons.Default.GraphicEq, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isListening) "Listening..." else "Tap to Speak", fontWeight = FontWeight.Bold)
            }
        }
    }
}
