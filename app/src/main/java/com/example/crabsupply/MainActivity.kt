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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrabSupplyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // LOGIKA PINDAH LAYAR SEDERHANA
                    // Default layar adalah "login"
                    var currentScreen by remember { mutableStateOf("login") }

                    if (currentScreen == "login") {
                        // Tampilkan Login
                        LoginScreen(
                            onLoginSuccess = { },
                            onRegisterClick = { currentScreen = "register" }
                        )

                    } else {
                        // Tampilkan Register
                        RegisterScreen(
                            onLoginClick = { currentScreen = "login" }, // Balik ke Login
                            onRegisterSuccess = { currentScreen = "login" } // Sukses daftar, suruh login
                        )
                    }
                }
            }
        }
    }
}