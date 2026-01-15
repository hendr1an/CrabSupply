package com.example.crabsupply

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.* import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crabsupply.ui.admin.AddProductScreen
import com.example.crabsupply.ui.auth.LoginScreen
import com.example.crabsupply.ui.auth.RegisterScreen
import com.example.crabsupply.ui.buyer.HomeScreen
import com.example.crabsupply.ui.theme.CrabSupplyTheme
import com.example.crabsupply.viewmodel.AdminViewModel // Import ViewModel Admin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrabSupplyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current

                    // 1. SIAPKAN VIEWMODEL ADMIN DI SINI
                    // Supaya kita bisa pakai fungsi deleteProduct()
                    val adminViewModel: AdminViewModel = viewModel()

                    // 2. PANTAU STATUS HAPUS
                    val deleteStatus by adminViewModel.uploadStatus.collectAsState()

                    // 3. LOGIKA TOAST (Muncul saat hapus berhasil)
                    LaunchedEffect(deleteStatus) {
                        if (deleteStatus == "DELETE_SUCCESS") {
                            Toast.makeText(context, "Produk berhasil dihapus!", Toast.LENGTH_SHORT).show()
                            adminViewModel.resetStatus()
                        }
                    }

                    // State navigasi: "login", "register", "home", atau "add_product"
                    var currentScreen by remember { mutableStateOf("login") }

                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                onLoginSuccess = { currentScreen = "home" },
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
                                onLogoutClick = { currentScreen = "login" },
                                onAddProductClick = { currentScreen = "add_product" },

                                // --- BAGIAN BARU: AKSI TOMBOL EDIT & HAPUS ---
                                onEditClick = { product ->
                                    // Sementara tampilkan Toast dulu (Nanti kita buat layarnya)
                                    Toast.makeText(context, "Edit: ${product.name} (Coming Soon)", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteClick = { product ->
                                    // Panggil fungsi hapus dari ViewModel
                                    adminViewModel.deleteProduct(product.id)
                                }
                                // ---------------------------------------------
                            )
                        }
                        "add_product" -> {
                            AddProductScreen(
                                onBackClick = { currentScreen = "home" }
                            )
                        }
                    }
                }
            }
        }
    }
}