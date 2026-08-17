package com.example.agriproject.presentation.workers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.example.agriproject.ui.theme.AgriProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(
    onBack: () -> Unit,
    onWorkerClick: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Tractor Driver", "Harvester Operator", "Electrician", "Welder", "Irrigation Expert", "Labor", "Plantation Worker")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skilled Workers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Category Filter
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val filteredWorkers = if (selectedCategory == "All") allWorkers else allWorkers.filter { it.category == selectedCategory }
                items(filteredWorkers) { worker ->
                    WorkerCard(worker, onClick = { onWorkerClick(worker.id) })
                }
            }
        }
    }
}

@Composable
fun WorkerCard(worker: Worker, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = worker.photo,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (worker.isAvailable) {
                        Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                            Box(modifier = Modifier.size(8.dp).background(GreenPrimary, CircleShape))
                        }
                    }
                }
                Text(worker.category, color = Color.Gray, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text(" ${worker.rating} • ${worker.experience} exp", fontSize = 12.sp, color = Color.Gray)
                }
                Text("₹${worker.dailyWage}/day", color = GreenPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${worker.distance} km", fontSize = 12.sp, color = Color.Gray)
                Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    modifier = Modifier.padding(top = 8.dp).height(32.dp)
                ) {
                    Text("Book", fontSize = 12.sp)
                }
            }
        }
    }
}

data class Worker(
    val id: String,
    val name: String,
    val category: String,
    val experience: String,
    val dailyWage: Int,
    val distance: Double,
    val rating: Double,
    val photo: String,
    val isAvailable: Boolean = true
)

val allWorkers = listOf(
    Worker("1", "Karthik Raja", "Tractor Driver", "8 Years", 800, 2.5, 4.8, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"),
    Worker("2", "Arjun Kumar", "Harvester Operator", "5 Years", 1200, 5.1, 4.9, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e"),
    Worker("3", "Selvam P.", "Labor", "10 Years", 500, 1.2, 4.6, "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e"),
    Worker("4", "Mani G.", "Electrician", "6 Years", 700, 3.8, 4.7, "https://images.unsplash.com/photo-1599566150163-29194dcaad36"),
    Worker("5", "Prakash J.", "Welder", "12 Years", 900, 4.2, 4.8, "https://images.unsplash.com/photo-1542909168-82c3e7fdca5c")
)

@Preview(showBackground = true)
@Composable
fun WorkersScreenPreview() {
    AgriProjectTheme {
        WorkersScreen(onBack = {}, onWorkerClick = {})
    }
}
