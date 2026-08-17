package com.example.agriproject.presentation.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import com.example.agriproject.ui.theme.AgriProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onBack: () -> Unit,
    onCropClick: (String) -> Unit,
    onSellClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Vegetables", "Fruits", "Grains", "Pulses", "Spices")

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("Crop Marketplace", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                        }
                    }
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSellClick,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Sell Crop") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Categories
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

            // Crop Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val filteredCrops = if (selectedCategory == "All") mockCrops else mockCrops.filter { it.category == selectedCategory }
                items(filteredCrops) { crop ->
                    CropCard(crop, onClick = { onCropClick(crop.id) })
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search crops, farmers...") },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        trailingIcon = { Icon(Icons.Default.Tune, null, tint = GreenPrimary) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = GreenPrimary
        )
    )
}

@Composable
fun CropCard(crop: Crop, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = crop.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = crop.grade,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(crop.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Quantity: ${crop.quantity}", color = Color.Gray, fontSize = 12.sp)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("₹${crop.price}/kg", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(32.dp).background(GreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Add, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

data class Crop(
    val id: String,
    val name: String,
    val category: String,
    val quantity: String,
    val price: Int,
    val grade: String,
    val image: String,
    val farmerName: String,
    val harvestDate: String
)

val mockCrops = listOf(
    Crop("1", "Organic Tomatoes", "Vegetables", "500 kg", 30, "Grade A", "https://images.unsplash.com/photo-1546473427-e140d4d1c3e2", "Suresh Farms", "20 Oct 2023"),
    Crop("2", "Sona Masuri Rice", "Grains", "1000 kg", 55, "Grade A+", "https://images.unsplash.com/photo-1586201375761-83865001e31c", "Ravi Kumar", "15 Oct 2023"),
    Crop("3", "Red Onions", "Vegetables", "800 kg", 25, "Grade B", "https://images.unsplash.com/photo-1508747703725-719777637510", "Sri Lakshmi Agro", "18 Oct 2023"),
    Crop("4", "Alphonso Mangoes", "Fruits", "200 kg", 150, "Grade A", "https://images.unsplash.com/photo-1553279768-865429fa0078", "Ratnagiri Orchards", "22 Oct 2023"),
    Crop("5", "Turmeric Finger", "Spices", "100 kg", 120, "Grade A", "https://images.unsplash.com/photo-1615485290382-441e4d049cb5", "Erode Organic", "25 Oct 2023")
)

@Preview(showBackground = true)
@Composable
fun MarketplacePreview() {
    AgriProjectTheme {
        MarketplaceScreen(onBack = {}, onCropClick = {}, onSellClick = {})
    }
}
