package com.example.agriproject.presentation.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agriproject.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onBack: () -> Unit,
    viewModel: WeatherViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Forecast", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            } else if (state.weather != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Current Weather Main Card
                    item {
                        CurrentWeatherCard(state.weather!!.current)
                    }

                    // Alerts
                    if (!state.weather!!.alerts.isNullOrEmpty()) {
                        items(state.weather!!.alerts!!) { alert ->
                            WeatherAlertCard(alert)
                        }
                    }

                    // Weather Details Grid
                    item {
                        WeatherDetailsGrid(state.weather!!.current)
                    }

                    // 7-Day Forecast
                    item {
                        Text("7-Day Forecast", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.weather!!.daily) { daily ->
                                DailyForecastItem(daily)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(current: CurrentWeather) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = "https://openweathermap.org/img/wn/${current.weather[0].icon}@4x.png",
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Text(
            "${current.temp.toInt()}°C",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            current.weather[0].description.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun WeatherAlertCard(alert: WeatherAlert) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(alert.event, fontWeight = FontWeight.Bold, color = Color.Red)
                Text(alert.description, fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun WeatherDetailsGrid(current: CurrentWeather) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        WeatherDetailItem(Modifier.weight(1f), Icons.Default.WaterDrop, "Humidity", "${current.humidity}%")
        WeatherDetailItem(Modifier.weight(1f), Icons.Default.Air, "Wind", "${current.wind_speed} km/h")
        WeatherDetailItem(Modifier.weight(1f), Icons.Default.WbSunny, "UV Index", current.uvi.toString())
    }
}

@Composable
fun WeatherDetailItem(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun DailyForecastItem(daily: DailyWeather) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sdf = SimpleDateFormat("EEE", Locale.getDefault())
            Text(sdf.format(Date(daily.dt * 1000)), fontWeight = FontWeight.Bold, color = Color.White)
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${daily.weather[0].icon}@2x.png",
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text("${daily.temp.max.toInt()}°", fontWeight = FontWeight.Bold, color = Color.White)
            Text("${daily.temp.min.toInt()}°", color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, null, tint = Color.Cyan, modifier = Modifier.size(10.dp))
                Text("${(daily.pop * 100).toInt()}%", fontSize = 10.sp, color = Color.White)
            }
        }
    }
}
