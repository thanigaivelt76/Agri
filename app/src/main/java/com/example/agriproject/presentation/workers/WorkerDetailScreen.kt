package com.example.agriproject.presentation.workers

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.example.agriproject.ui.theme.AgriProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    workerId: String,
    onBack: () -> Unit,
    onChatClick: (String, String) -> Unit = { _, _ -> },
    onBookClick: (String) -> Unit = {}
) {
    val worker = remember { allWorkers.find { it.id == workerId } ?: allWorkers[0] }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(100.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Wage", color = Color.Gray, fontSize = 12.sp)
                        Text("₹${worker.dailyWage}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                    Button(
                        onClick = { onBookClick("payment/${worker.dailyWage}/${worker.name} Booking") },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.6f).height(56.dp)
                    ) {
                        Text("Book Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Box {
                AsyncImage(
                    model = worker.photo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (worker.isAvailable) {
                        StatusBadge("Available", GreenPrimary)
                    }
                }
                Text(worker.category, color = Color.Gray, fontSize = 16.sp)
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    WorkerStat(Icons.Default.Star, worker.rating.toString(), "Rating")
                    WorkerStat(Icons.Default.WorkHistory, worker.experience, "Experience")
                    WorkerStat(Icons.Default.LocationOn, "${worker.distance} km", "Distance")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Contact Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ContactButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Chat,
                        label = "Chat",
                        color = Color(0xFFE3F2FD),
                        tint = Color(0xFF1976D2),
                        onClick = { onChatClick(worker.id, worker.name) }
                    )
                    ContactButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Call,
                        label = "Call",
                        color = Color(0xFFE8F5E9),
                        tint = GreenPrimary,
                        onClick = { }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Bio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Professional ${worker.category} with ${worker.experience} of experience in agricultural fields. Specialized in efficient operations and maintenance. Known for punctuality and high-quality work.",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text("Location", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = "https://maps.googleapis.com/maps/api/staticmap?center=11.0168,76.9558&zoom=13&size=600x300&key=MOCK",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = CircleShape,
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Icon(Icons.Default.Directions, null, tint = Color.Blue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Realtime Tracking", color = Color.Blue)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WorkerStat(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(28.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ContactButton(modifier: Modifier, icon: ImageVector, label: String, color: Color, tint: Color, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier.height(56.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = tint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(start = 12.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WorkerDetailPreview() {
    AgriProjectTheme {
        WorkerDetailScreen(workerId = "1", onBack = {}, onChatClick = { _, _ -> }, onBookClick = {})
    }
}
