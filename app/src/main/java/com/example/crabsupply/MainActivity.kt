package com.example.crabsupply

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crabsupply.data.model.Product // Import Product
import com.example.crabsupply.ui.admin.AddProductScreen
import com.example.crabsupply.ui.admin.EditProductScreen // Import Edit Screen
import com.example.crabsupply.ui.auth.LoginScreen
import com.example.crabsupply.ui.auth.RegisterScreen
import com.example.crabsupply.ui.buyer.HomeScreen
import com.example.crabsupply.ui.theme.CrabSupplyTheme
import com.example.crabsupply.viewmodel.AdminViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrabSupplyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val adminViewModel: AdminViewModel = viewModel()
                    val deleteStatus by adminViewModel.uploadStatus.collectAsState()
                    val context = LocalContext.current

                    LaunchedEffect(deleteStatus) {
                        if (deleteStatus == "DELETE_SUCCESS") {
                            Toast.makeText(context, "Produk berhasil dihapus!", Toast.LENGTH_SHORT).show()
                            adminViewModel.resetStatus()
                        }
                    }

                    // Variabel Navigasi & Data Sementara
                    var currentScreen by remember { mutableStateOf("login") }
                    var selectedProduct by remember { mutableStateOf<Product?>(null) } // Wadah data edit

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
                                // AKSI EDIT: Simpan data, lalu pindah layar
                                onEditClick = { product ->
                                    selectedProduct = product
                                    currentScreen = "edit_product"
                                },
                                onDeleteClick = { product ->
                                    adminViewModel.deleteProduct(product.id)
                                }
                            )
                        }
                        "add_product" -> {
                            AddProductScreen(
                                onBackClick = { currentScreen = "home" }
                            )
                        }
                        // HALAMAN EDIT
                        "edit_product" -> {
                            // Pastikan ada data yang diedit
                            selectedProduct?.let { product ->
                                EditProductScreen(
                                    productToEdit = product,
                                    onBackClick = { currentScreen = "home" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}