package com.example.agriproject.presentation.machinery

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.example.agriproject.ui.theme.AgriProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineryDetailScreen(
    machineId: String,
    onBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onBookClick: (String) -> Unit = {}
) {
    // Mock data for the machine
    val machine = remember {
        MachineryDetail(
            id = machineId,
            name = "John Deere 5050D",
            type = "Tractor",
            image = "https://images.unsplash.com/photo-1594488310342-998f451f28b7?auto=format&fit=crop&q=80&w=800",
            ownerName = "Ramesh Kumar",
            ownerRating = 4.8,
            ownerExperience = "12 Years",
            description = "High-performance tractor suitable for all types of farming activities including ploughing, sowing, and harvesting. Well-maintained and recently serviced.",
            hp = "50 HP",
            fuel = "Diesel",
            transmission = "8 Forward + 4 Reverse",
            usage = "850 Hours",
            hourlyPrice = 1500,
            halfDayPrice = 5500,
            fullDayPrice = 10000
        )
    }

    var selectedBookingType by remember { mutableStateOf("Hourly") }
    val totalPrice = when (selectedBookingType) {
        "Hourly" -> machine.hourlyPrice
        "Half Day" -> machine.halfDayPrice
        else -> machine.fullDayPrice
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(100.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Price", color = Color.Gray, fontSize = 12.sp)
                        Text("₹$totalPrice", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                    Button(
                        onClick = { onBookClick("payment/$totalPrice/${machine.name} - $selectedBookingType") },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(56.dp)
                    ) {
                        Text("Book Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Large Image & Top Bar
            Box {
                AsyncImage(
                    model = machine.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                    Row {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(machine.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(machine.type, color = Color.Gray)
                    }
                    Surface(
                        color = Color(0xFFFFF9C4),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
                            Text(" ${machine.ownerRating}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Owner Info Section
                Text("Owner Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color.LightGray) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(machine.ownerName, fontWeight = FontWeight.Bold)
                            Text("Exp: ${machine.ownerExperience}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = { }, modifier = Modifier.background(Color.White, CircleShape)) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = GreenPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { }, modifier = Modifier.background(Color.White, CircleShape)) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = GreenPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = machine.description,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Specifications
                Text("Specifications", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecItem(Icons.Default.ElectricBolt, "Horse Power", machine.hp)
                    SpecItem(Icons.Default.LocalGasStation, "Fuel Type", machine.fuel)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecItem(Icons.Default.Settings, "Transmission", machine.transmission)
                    SpecItem(Icons.Default.Timer, "Total Usage", machine.usage)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location/Map Navigation
                Text("Location", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { onNavigateToMap() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = "https://maps.googleapis.com/maps/api/staticmap?center=11.0168,76.9558&zoom=13&size=600x300&key=YOUR_API_KEY",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Button(
                            onClick = onNavigateToMap,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Navigation", color = Color.Blue, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Booking Selection
                Text("Select Booking Duration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BookingOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "Hourly",
                        price = "₹${machine.hourlyPrice}",
                        isSelected = selectedBookingType == "Hourly",
                        onClick = { selectedBookingType = "Hourly" }
                    )
                    BookingOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "Half Day",
                        price = "₹${machine.halfDayPrice}",
                        isSelected = selectedBookingType == "Half Day",
                        onClick = { selectedBookingType = "Half Day" }
                    )
                    BookingOptionCard(
                        modifier = Modifier.weight(1f),
                        title = "Full Day",
                        price = "₹${machine.fullDayPrice}",
                        isSelected = selectedBookingType == "Full Day",
                        onClick = { selectedBookingType = "Full Day" }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SpecItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .width(170.dp)
            .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookingOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isSelected) GreenPrimary else Color(0xFFEEEEEE)),
        color = if (isSelected) GreenPrimary.copy(alpha = 0.05f) else Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) GreenPrimary else Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSelected) GreenPrimary else Color.Gray)
        }
    }
}

data class MachineryDetail(
    val id: String,
    val name: String,
    val type: String,
    val image: String,
    val ownerName: String,
    val ownerRating: Double,
    val ownerExperience: String,
    val description: String,
    val hp: String,
    val fuel: String,
    val transmission: String,
    val usage: String,
    val hourlyPrice: Int,
    val halfDayPrice: Int,
    val fullDayPrice: Int
)

@Preview(showBackground = true)
@Composable
fun MachineryDetailPreview() {
    AgriProjectTheme {
        MachineryDetailScreen(
            machineId = "1",
            onBack = {},
            onNavigateToMap = {},
            onBookClick = {}
        )
    }
}
