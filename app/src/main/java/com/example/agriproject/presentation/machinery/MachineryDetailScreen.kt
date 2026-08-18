package com.example.agriproject.presentation.machinery

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agriproject.data.model.Machinery
import com.example.agriproject.data.repository.MachineryRepository
import com.example.agriproject.ui.theme.GreenPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MachineryDetailViewModel : ViewModel() {
    private val repository = MachineryRepository()
    private val _state = MutableStateFlow(MachineryDetailState())
    val state = _state.asStateFlow()

    fun loadMachinery(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val machine = repository.getMachineryById(id)
            _state.value = _state.value.copy(machinery = machine, isLoading = false)
        }
    }
}

data class MachineryDetailState(
    val machinery: Machinery? = null,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineryDetailScreen(
    machineId: String,
    onBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onBookClick: (String) -> Unit
) {
    val viewModel: MachineryDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(machineId) {
        viewModel.loadMachinery(machineId)
    }

    Scaffold(
        bottomBar = {
            state.machinery?.let { machine ->
                BottomAppBar(containerColor = Color.White, tonalElevation = 8.dp, modifier = Modifier.height(80.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Rental Price", color = Color.Gray, fontSize = 12.sp)
                            Text("₹${machine.price}/${machine.rentalUnit}", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 20.sp)
                        }
                        Button(
                            onClick = { onBookClick("machinery_booking/${machine.id}") },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(160.dp).height(48.dp)
                        ) {
                            Text("Book Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        state.machinery?.let { machine ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
                Box {
                    AsyncImage(
                        model = machine.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(machine.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(color = GreenPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(machine.type, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    
                    Text("Reg No: ${machine.registrationNumber}", color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        DetailItem(Icons.Default.Star, machine.rating.toString(), "Rating")
                        DetailItem(Icons.Default.LocationOn, "2.5 km", "Distance")
                        DetailItem(Icons.Default.CheckCircle, if(machine.isAvailable) "Available" else "Busy", "Status")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(machine.description, color = Color.DarkGray, modifier = Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Owner Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color.LightGray) { Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp)) }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(machine.ownerName, fontWeight = FontWeight.Bold)
                                Text("Verified Owner", fontSize = 12.sp, color = GreenPrimary)
                            }
                            Row {
                                IconButton(onClick = {}, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.Default.Chat, null, tint = GreenPrimary) }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {}, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.Default.Call, null, tint = GreenPrimary) }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GreenPrimary)
                    ) {
                        Icon(Icons.Default.Navigation, null, tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Navigate to Machine", color = GreenPrimary)
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
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}
