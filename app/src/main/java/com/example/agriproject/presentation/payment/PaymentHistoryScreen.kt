package com.example.agriproject.presentation.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agriproject.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (state.history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No payment history found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.history) { record ->
                    PaymentHistoryItem(record, onRefundClick = { viewModel.requestRefund(record.paymentId ?: "") })
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryItem(record: PaymentRecord, onRefundClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(record.description, fontWeight = FontWeight.Bold)
                    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    Text(sdf.format(Date(record.timestamp)), fontSize = 12.sp, color = Color.Gray)
                }
                Text("₹${record.amount}", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when(record.status) {
                        "SUCCESS" -> GreenPrimary.copy(alpha = 0.1f)
                        "REFUND_REQUESTED" -> Color(0xFFFFF3E0)
                        else -> Color.Red.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        record.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when(record.status) {
                            "SUCCESS" -> GreenPrimary
                            "REFUND_REQUESTED" -> Color(0xFFFF9800)
                            else -> Color.Red
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row {
                    TextButton(onClick = { /* Generate Invoice Mock */ }) {
                        Icon(Icons.Default.Receipt, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Invoice", fontSize = 12.sp)
                    }
                    if (record.status == "SUCCESS") {
                        TextButton(onClick = onRefundClick) {
                            Text("Refund", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
