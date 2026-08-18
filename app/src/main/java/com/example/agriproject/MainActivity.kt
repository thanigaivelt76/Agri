package com.example.agriproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agriproject.presentation.dashboard.DashboardScreen
import com.example.agriproject.presentation.login.LoginScreen
import com.example.agriproject.presentation.machinery.MachineryMapScreen
import com.example.agriproject.presentation.machinery.MachineryDetailScreen
import com.example.agriproject.presentation.machinery.AddMachineryScreen
import com.example.agriproject.presentation.machinery.MachineryBookingScreen
import com.example.agriproject.presentation.owner.OwnerDashboardScreen
import com.example.agriproject.presentation.workers.WorkersScreen
import com.example.agriproject.presentation.workers.WorkerDetailScreen
import com.example.agriproject.presentation.workers.WorkerRegistrationScreen
import com.example.agriproject.presentation.marketplace.MarketplaceScreen
import com.example.agriproject.presentation.marketplace.SellCropScreen
import com.example.agriproject.presentation.marketplace.CropDetailScreen
import com.example.agriproject.presentation.profile.ProfileScreen
import com.example.agriproject.presentation.ai.AiAssistantScreen
import com.example.agriproject.presentation.voice.VoiceAssistantScreen
import com.example.agriproject.presentation.signup.SignUpScreen
import com.example.agriproject.presentation.splash.SplashScreen
import com.example.agriproject.presentation.tracking.OrderTrackingScreen
import com.example.agriproject.presentation.chat.ChatScreen
import com.example.agriproject.presentation.payment.PaymentScreen
import com.example.agriproject.presentation.payment.PaymentHistoryScreen
import com.example.agriproject.presentation.payment.PaymentViewModel
import com.example.agriproject.presentation.admin.AdminDashboardScreen
import com.example.agriproject.presentation.weather.WeatherScreen
import com.example.agriproject.notifications.NotificationScreen
import com.example.agriproject.notifications.NotificationViewModel
import com.example.agriproject.ui.theme.AgriProjectTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.razorpay.PaymentResultListener
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity(), PaymentResultListener {
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var notificationViewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        paymentViewModel = ViewModelProvider(this)[PaymentViewModel::class.java]
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setContent {
            AgriProjectTheme {
                UzhavuThozhanApp(paymentViewModel, notificationViewModel)
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        // Here we need the amount and description from the state or pass it through
        // For simplicity, we can fetch the last requested payment details if stored
        paymentViewModel.onPaymentSuccess(razorpayPaymentId ?: "", 0.0, "Payment Success")
    }

    override fun onPaymentError(code: Int, response: String?) {
        paymentViewModel.onPaymentError(code, response ?: "Unknown Error")
    }
}

@Composable
fun UzhavuThozhanApp(
    paymentViewModel: PaymentViewModel,
    notificationViewModel: NotificationViewModel
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    
    // Google Sign-In Setup
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("171884000943-9sqddu36j8kk1cqt750lrr4ol2vnm5o9.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        } catch (e: ApiException) {
            // Handle error
        }
    }
    
    // Safety check for FirebaseAuth
    val currentUser = try {
        auth.currentUser
    } catch (e: Exception) {
        null
    }
    val isLoggedIn = currentUser != null

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigationNext = {
                    val destination = if (isLoggedIn) "dashboard" else "login"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate("signup")
                },
                onGoogleLoginClick = {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onSignUpComplete = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onMachineryClick = { navController.navigate("machinery") },
                onWorkersClick = { navController.navigate("workers") },
                onMarketplaceClick = { navController.navigate("marketplace") },
                onProfileClick = { navController.navigate("profile") },
                onAiAssistantClick = { navController.navigate("ai_assistant") },
                onVoiceAssistantClick = { navController.navigate("voice_assistant") },
                onNotificationsClick = { navController.navigate("notifications") },
                onAdminClick = { navController.navigate("admin_dashboard") },
                onWeatherClick = { navController.navigate("weather") },
                notificationViewModel = notificationViewModel
            )
        }
        composable("machinery") {
            MachineryMapScreen(
                onBack = { navController.popBackStack() },
                onSeeDetails = { id -> navController.navigate("machinery_detail/$id") },
                onAddMachinery = { navController.navigate("add_machinery") }
            )
        }
        composable("add_machinery") {
            AddMachineryScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        composable("machinery_detail/{machineId}") { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            MachineryDetailScreen(
                machineId = machineId,
                onBack = { navController.popBackStack() },
                onNavigateToMap = { navController.navigate("machinery") },
                onBookClick = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable("machinery_booking/{machineId}") { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            MachineryBookingScreen(
                machineId = machineId,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        composable("workers") {
            WorkersScreen(
                onBack = { navController.popBackStack() },
                onWorkerClick = { id -> navController.navigate("worker_detail/$id") }
            )
        }
        composable("worker_detail/{workerId}") { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
            WorkerDetailScreen(
                workerId = workerId,
                onBack = { navController.popBackStack() },
                onChatClick = { userId, userName ->
                    navController.navigate("chat/$userId/$userName")
                },
                onBookClick = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable("marketplace") {
            MarketplaceScreen(
                onBack = { navController.popBackStack() },
                onCropClick = { id -> navController.navigate("crop_detail/$id") },
                onSellClick = { navController.navigate("sell_crop") }
            )
        }
        composable("crop_detail/{cropId}") { backStackEntry ->
            val cropId = backStackEntry.arguments?.getString("cropId") ?: ""
            CropDetailScreen(
                cropId = cropId,
                onBack = { navController.popBackStack() },
                onOrderPlaced = { route ->
                    navController.navigate(route)
                },
                onChatClick = { userId, userName ->
                    navController.navigate("chat/$userId/$userName")
                }
            )
        }
        composable("sell_crop") {
            SellCropScreen(
                onBack = { navController.popBackStack() },
                onUploadSuccess = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSwitchToOwner = { navController.navigate("owner_dashboard") },
                onRegisterAsWorker = { navController.navigate("worker_registration") },
                onPaymentHistoryClick = { navController.navigate("payment_history") },
                onAdminClick = { navController.navigate("admin_dashboard") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        composable("worker_registration") {
            WorkerRegistrationScreen(
                onBack = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.popBackStack()
                    // Show success message or navigate to worker dashboard
                }
            )
        }
        composable("ai_assistant") {
            AiAssistantScreen(onBack = { navController.popBackStack() })
        }
        composable("voice_assistant") {
            VoiceAssistantScreen(onBack = { navController.popBackStack() })
        }
        composable("order_tracking/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("chat/{userId}/{userName}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            ChatScreen(
                otherUserId = userId,
                otherUserName = userName,
                onBack = { navController.popBackStack() }
            )
        }
        composable("owner_dashboard") {
            OwnerDashboardScreen(
                onBack = { navController.popBackStack() },
                onAddMachine = { navController.navigate("add_machinery") },
                onEditMachine = { id -> /* Navigate to Edit Machine */ }
            )
        }
        composable("payment/{amount}/{description}") { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            val description = backStackEntry.arguments?.getString("description") ?: "Payment"
            PaymentScreen(
                amount = amount,
                description = description,
                onBack = { navController.popBackStack() },
                onPaymentSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                viewModel = paymentViewModel
            )
        }
        composable("payment_history") {
            PaymentHistoryScreen(
                onBack = { navController.popBackStack() },
                viewModel = paymentViewModel
            )
        }
        composable("notifications") {
            NotificationScreen(
                onBack = { navController.popBackStack() },
                viewModel = notificationViewModel
            )
        }
        composable("admin_dashboard") {
            AdminDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("weather") {
            WeatherScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
