package com.example.agriproject.presentation.signup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBack: () -> Unit, 
    onSignUpComplete: () -> Unit,
    viewModel: FarmerViewModel = viewModel()
) {
    val registrationState by viewModel.state.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("") }
    
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showMap by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    LaunchedEffect(registrationState.isSuccess) {
        if (registrationState.isSuccess) {
            onSignUpComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farmer Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = GreenPrimary)
                    }
                }
            )
        }
    ) { padding ->
        if (showMap) {
            MapPicker(
                onLocationSelected = { latLng ->
                    selectedLocation = latLng
                    showMap = false
                },
                onDismiss = { showMap = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Image Picker
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri != null) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White)
                    }
                }
                Text("Upload Profile Image", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(24.dp))

                SignUpTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", icon = Icons.Default.Person)
                SignUpTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = "Mobile Number", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                SignUpTextField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                
                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                var confirmPasswordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                SignUpTextField(value = state, onValueChange = { state = it }, label = "State", icon = Icons.Default.Map)
                SignUpTextField(value = district, onValueChange = { district = it }, label = "District", icon = Icons.Default.LocationCity)
                SignUpTextField(value = village, onValueChange = { village = it }, label = "Village", icon = Icons.Default.Home)
                SignUpTextField(value = pincode, onValueChange = { pincode = it }, label = "Pincode", icon = Icons.Default.PinDrop, keyboardType = KeyboardType.Number)
                SignUpTextField(value = farmSize, onValueChange = { farmSize = it }, label = "Farm Size (Acres)", icon = Icons.Default.Straighten, keyboardType = KeyboardType.Decimal)
                SignUpTextField(value = cropType, onValueChange = { cropType = it }, label = "Crop Type", icon = Icons.Default.Grass)

                Spacer(modifier = Modifier.height(16.dp))

                // GPS Location Selector
                OutlinedCard(
                    onClick = { showMap = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (selectedLocation == null) "Select GPS Farm Location" else "Location Selected: ${"%.4f".format(selectedLocation!!.latitude)}, ${"%.4f".format(selectedLocation!!.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (registrationState.isLoading) {
                    CircularProgressIndicator(color = GreenPrimary)
                } else {
                    Button(
                        onClick = {
                            if (password.isNotEmpty() && password == confirmPassword) {
                                viewModel.registerFarmer(
                                    fullName, mobileNumber, email, password, state, district, village, 
                                    pincode, farmSize, cropType, 
                                    selectedLocation?.let { it.latitude to it.longitude }, 
                                    profileImageUri
                                )
                            } else {
                                // Error feedback would go here
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                registrationState.error?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun MapPicker(onLocationSelected: (LatLng) -> Unit, onDismiss: () -> Unit) {
    var markerPosition by remember { mutableStateOf(LatLng(11.0168, 76.9558)) } // Default Coimbatore
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
                title = "Farm Location",
                draggable = true
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth()
        ) {
            Button(
                onClick = { onLocationSelected(markerPosition) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Confirm Location")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White))
            ) {
                Text("Cancel", color = GreenPrimary)
            }
        }
    }
}
