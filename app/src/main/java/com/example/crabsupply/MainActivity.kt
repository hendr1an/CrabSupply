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
import com.example.crabsupply.ui.SplashScreen
import com.example.crabsupply.ui.admin.AddProductScreen
import com.example.crabsupply.ui.admin.AdminDashboardScreen
import com.example.crabsupply.ui.admin.AdminOrderScreen
import com.example.crabsupply.ui.admin.EditProductScreen
import com.example.crabsupply.ui.auth.LoginScreen
import com.example.crabsupply.ui.auth.ProfileScreen
import com.example.crabsupply.ui.auth.RegisterScreen
import com.example.crabsupply.ui.buyer.BuyerOrderScreen
import com.example.crabsupply.ui.buyer.HomeScreen
import com.example.crabsupply.ui.buyer.MapPickerScreen // <--- IMPORT BARU (PETA)
import com.example.crabsupply.ui.buyer.ProductDetailScreen
import com.example.crabsupply.ui.theme.CrabSupplyTheme
import com.example.crabsupply.viewmodel.AdminViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrabSupplyTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val adminViewModel: AdminViewModel = viewModel()
                    val deleteStatus by adminViewModel.uploadStatus.collectAsState()
                    val context = LocalContext.current

                    LaunchedEffect(deleteStatus) {
                        if (deleteStatus == "DELETE_SUCCESS") {
                            Toast.makeText(context, "Produk dihapus!", Toast.LENGTH_SHORT).show()
                            adminViewModel.resetStatus()
                        }
                    }

                    // --- STATE NAVIGASI ---
                    var currentScreen by remember { mutableStateOf("splash") }
                    var selectedProduct by remember { mutableStateOf<Product?>(null) }

                    // --- STATE UNTUK MENYIMPAN HASIL PETA (PENTING!) ---
                    // Agar saat kembali dari Peta, datanya tidak hilang
                    var selectedLat by remember { mutableStateOf("") }
                    var selectedLong by remember { mutableStateOf("") }

                    when (currentScreen) {
                        "splash" -> SplashScreen(
                            onNavigateToHome = { currentScreen = "home" },
                            onNavigateToLogin = { currentScreen = "login" }
                        )

                        "login" -> LoginScreen(onLoginSuccess = { currentScreen = "home" }, onRegisterClick = { currentScreen = "register" })
                        "register" -> RegisterScreen(onRegisterSuccess = { currentScreen = "login" }, onLoginClick = { currentScreen = "login" })

                        "home" -> HomeScreen(
                            onProfileClick = { currentScreen = "profile" },
                            onAddProductClick = { currentScreen = "add_product" },
                            onEditClick = { product -> selectedProduct = product; currentScreen = "edit_product" },
                            onDeleteClick = { product -> adminViewModel.deleteProduct(product.id) },

                            // SAAT PRODUK DIKLIK, RESET LOKASI DULU (BIAR BERSIH)
                            onProductClick = { product ->
                                selectedProduct = product
                                selectedLat = ""  // Reset lokasi lama
                                selectedLong = "" // Reset lokasi lama
                                currentScreen = "detail_product"
                            },

                            onAdminDashboardClick = { currentScreen = "admin_dashboard" },
                            onBuyerHistoryClick = { currentScreen = "buyer_orders" }
                        )

                        "add_product" -> AddProductScreen(onBackClick = { currentScreen = "home" })
                        "edit_product" -> selectedProduct?.let { EditProductScreen(productToEdit = it, onBackClick = { currentScreen = "home" }) }

                        // --- UPDATE DETAIL PRODUCT ---
                        "detail_product" -> selectedProduct?.let { product ->
                            ProductDetailScreen(
                                product = product,
                                initialLat = selectedLat,   // Kirim data Lat yang disimpan
                                initialLong = selectedLong, // Kirim data Long yang disimpan
                                onBackClick = { currentScreen = "home" },
                                onOpenMap = { currentScreen = "map_picker" } // Buka Peta
                            )
                        }

                        // --- RUTE BARU: PETA (MAP PICKER) ---
                        "map_picker" -> MapPickerScreen(
                            onLocationSelected = { lat, long ->
                                // Saat user klik "Pilih Lokasi Ini":
                                selectedLat = lat.toString()
                                selectedLong = long.toString()
                                // Kembali ke halaman checkout
                                currentScreen = "detail_product"
                            }
                        )

                        "profile" -> ProfileScreen(onBackClick = { currentScreen = "home" }, onLogoutSuccess = { currentScreen = "login" })
                        "buyer_orders" -> BuyerOrderScreen(onBackClick = { currentScreen = "home" })

                        "admin_dashboard" -> AdminDashboardScreen(
                            onBackClick = { currentScreen = "home" },
                            onSeeOrdersClick = { currentScreen = "admin_orders" }
                        )
                        "admin_orders" -> AdminOrderScreen(onBackClick = { currentScreen = "admin_dashboard" })
                    }
                }
            }
        }
    }
}