package com.example.agriproject.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agriproject.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail (Sidebar)
        NavigationRail(
            containerColor = GreenPrimary,
            contentColor = Color.White,
            header = {
                Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(40.dp).padding(vertical = 16.dp))
            }
        ) {
            val categories = listOf("Dashboard", "Farmers", "Machinery", "Workers", "Payments", "Complaints")
            val icons = listOf(Icons.Default.Dashboard, Icons.Default.Person, Icons.Default.Agriculture, Icons.Default.Groups, Icons.Default.Payment, Icons.Default.Warning)
            
            categories.forEachIndexed { index, category ->
                NavigationRailItem(
                    selected = state.selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    icon = { Icon(icons[index], null) },
                    label = { Text(category, fontSize = 10.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = GreenPrimary,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Logout, null)
            }
        }

        // Main Content
        Column(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF5F5F5))) {
            TopAppBar(
                title = { Text("Admin Dashboard - ${state.selectedCategory}", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.exportReport("PDF") }) {
                        Icon(Icons.Default.Download, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
            
            Box(modifier = Modifier.padding(24.dp)) {
                when (state.selectedCategory) {
                    "Dashboard" -> DashboardOverview(state.stats)
                    else -> ManagementList(state.selectedCategory, state.searchQuery) { viewModel.updateSearchQuery(it) }
                }
            }
        }
    }
}

@Composable
fun DashboardOverview(stats: AdminStats) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Total Revenue", "₹${stats.totalRevenue}", Icons.Default.Payments, GreenPrimary)
                StatCard(Modifier.weight(1f), "Total Users", stats.totalUsers.toString(), Icons.Default.People, Color(0xFF1976D2))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Active Bookings", stats.activeBookings.toString(), Icons.Default.EventAvailable, Color(0xFFFF9800))
                StatCard(Modifier.weight(1f), "Complaints", stats.pendingComplaints.toString(), Icons.Default.Report, Color.Red)
            }
        }
        item {
            Text("Revenue Growth", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                // Mock Graph
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Monthly Revenue Graph (Mock)", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Icon(icon, null, modifier = Modifier.padding(12.dp), tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 12.sp)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun ManagementList(category: String, query: String, onQueryChange: (String) -> Unit) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search $category...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(10) { index ->
                ManagementItem(category, index)
            }
        }
    }
}

@Composable
fun ManagementItem(category: String, index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.LightGray) {
                Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$category Item #$index", fontWeight = FontWeight.Bold)
                Text("ID: AGR-${1000 + index} • Status: Active", fontSize = 12.sp, color = Color.Gray)
            }
            Row {
                IconButton(onClick = { }) { Icon(Icons.Default.Edit, null, tint = GreenPrimary) }
                IconButton(onClick = { }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}
