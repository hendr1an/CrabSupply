package com.example.crabsupply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.* // Import penting untuk State
import androidx.compose.ui.Modifier
import com.example.crabsupply.ui.auth.LoginScreen
import com.example.crabsupply.ui.auth.RegisterScreen
import com.example.crabsupply.ui.theme.CrabSupplyTheme // Sesuaikan jika nama theme beda
import com.example.crabsupply.ui.buyer.HomeScreen // Import Home

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrabSupplyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State navigasi: "login", "register", atau "home"
                    var currentScreen by remember { mutableStateOf("login") }

                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                onLoginSuccess = { currentScreen = "home" }, // Pindah ke Home
                                onRegisterClick = { currentScreen = "register" }
                            )
                        }
                        "register" -> {
                            RegisterScreen(
                                onRegisterSuccess = { currentScreen = "login" },
                                onLoginClick = { currentScreen = "login" }
                            )
                        }
                        "home" -> {
                            HomeScreen(
                                onLogoutClick = { currentScreen = "login" } // Tombol logout balik ke login
                            )
                        }
                    }
                }
            }
        }
    }
}