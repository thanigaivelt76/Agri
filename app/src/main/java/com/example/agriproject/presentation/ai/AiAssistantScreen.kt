package com.example.agriproject.presentation.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agriproject.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onBack: () -> Unit,
    viewModel: AiAssistantViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.analyzeImage(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Crop Health Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Upload a clear photo of the crop leaf to detect diseases using AI.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Image Selection Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F5F5))
                    .border(2.dp, GreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.capturedImage != null) {
                    Image(
                        bitmap = state.capturedImage!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = GreenPrimary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tap to Select Leaf Image", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = state.result != null,
                enter = fadeIn() + expandVertically()
            ) {
                state.result?.let { result ->
                    ResultSection(result)
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            if (state.result != null) {
                Button(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Analyze Another Leaf", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ResultSection(result: DetectionResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Diagnosis Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Diagnosis", fontWeight = FontWeight.Bold, color = GreenPrimary)
                    Surface(
                        color = GreenPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${(result.confidence * 100).toInt()}% Confidence",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    result.diseaseName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (result.diseaseName == "Healthy") GreenPrimary else Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Treatment & Recommendations
        ResultItem(Icons.Default.Healing, "Treatment Plan", result.treatment)
        ResultItem(Icons.Default.Science, "Suggested Fertilizer", result.suggestedFertilizer)
        ResultItem(Icons.Default.Store, "Nearby Agriculture Shop", result.nearbyShop)
    }
}

@Composable
fun ResultItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFF5F5F5)
        ) {
            Icon(icon, null, modifier = Modifier.padding(10.dp), tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(content, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
