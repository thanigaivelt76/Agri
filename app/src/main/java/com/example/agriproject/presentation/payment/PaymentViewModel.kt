package com.example.agriproject.presentation.payment

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.razorpay.Checkout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.UUID

data class PaymentRecord(
    val id: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val status: String = "PENDING",
    val paymentId: String? = null,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)

data class PaymentState(
    val history: List<PaymentRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastPaymentSuccess: Boolean = false
)

class PaymentViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _state = MutableStateFlow(PaymentState())
    val state = _state.asStateFlow()

    init {
        fetchPaymentHistory()
    }

    fun startPayment(activity: Activity, amount: Double, description: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_YOUR_KEY_HERE") // In production, this would come from a secure place
        
        try {
            val options = JSONObject()
            options.put("name", "Uzhavu Thozhan")
            options.put("description", description)
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#2E7D32")
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt()) // amount in paisa
            
            val prefill = JSONObject()
            prefill.put("email", auth.currentUser?.email ?: "farmer@example.com")
            prefill.put("contact", "9876543210")
            options.put("prefill", prefill)

            checkout.open(activity, options)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Error in starting payment: ${e.message}")
        }
    }

    fun onPaymentSuccess(razorpayPaymentId: String, amount: Double, description: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val record = PaymentRecord(
                    id = UUID.randomUUID().toString(),
                    amount = amount,
                    status = "SUCCESS",
                    paymentId = razorpayPaymentId,
                    description = description,
                    userId = auth.currentUser?.uid ?: "anonymous"
                )
                
                firestore.collection("payments").document(record.id).set(record).await()
                _state.value = _state.value.copy(isLoading = false, lastPaymentSuccess = true)
                fetchPaymentHistory()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun onPaymentError(code: Int, response: String) {
        _state.value = _state.value.copy(error = "Payment Failed ($code): $response")
    }

    private fun fetchPaymentHistory() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("payments")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val history = snapshot?.toObjects(PaymentRecord::class.java) ?: emptyList()
                _state.value = _state.value.copy(history = history)
            }
    }

    fun requestRefund(paymentId: String) {
        viewModelScope.launch {
            // Mocking refund request
            try {
                firestore.collection("payments")
                    .whereEqualTo("paymentId", paymentId)
                    .get().await().documents.firstOrNull()?.reference?.update("status", "REFUND_REQUESTED")?.await()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Refund Error: ${e.localizedMessage}")
            }
        }
    }
}
