package com.example.agriproject.presentation.marketplace

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
fun CropDetailScreen(
    cropId: String,
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    onChatClick: (String, String) -> Unit
) {
    val crop = remember { mockCrops.find { it.id == cropId } ?: mockCrops[0] }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(100.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Amount", color = Color.Gray, fontSize = 12.sp)
                        Text("₹${crop.price * 500}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                    Button(
                        onClick = { 
                            val totalAmount = crop.price * 500.0
                            onOrderPlaced("payment/$totalAmount/${crop.name}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.6f).height(56.dp)
                    ) {
                        Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Box {
                AsyncImage(
                    model = crop.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(crop.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(color = GreenPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(crop.grade, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Text(crop.category, color = Color.Gray, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailInfoItem(Icons.Default.Inventory, crop.quantity, "Quantity")
                    DetailInfoItem(Icons.Default.Event, crop.harvestDate, "Harvested")
                    DetailInfoItem(Icons.Default.LocationOn, "2.5 km", "Distance")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Farmer Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color.LightGray) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(crop.farmerName, fontWeight = FontWeight.Bold)
                            Text("Verified Farmer", fontSize = 12.sp, color = GreenPrimary)
                        }
                        Row {
                            IconButton(
                                onClick = { onChatClick(crop.farmerName, crop.farmerName) }, // Using name as ID for mock
                                modifier = Modifier.background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.Default.Chat, null, tint = GreenPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { }, modifier = Modifier.background(Color.White, CircleShape)) {
                                Icon(Icons.Default.Call, null, tint = GreenPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Logistics", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Schedule Pickup", fontWeight = FontWeight.Bold)
                            Text("Available for transport booking", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { onOrderPlaced(cropId) }) { Text("Book Truck") }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DetailInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun CropDetailPreview() {
    AgriProjectTheme {
        CropDetailScreen(cropId = "1", onBack = {}, onOrderPlaced = {}, onChatClick = { _, _ -> })
    }
}
