package com.example.agriproject.presentation.machinery

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agriproject.data.model.Machinery
import com.example.agriproject.data.repository.MachineryRepository
import com.example.agriproject.ui.theme.GreenPrimary
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddMachineryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val repository = MachineryRepository()

    private val _state = MutableStateFlow(AddMachineryState())
    val state = _state.asStateFlow()

    private val TAG = "AddMachineryViewModel"

    fun addMachinery(
        type: String,
        name: String,
        regNumber: String,
        price: String,
        unit: String,
        description: String,
        imageUri: Uri?,
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                var imageUrl: String? = null
                
                // Fix: Only attempt upload if imageUri is NOT null
                if (imageUri != null) {
                    try {
                        val fileName = "machinery_images/${UUID.randomUUID()}.jpg"
                        val ref = storage.reference.child(fileName)
                        Log.d(TAG, "Uploading image to: $fileName")
                        ref.putFile(imageUri).await()
                        imageUrl = ref.downloadUrl.await().toString()
                        Log.d(TAG, "Upload success. URL: $imageUrl")
                    } catch (e: Exception) {
                        Log.e(TAG, "Storage error", e)
                        throw Exception("Image upload failed. Please check your internet connection.")
                    }
                }

                val machinery = Machinery(
                    ownerId = userId,
                    ownerName = auth.currentUser?.displayName ?: "Owner",
                    type = type,
                    name = name,
                    registrationNumber = regNumber,
                    phoneNumber = auth.currentUser?.phoneNumber ?: "",
                    imageUrl = imageUrl,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    price = price.toDoubleOrNull() ?: 0.0,
                    rentalUnit = unit,
                    description = description
                )

                Log.d(TAG, "Saving machinery to Firestore")
                repository.addMachinery(machinery).onSuccess {
                    Log.d(TAG, "Save success")
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }.onFailure { e ->
                    Log.e(TAG, "Firestore error", e)
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to save: ${e.localizedMessage}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "General error", e)
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}

data class AddMachineryState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMachineryScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val viewModel: AddMachineryViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var machineType by remember { mutableStateOf("Tractor") }
    var machineName by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Per Hour") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var address by remember { mutableStateOf("") }
    var showMapPicker by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    if (showMapPicker) {
        MapPicker(
            onLocationSelected = { latLng ->
                latitude = latLng.latitude
                longitude = latLng.longitude
                address = "Selected Location (Map)"
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Machinery", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = GreenPrimary, modifier = Modifier.size(48.dp))
                            Text("Add Machine Photo", color = GreenPrimary)
                        }
                    }
                }

                // Form
                Text("Machine Information", fontWeight = FontWeight.Bold, color = GreenPrimary)
                
                var expanded by remember { mutableStateOf(false) }
                val types = listOf("Tractor", "Harvester", "Rotavator", "Cultivator", "Seeder", "Sprayer", "Power Tiller", "Other")
                
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = machineType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Machine Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = { machineType = type; expanded = false })
                        }
                    }
                }

                OutlinedTextField(value = machineName, onValueChange = { machineName = it }, label = { Text("Machine Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = regNumber, onValueChange = { regNumber = it }, label = { Text("Registration Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                
                Text("Location", fontWeight = FontWeight.Bold, color = GreenPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            latitude = 11.0168
                            longitude = 76.9558
                            address = "Coimbatore, Tamil Nadu"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Current", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Map", fontSize = 12.sp)
                    }
                }
                
                if (address.isNotEmpty()) {
                    Text(address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Text("Rental Details", fontWeight = FontWeight.Bold, color = GreenPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                    
                    var unitExpanded by remember { mutableStateOf(false) }
                    val units = listOf("Per Hour", "Half Day", "Full Day")
                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            units.forEach { u ->
                                DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                            }
                        }
                    }
                }

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = GreenPrimary)
                } else {
                    Button(
                        onClick = {
                            if (machineName.isNotEmpty() && price.isNotEmpty() && latitude != 0.0) {
                                viewModel.addMachinery(machineType, machineName, regNumber, price, unit, description, imageUri, latitude, longitude, address)
                            } else {
                                // Explicitly check for location
                                if (latitude == 0.0) {
                                    // Normally you'd show a Snackbar here
                                    Log.e("AddMachinery", "Location not selected")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Machinery", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                state.error?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp)) }
            }
        }
    }
}

@Composable
fun MapPicker(onLocationSelected: (LatLng) -> Unit, onDismiss: () -> Unit) {
    var markerPosition by remember { mutableStateOf(LatLng(11.0168, 76.9558)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 10f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { markerPosition = it }
        ) {
            Marker(
                state = rememberMarkerState(position = markerPosition),
                title = "Machinery Location"
            )
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)) {
            Button(onClick = { onLocationSelected(markerPosition) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                Text("Confirm Location")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
