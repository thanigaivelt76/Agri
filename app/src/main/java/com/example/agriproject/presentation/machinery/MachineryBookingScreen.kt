package com.example.agriproject.presentation.machinery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agriproject.data.model.Machinery
import com.example.agriproject.data.model.MachineryBooking
import com.example.agriproject.data.repository.MachineryRepository
import com.example.agriproject.ui.theme.GreenPrimary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MachineryBookingViewModel : ViewModel() {
    private val repository = MachineryRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow(MachineryBookingState())
    val state = _state.asStateFlow()

    fun loadMachinery(id: String) {
        viewModelScope.launch {
            val machine = repository.getMachineryById(id)
            _state.value = _state.value.copy(machinery = machine)
        }
    }

    fun confirmBooking(date: String, startTime: String, endTime: String, duration: Int) {
        val machine = _state.value.machinery ?: return
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val booking = MachineryBooking(
                machineId = machine.id,
                ownerId = machine.ownerId,
                farmerId = userId,
                machineName = machine.name,
                bookingDate = date,
                startTime = startTime,
                endTime = endTime,
                totalAmount = machine.price * duration,
                farmLatitude = 11.0168, // Dummy
                farmLongitude = 76.9558 // Dummy
            )
            repository.createBooking(booking).onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}

data class MachineryBookingState(
    val machinery: Machinery? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineryBookingScreen(
    machineId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val viewModel: MachineryBookingViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(machineId) {
        viewModel.loadMachinery(machineId)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    var bookingDate by remember { mutableStateOf("20 Aug 2026") }
    var startTime by remember { mutableStateOf("10:00 AM") }
    var endTime by remember { mutableStateOf("02:00 PM") }
    val duration = 4 // hours

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Booking", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        state.machinery?.let { machine ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("Booking Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))

                BookingSummaryItem("Machine", machine.name)
                BookingSummaryItem("Date", bookingDate)
                BookingSummaryItem("Time", "$startTime - $endTime")
                BookingSummaryItem("Duration", "$duration hours")
                BookingSummaryItem("Price", "₹${machine.price}/${machine.rentalUnit}")

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("₹${machine.price * duration}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = GreenPrimary)
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = GreenPrimary)
                } else {
                    Button(
                        onClick = { viewModel.confirmBooking(bookingDate, startTime, endTime, duration) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Booking", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                
                state.error?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp)) }
            }
        }
    }
}

@Composable
fun BookingSummaryItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
