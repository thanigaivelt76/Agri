package com.example.agriproject.presentation.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
fun OwnerDashboardScreen(
    onBack: () -> Unit,
    onAddMachine: () -> Unit,
    onEditMachine: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Machines", "Bookings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Owner Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = onAddMachine,
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Machine")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = GreenPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GreenPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab()
                1 -> MachinesTab(onEditMachine)
                2 -> BookingsTab()
            }
        }
    }
}

@Composable
fun OverviewTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Total Earnings", value = "₹45,200", icon = Icons.Default.AccountBalanceWallet, color = Color(0xFFE8F5E9))
                StatCard(modifier = Modifier.weight(1f), title = "Total Bookings", value = "28", icon = Icons.Default.Event, color = Color(0xFFE3F2FD))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Active Machines", value = "4", icon = Icons.Default.Agriculture, color = Color(0xFFFFF3E0))
                StatCard(modifier = Modifier.weight(1f), title = "Pending Req.", value = "3", icon = Icons.Default.PendingActions, color = Color(0xFFFFEBEE))
            }
        }
        item {
            Text("Revenue Graph", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Revenue Trends (Bar Chart Placeholder)", color = Color.Gray)
                }
            }
        }
        item {
            Text("Upcoming Bookings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(upcomingBookings) { booking ->
            BookingRequestItem(booking)
        }
    }
}

@Composable
fun MachinesTab(onEditMachine: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(myMachines) { machine ->
            OwnerMachineCard(machine, onEditMachine)
        }
    }
}

@Composable
fun BookingsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(bookingHistory) { booking ->
            HistoryBookingItem(booking)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OwnerMachineCard(machine: OwnerMachine, onEditMachine: (String) -> Unit) {
    var isAvailable by remember { mutableStateOf(machine.isAvailable) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = machine.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { onEditMachine(machine.id) }, modifier = Modifier.background(Color.White, CircleShape).size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { }, modifier = Modifier.background(Color.White, CircleShape).size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(machine.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("₹${machine.price}/hr", color = GreenPrimary, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isAvailable) "Available" else "Busy", fontSize = 12.sp, color = if (isAvailable) GreenPrimary else Color.Red)
                    Switch(checked = isAvailable, onCheckedChange = { isAvailable = it }, colors = SwitchDefaults.colors(checkedThumbColor = GreenPrimary))
                }
            }
        }
    }
}

@Composable
fun BookingRequestItem(booking: OwnerBooking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.LightGray) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.farmerName, fontWeight = FontWeight.Bold)
                Text("${booking.machineName} • ${booking.time}", fontSize = 12.sp, color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { }, modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { }, modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryBookingItem(booking: OwnerBooking) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5)) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.padding(10.dp), tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(booking.machineName, fontWeight = FontWeight.Bold)
            Text(booking.date, fontSize = 12.sp, color = Color.Gray)
        }
        Text("₹${booking.totalEarnings}", fontWeight = FontWeight.Bold, color = GreenPrimary)
    }
}

data class OwnerMachine(
    val id: String,
    val name: String,
    val price: Int,
    val image: String,
    val isAvailable: Boolean
)

data class OwnerBooking(
    val id: String,
    val farmerName: String,
    val machineName: String,
    val time: String = "",
    val date: String = "",
    val totalEarnings: Int = 0,
    val status: String
)

val myMachines = listOf(
    OwnerMachine("1", "John Deere 5050D", 1500, "https://images.unsplash.com/photo-1594488310342-998f451f28b7", true),
    OwnerMachine("2", "Kubota Harvester", 3200, "https://images.unsplash.com/photo-1592991538534-00972b6f59ab", false)
)

val upcomingBookings = listOf(
    OwnerBooking("1", "Anil Kumar", "John Deere 5050D", "Today, 10 AM", status = "Pending"),
    OwnerBooking("2", "Suresh P.", "Rotavator 7ft", "Tomorrow, 8 AM", status = "Pending")
)

val bookingHistory = listOf(
    OwnerBooking("101", "Vijay S.", "John Deere 5050D", date = "12 Oct 2023", totalEarnings = 4500, status = "Completed"),
    OwnerBooking("102", "Manoj K.", "Harvester", date = "10 Oct 2023", totalEarnings = 9600, status = "Completed"),
    OwnerBooking("103", "Ravi Teja", "Tractor", date = "08 Oct 2023", totalEarnings = 3000, status = "Completed")
)

@Preview(showBackground = true)
@Composable
fun OwnerDashboardPreview() {
    AgriProjectTheme {
        OwnerDashboardScreen(onBack = {}, onAddMachine = {}, onEditMachine = {})
    }
}
