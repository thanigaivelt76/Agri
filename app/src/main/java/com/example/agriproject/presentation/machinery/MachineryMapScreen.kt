package com.example.agriproject.presentation.machinery

import android.location.Location
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

data class MachineryMarker(
    val id: String,
    val name: String,
    val type: String,
    val latLng: LatLng,
    val price: Int,
    val rating: Double,
    val owner: String,
    val image: String,
    val availability: String = "Available"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineryMapScreen(
    onBack: () -> Unit,
    onSeeDetails: (String) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    
    var selectedMachine by remember { mutableStateOf<MachineryMarker?>(null) }
    var currentRadius by remember { mutableStateOf(5f) }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(11.0168, 76.9558), 13f)
    }

    val mockMachinery = listOf(
        MachineryMarker("1", "John Deere 5050D", "Tractor", LatLng(11.0200, 76.9600), 1500, 4.8, "Ramesh Farms", "https://images.unsplash.com/photo-1594488310342-998f451f28b7"),
        MachineryMarker("2", "Kubota Harvester", "Harvester", LatLng(11.0100, 76.9500), 3200, 4.9, "Agro Masters", "https://images.unsplash.com/photo-1592991538534-00972b6f59ab"),
        MachineryMarker("3", "Rotavator 7ft", "Rotavator", LatLng(11.0300, 76.9400), 800, 4.6, "Green Tech", "https://images.unsplash.com/photo-1570586111516-e58f00095817")
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            selectedMachine?.let { machine ->
                MachineDetailSheet(machine, onSeeDetails)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Rent Machinery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)
            ) {
                mockMachinery.forEach { machine ->
                    val markerState = rememberMarkerState(position = machine.latLng)
                    Marker(
                        state = markerState,
                        title = "${machine.name} - ₹${machine.price}/hr",
                        snippet = "Rating: ${machine.rating}",
                        onClick = {
                            selectedMachine = machine
                            scope.launch { scaffoldState.bottomSheetState.expand() }
                            true
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                // Category Filter
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val categories = listOf("All", "Tractor", "Harvester", "Rotavator", "Seeder", "Drone")
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Radius Filter
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Radius: ${currentRadius.toInt()} km", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = currentRadius,
                            onValueChange = { currentRadius = it },
                            valueRange = 5f..50f,
                            steps = 3,
                            modifier = Modifier.width(150.dp).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MachineDetailSheet(
    machine: MachineryMarker,
    onSeeDetails: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = machine.image,
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(machine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(machine.owner, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text("${machine.rating} • 1.2 km away", fontSize = 12.sp)
                }
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Price", color = Color.Gray, fontSize = 12.sp)
                Text("₹${machine.price}/hr", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Status", color = Color.Gray, fontSize = 12.sp)
                Text(machine.availability, color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onSeeDetails(machine.id) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Book Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
