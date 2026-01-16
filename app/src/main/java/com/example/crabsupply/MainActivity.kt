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
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.ui.admin.AddProductScreen
import com.example.crabsupply.ui.admin.EditProductScreen
import com.example.crabsupply.ui.auth.LoginScreen
import com.example.crabsupply.ui.auth.RegisterScreen
import com.example.crabsupply.ui.buyer.HomeScreen
import com.example.crabsupply.ui.buyer.ProductDetailScreen // <--- IMPORT BARU
import com.example.crabsupply.ui.theme.CrabSupplyTheme
import com.example.crabsupply.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrabSupplyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // SETUP ADMIN VIEWMODEL
                    val adminViewModel: AdminViewModel = viewModel()
                    val deleteStatus by adminViewModel.uploadStatus.collectAsState()
                    val context = LocalContext.current

                    // Notifikasi Hapus
                    LaunchedEffect(deleteStatus) {
                        if (deleteStatus == "DELETE_SUCCESS") {
                            Toast.makeText(context, "Produk berhasil dihapus!", Toast.LENGTH_SHORT).show()
                            adminViewModel.resetStatus()
                        }
                    }

                    // AUTO LOGIN
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val startDestination = if (currentUser != null) "home" else "login"

                    // Navigasi & State
                    var currentScreen by remember { mutableStateOf(startDestination) }
                    var selectedProduct by remember { mutableStateOf<Product?>(null) } // Menyimpan produk yang dipilih

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

                                // Aksi Edit (Admin)
                                onEditClick = { product ->
                                    selectedProduct = product
                                    currentScreen = "edit_product"
                                },
                                // Aksi Hapus (Admin)
                                onDeleteClick = { product ->
                                    adminViewModel.deleteProduct(product.id)
                                },

                                // Aksi Klik Produk (Buyer) -> Ke Detail Pesanan
                                onProductClick = { product ->
                                    selectedProduct = product // Simpan data produknya
                                    currentScreen = "detail_product" // Pindah layar
                                }
                            )
                        }
                        "add_product" -> {
                            AddProductScreen(
                                onBackClick = { currentScreen = "home" }
                            )
                        }
                        "edit_product" -> {
                            selectedProduct?.let { product ->
                                EditProductScreen(
                                    productToEdit = product,
                                    onBackClick = { currentScreen = "home" }
                                )
                            }
                        }
                        // HALAMAN DETAIL PESANAN (BARU)
                        "detail_product" -> {
                            selectedProduct?.let { product ->
                                ProductDetailScreen(
                                    product = product,
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