package com.example.agriproject.presentation.machinery

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.agriproject.data.model.Machinery
import com.example.agriproject.ui.theme.GreenPrimary
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineryMapScreen(
    onBack: () -> Unit,
    onSeeDetails: (String) -> Unit,
    onAddMachinery: () -> Unit = {},
    viewModel: MachineryMapViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val categories = listOf("All", "Tractor", "Harvester", "Rotavator", "Cultivator")
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) viewModel.fetchLocationAndMachinery()
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rent Machinery", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.FilterList, null) } }
            )
        },
        floatingActionButton = {
            if (state.userRole == "MachineryOwner") {
                ExtendedFloatingActionButton(
                    onClick = onAddMachinery,
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add Machinery") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Category Chips
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.updateCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenPrimary, selectedLabelColor = Color.White)
                    )
                }
            }

            // Radius Selector
            RadiusSelector(
                currentRadius = state.radius,
                onRadiusChange = { viewModel.updateRadius(it) }
            )

            // Map Area
            Box(modifier = Modifier.weight(0.6f).fillMaxWidth()) {
                val userLatLng = state.userLocation?.let { LatLng(it.first, it.second) } ?: LatLng(11.0168, 76.9558)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(userLatLng, 12f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = state.userLocation != null)
                ) {
                    state.nearbyMachinery.forEach { machine ->
                        val markerState = rememberMarkerState(position = LatLng(machine.latitude, machine.longitude))
                        Marker(
                            state = markerState,
                            title = machine.name,
                            snippet = "₹${machine.price}/${machine.rentalUnit}",
                            onClick = { onSeeDetails(machine.id); true }
                        )
                    }
                }
            }

            // Bottom List
            Column(modifier = Modifier.weight(0.4f).background(Color.White)) {
                Text(
                    "Nearby Available Machinery",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                if (state.nearbyMachinery.isEmpty() && !state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No machinery available within ${state.radius.toInt()} km.", color = Color.Gray)
                            TextButton(onClick = { viewModel.updateRadius(state.radius + 10) }) {
                                Text("Increase Search Radius", color = GreenPrimary)
                            }
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)
                    ) {
                        items(state.nearbyMachinery) { machine ->
                            MachineryMiniCard(machine, onClick = { onSeeDetails(machine.id) })
                        }
                    }
                }
            }
        }
        
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        }
    }
}

@Composable
fun RadiusSelector(currentRadius: Float, onRadiusChange: (Float) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Radius: ${currentRadius.toInt()} km", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Slider(
                value = currentRadius,
                onValueChange = onRadiusChange,
                valueRange = 5f..50f,
                steps = 3,
                colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary)
            )
        }
    }
}

@Composable
fun MachineryMiniCard(machine: Machinery, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(260.dp).fillMaxHeight().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column {
            AsyncImage(
                model = machine.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(machine.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text(" ${machine.rating} • Available", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("₹${machine.price}/${machine.rentalUnit}", fontWeight = FontWeight.Bold, color = GreenPrimary)
                Text("Owner: ${machine.ownerName}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
