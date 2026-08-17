package com.example.agriproject.presentation.machinery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agriproject.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineryScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Machinery Near You") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search machinery (Tractor, Harvester...)") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryTab("All", true)
                CategoryTab("Tractor", false)
                CategoryTab("Harvester", false)
                CategoryTab("Rotavator", false)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(machineryList) { item ->
                    MachineryCard(item)
                }
            }
        }
    }
}

@Composable
fun CategoryTab(label: String, isSelected: Boolean) {
    Surface(
        color = if (isSelected) GreenPrimary else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) null else AssistChipDefaults.assistChipBorder(enabled = true),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MachineryCard(item: MachineryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Placeholder for Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Agriculture, null, tint = GreenPrimary, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text(item.rating.toString(), fontSize = 12.sp)
                }
                Text("${item.type} • ${item.power}", color = Color.Gray, fontSize = 12.sp)
                Text("₹${item.price} /hour", color = GreenPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                
                Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(item.owner, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(item.location, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                }
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("View Details", color = GreenPrimary, fontSize = 10.sp)
            }
        }
    }
}

data class MachineryItem(
    val name: String,
    val type: String,
    val power: String,
    val rating: Double,
    val price: Int,
    val owner: String,
    val location: String
)

val machineryList = listOf(
    MachineryItem("John Deere 5050D", "Tractor", "50 HP", 4.6, 1500, "Ramesh Farms", "Coimbatore"),
    MachineryItem("Kubota DC 70G", "Harvester", "70 HP", 4.7, 3500, "Sri Venkatesh Agro", "Coimbatore"),
    MachineryItem("New Holland 3630", "Tractor", "60 HP", 4.5, 1600, "Kumar Farms", "Coimbatore")
)
