package com.example.agriproject.presentation.dashboard

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.example.agriproject.ui.theme.AgriProjectTheme
import com.example.agriproject.notifications.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String = "Farmer",
    onMachineryClick: () -> Unit = {},
    onWorkersClick: () -> Unit = {},
    onMarketplaceClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSellCropsClick: () -> Unit = {},
    onAiAssistantClick: () -> Unit = {},
    onVoiceAssistantClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    notificationViewModel: NotificationViewModel? = null
) {
    val notificationState by notificationViewModel?.state?.collectAsState() ?: remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Welcome Farmer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                            Text("Coimbatore, Tamil Nadu", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (notificationState != null && notificationState!!.unreadCount > 0) {
                                Badge { Text(notificationState!!.unreadCount.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null)
                        }
                    }
                    IconButton(onClick = onProfileClick) {
                        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color.LightGray) {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Agriculture, null) },
                    label = { Text("Machinery") },
                    selected = false,
                    onClick = onMachineryClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Groups, null) },
                    label = { Text("Workers") },
                    selected = false,
                    onClick = onWorkersClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Storefront, null) },
                    label = { Text("Market") },
                    selected = false,
                    onClick = onMarketplaceClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Weather Widget
            item {
                WeatherWidget(onClick = onWeatherClick)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Today's Bookings
            item {
                SectionHeader("Today's Bookings", onViewAll = {})
                BookingCard()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Nearby Machinery (Airbnb Style)
            item {
                SectionHeader("Nearby Machinery", onViewAll = onMachineryClick)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(nearbyMachinery) { machinery ->
                        AirbnbCard(
                            imageRes = machinery.image,
                            title = machinery.name,
                            subtitle = machinery.location,
                            price = "₹${machinery.price}/hr",
                            rating = machinery.rating
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // AI Assistant & Sell Crops
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = "AI Crop Assistant",
                        subtitle = "Check Crop Health",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFFE8F5E9),
                        tint = GreenPrimary,
                        onClick = onAiAssistantClick
                    )
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Voice Assistant",
                        subtitle = "Ask Anything",
                        icon = Icons.Default.GraphicEq,
                        color = Color(0xFFE3F2FD),
                        tint = Color(0xFF1976D2),
                        onClick = onVoiceAssistantClick
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                ActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Sell Crops",
                    subtitle = "Best Market Price",
                    icon = Icons.Default.AddBusiness,
                    color = Color(0xFFFFF3E0),
                    tint = Color(0xFFFF9800),
                    onClick = onSellCropsClick
                )
                Spacer(modifier = Modifier.height(12.dp))
                ActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Admin Dashboard",
                    subtitle = "Manage Users, Machinery & Reports",
                    icon = Icons.Default.AdminPanelSettings,
                    color = Color(0xFFF3E5F5),
                    tint = Color(0xFF9C27B0),
                    onClick = onAdminClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Machinery Owner Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onProfileClick() },
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Agriculture, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Own a Machine?", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Start earning by renting it out", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Nearby Workers (Airbnb Style)
            item {
                SectionHeader("Nearby Workers", onViewAll = onWorkersClick)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(nearbyWorkers) { worker ->
                        AirbnbCard(
                            imageRes = worker.image,
                            title = worker.name,
                            subtitle = worker.expertise,
                            price = "₹${worker.price}/day",
                            rating = worker.rating
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Orders
            item {
                SectionHeader("Recent Orders", onViewAll = {})
                OrderCard(onClick = { onOrderClick("12345") })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun WeatherWidget(onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("28°C", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Mostly Sunny", color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    WeatherInfo(Icons.Default.WaterDrop, "65%")
                    Spacer(modifier = Modifier.width(12.dp))
                    WeatherInfo(Icons.Default.Air, "12 km/h")
                }
            }
            Icon(
                Icons.Default.WbSunny,
                contentDescription = null,
                tint = Color(0xFFFFD600),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun WeatherInfo(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onViewAll) {
            Text("See all", color = GreenPrimary)
        }
    }
}

@Composable
fun AirbnbCard(
    imageRes: String,
    title: String,
    subtitle: String,
    price: String,
    rating: Double
) {
    Column(modifier = Modifier.width(200.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
        ) {
            AsyncImage(
                model = imageRes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
            Text(" $rating", fontSize = 12.sp)
        }
        Text(subtitle, color = Color.Gray, fontSize = 14.sp)
        Text(price, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun BookingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(8.dp), color = GreenPrimary.copy(alpha = 0.1f)) {
                Icon(Icons.Default.Agriculture, contentDescription = null, tint = GreenPrimary, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("John Deere Tractor", fontWeight = FontWeight.Bold)
                Text("Today, 10:00 AM - 02:00 PM", fontSize = 12.sp, color = Color.Gray)
            }
            StatusBadge("Confirmed", GreenPrimary)
        }
    }
}

@Composable
fun OrderCard(onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFE3F2FD)) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Organic Fertilizer", fontWeight = FontWeight.Bold)
                Text("Order #12345 • In Transit", fontSize = 12.sp, color = Color.Gray)
            }
            Text("₹2,450", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    AgriProjectTheme {
        DashboardScreen(
            onMachineryClick = {},
            onWorkersClick = {},
            onMarketplaceClick = {},
            onProfileClick = {},
            onSellCropsClick = {},
            onAiAssistantClick = {},
            onOrderClick = {},
            onNotificationsClick = {},
            onAdminClick = {},
            onWeatherClick = {},
            notificationViewModel = null
        )
    }
}

data class DashboardItem(
    val name: String,
    val location: String,
    val expertise: String = "",
    val price: Int,
    val rating: Double,
    val image: String
)

val nearbyMachinery = listOf(
    DashboardItem("John Deere 5050D", "2.5 km away", "", 1500, 4.8, "https://images.unsplash.com/photo-1594488310342-998f451f28b7?auto=format&fit=crop&q=80&w=400"),
    DashboardItem("Kubota Harvester", "5.1 km away", "", 3200, 4.9, "https://images.unsplash.com/photo-1592991538534-00972b6f59ab?auto=format&fit=crop&q=80&w=400"),
    DashboardItem("Rotavator 7ft", "1.2 km away", "", 800, 4.6, "https://images.unsplash.com/photo-1570586111516-e58f00095817?auto=format&fit=crop&q=80&w=400")
)

val nearbyWorkers = listOf(
    DashboardItem("Karthik Raja", "Coimbatore", "Ploughing Spec.", 500, 4.7, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=400"),
    DashboardItem("Arjun Kumar", "Pollachi", "Harvesting", 650, 4.8, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=400"),
    DashboardItem("Selvam P.", "Tirupur", "General Labor", 450, 4.5, "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=400")
)
