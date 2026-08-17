package com.example.agriproject.presentation.workers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agriproject.ui.theme.GreenPrimary
import com.example.agriproject.ui.theme.AgriProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegistrationScreen(
    onBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: WorkerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var dailyWage by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    var photoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var aadhaarUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { photoUri = it }
    val aadhaarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { aadhaarUri = it }

    val selectedSkills = remember { mutableStateListOf<String>() }
    val selectedLanguages = remember { mutableStateListOf<String>() }
    val selectedDays = remember { mutableStateListOf<String>() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onRegistrationSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Registration", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo Upload
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    coil.compose.AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                }
            }
            Text("Upload Photo", modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp, color = GreenPrimary)

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Exp (Years)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val skillsList = listOf("Tractor Driver", "Harvester Operator", "Electrician", "Welder", "Irrigation Expert", "Labor", "Plantation Worker")
            val languagesList = listOf("Tamil", "English", "Hindi", "Malayalam", "Kannada")
            val daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            // Skills Selection
            Text("Select Skills", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skillsList.forEach { skill ->
                    FilterChip(
                        selected = selectedSkills.contains(skill),
                        onClick = {
                            if (selectedSkills.contains(skill)) selectedSkills.remove(skill)
                            else selectedSkills.add(skill)
                        },
                        label = { Text(skill) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Languages
            Text("Languages Known", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                languagesList.take(3).forEach { lang ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Checkbox(
                            checked = selectedLanguages.contains(lang),
                            onCheckedChange = {
                                if (selectedLanguages.contains(lang)) selectedLanguages.remove(lang)
                                else selectedLanguages.add(lang)
                            }
                        )
                        Text(lang, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Wage & Available Days
            OutlinedTextField(
                value = dailyWage,
                onValueChange = { dailyWage = it },
                label = { Text("Expected Daily Wage (₹)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Available Days", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                daysList.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day, fontSize = 12.sp)
                        Checkbox(
                            checked = selectedDays.contains(day),
                            onCheckedChange = {
                                if (selectedDays.contains(day)) selectedDays.remove(day)
                                else selectedDays.add(day)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Current Location") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.MyLocation, null, tint = GreenPrimary) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Aadhaar Upload
            Card(
                modifier = Modifier.fillMaxWidth().height(80.dp).clickable { aadhaarLauncher.launch("*/*") },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Badge, null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(if (aadhaarUri == null) "Upload Aadhaar Card" else "Aadhaar Card Selected", fontWeight = FontWeight.Bold)
                        Text("For Profile Verification", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.CloudUpload, null, tint = if (aadhaarUri == null) Color.Gray else GreenPrimary)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = GreenPrimary)
            } else {
                Button(
                    onClick = {
                        viewModel.registerWorker(
                            name, age, experience, selectedSkills.toList(), 
                            selectedLanguages.toList(), dailyWage, selectedDays.toList(), 
                            location, photoUri, aadhaarUri
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotEmpty() && dailyWage.isNotEmpty()
                ) {
                    Text("Register as Worker", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun Modifier.size(size: Int): Modifier = this.size(size.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}

@Preview(showBackground = true)
@Composable
fun WorkerRegistrationPreview() {
    AgriProjectTheme {
        WorkerRegistrationScreen(onBack = {}, onRegistrationSuccess = {})
    }
}
