package com.example.agriproject.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSwitchToOwner: () -> Unit,
    onRegisterAsWorker: () -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onAdminClick: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfileImage(it) }
    }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    if (state.user.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = state.user.profileImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(20.dp))
                    }
                }
                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .size(32.dp)
                        .background(GreenPrimary, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(state.user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(state.user.email, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // Modes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModeCard(
                    modifier = Modifier.weight(1f),
                    title = "Machine Owner",
                    icon = Icons.Default.Agriculture,
                    color = GreenPrimary,
                    onClick = onSwitchToOwner
                )
                ModeCard(
                    modifier = Modifier.weight(1f),
                    title = "Skilled Worker",
                    icon = Icons.Default.Groups,
                    color = Color(0xFF1976D2),
                    onClick = onRegisterAsWorker
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Sections
            SettingsSection("Personal Information") {
                ProfileMenuItem(Icons.Outlined.Person, "Edit Profile", onClick = { showEditDialog = true })
                ProfileMenuItem(Icons.Outlined.LocationOn, "My Address") {
                    Text(state.user.location.ifEmpty { "Not Set" }, color = Color.Gray, fontSize = 12.sp)
                }
                ProfileMenuItem(Icons.Outlined.Agriculture, "Farm Details") {
                    Text(state.user.farmSize.ifEmpty { "Add Farm Info" }, color = Color.Gray, fontSize = 12.sp)
                }
            }

            SettingsSection("Preferences") {
                ProfileMenuItem(Icons.Outlined.Translate, "Language") {
                    Text(state.user.language, color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                ProfileMenuItem(Icons.Outlined.DarkMode, "Dark Mode") {
                    Switch(
                        checked = state.user.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GreenPrimary)
                    )
                }
            }

            SettingsSection("Finance & History") {
                ProfileMenuItem(Icons.Outlined.Payment, "Payment History", onClick = onPaymentHistoryClick)
                ProfileMenuItem(Icons.Outlined.History, "My Bookings")
            }

            SettingsSection("Admin Tools") {
                ProfileMenuItem(Icons.Outlined.AdminPanelSettings, "Admin Dashboard", onClick = onAdminClick)
            }

            SettingsSection("Account Action") {
                ProfileMenuItem(Icons.Outlined.Logout, "Logout", textColor = Color.Red, onClick = { viewModel.logout() })
                ProfileMenuItem(Icons.Outlined.DeleteForever, "Delete Account", textColor = Color.Red, onClick = { showDeleteDialog = true })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            user = state.user,
            onDismiss = { showEditDialog = false },
            onSave = { name, loc, size, types ->
                viewModel.updateProfile(name, loc, size, types)
                showEditDialog = false
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?") },
            text = { Text("This action is permanent and cannot be undone. All your data will be lost.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAccount() }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ModeCard(modifier: Modifier, title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF9F9F9))
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (textColor == Color.Red) Color.Red else Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        if (trailing != null) {
            trailing()
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var location by remember { mutableStateOf(user.location) }
    var farmSize by remember { mutableStateOf(user.farmSize) }
    var cropTypes by remember { mutableStateOf(user.cropTypes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                OutlinedTextField(value = farmSize, onValueChange = { farmSize = it }, label = { Text("Farm Size") })
                OutlinedTextField(value = cropTypes, onValueChange = { cropTypes = it }, label = { Text("Main Crops") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, location, farmSize, cropTypes) }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
