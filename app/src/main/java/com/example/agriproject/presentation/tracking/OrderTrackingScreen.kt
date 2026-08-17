package com.example.agriproject.presentation.tracking

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.agriproject.ui.theme.GreenPrimary
import androidx.compose.ui.tooling.preview.Preview
import com.example.agriproject.ui.theme.AgriProjectTheme

enum class OrderStatus(val label: String) {
    ACCEPTED("Accepted"),
    PICKUP_STARTED("Pickup Started"),
    ON_THE_WAY("On the Way"),
    DELIVERED("Delivered")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit
) {
    val pickupLocation = LatLng(13.0827, 80.2707) // Chennai
    val destinationLocation = LatLng(12.9716, 77.5946) // Bangalore
    val driverLocation by remember { mutableStateOf(LatLng(13.0000, 79.0000)) }
    
    val currentStatus = OrderStatus.ON_THE_WAY
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverLocation, 7f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Google Map
            Box(modifier = Modifier.weight(1f)) {
                val pickupMarkerState = rememberMarkerState(position = pickupLocation)
                val destinationMarkerState = rememberMarkerState(position = destinationLocation)
                val driverMarkerState = rememberMarkerState(position = driverLocation)

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = pickupMarkerState,
                        title = "Pickup",
                        snippet = "Erode Organic Farm"
                    )
                    Marker(
                        state = destinationMarkerState,
                        title = "Destination",
                        snippet = "Koyambedu Market"
                    )
                    Marker(
                        state = driverMarkerState,
                        title = "Driver",
                        snippet = "On the way to destination",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }

            // Bottom Tracking Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Estimated Arrival", color = Color.Gray, fontSize = 12.sp)
                            Text("4:30 PM (25 mins)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(onClick = { }, modifier = Modifier.background(GreenPrimary.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = GreenPrimary)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    // Timeline UI
                    TrackingTimeline(currentStatus)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFFF5F5F5)) {
                            Icon(Icons.Default.LocalShipping, null, modifier = Modifier.padding(8.dp), tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("TN 33 AB 1234", fontWeight = FontWeight.Bold)
                            Text("Senthil Kumar • Tata Ace", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingTimeline(currentStatus: OrderStatus) {
    val statuses = OrderStatus.values()
    val currentIndex = currentStatus.ordinal

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        statuses.forEachIndexed { index, status ->
            TimelineItem(
                label = status.label,
                isCompleted = index <= currentIndex,
                isLast = index == statuses.size - 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TimelineItem(
    label: String,
    isCompleted: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Line
            if (!isLast) {
                Divider(
                    modifier = Modifier.padding(start = 12.dp).align(Alignment.CenterStart),
                    color = if (isCompleted) GreenPrimary else Color.LightGray,
                    thickness = 2.dp
                )
            }
            
            // Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) GreenPrimary else Color.LightGray)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = if (isCompleted) GreenPrimary else Color.Gray,
            lineHeight = 12.sp,
            modifier = Modifier.width(60.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderTrackingPreview() {
    AgriProjectTheme {
        OrderTrackingScreen(orderId = "123", onBack = {})
    }
}
