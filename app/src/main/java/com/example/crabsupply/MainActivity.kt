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
import com.example.crabsupply.data.repository.AuthRepository
import com.example.crabsupply.ui.SplashScreen
import com.example.crabsupply.ui.admin.AddProductScreen
import com.example.crabsupply.ui.admin.AdminMainScreen
import com.example.crabsupply.ui.admin.AdminOrderDetailScreen
import com.example.crabsupply.ui.admin.EditProductScreen
import com.example.crabsupply.ui.auth.LoginScreen
import com.example.crabsupply.ui.auth.ProfileScreen
import com.example.crabsupply.ui.auth.RegisterScreen
import com.example.crabsupply.ui.buyer.BuyerOrderScreen
import com.example.crabsupply.ui.buyer.CartScreen
import com.example.crabsupply.ui.buyer.HomeScreen
import com.example.crabsupply.ui.buyer.MapPickerScreen
import com.example.crabsupply.ui.buyer.MassCheckoutScreen // <--- IMPORT BARU
import com.example.crabsupply.ui.buyer.ProductDetailScreen
import com.example.crabsupply.ui.theme.CrabSupplyTheme
import com.example.crabsupply.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrabSupplyTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val adminViewModel: AdminViewModel = viewModel()
                    val deleteStatus by adminViewModel.uploadStatus.collectAsState()
                    val context = LocalContext.current

                    // Notifikasi Hapus
                    LaunchedEffect(deleteStatus) {
                        if (deleteStatus == "DELETE_SUCCESS") {
                            Toast.makeText(context, "Produk dihapus!", Toast.LENGTH_SHORT).show()
                            adminViewModel.resetStatus()
                        }
                    }

                    // --- STATE NAVIGASI ---
                    var currentScreen by remember { mutableStateOf("splash") }
                    var selectedProduct by remember { mutableStateOf<Product?>(null) }

                    // State Peta
                    var selectedLat by remember { mutableStateOf("") }
                    var selectedLong by remember { mutableStateOf("") }

                    // Fungsi Cek Role
                    fun navigateBasedOnRole() {
                        AuthRepository().getUserRole { role ->
                            if (role == "admin") {
                                currentScreen = "admin_main"
                            } else {
                                currentScreen = "home"
                            }
                        }
                    }

                    when (currentScreen) {
                        "splash" -> SplashScreen(
                            onNavigateToHome = { navigateBasedOnRole() },
                            onNavigateToLogin = { currentScreen = "login" }
                        )

                        "login" -> LoginScreen(
                            onLoginSuccess = { navigateBasedOnRole() },
                            onRegisterClick = { currentScreen = "register" }
                        )

                        "register" -> RegisterScreen(
                            onRegisterSuccess = { currentScreen = "login" },
                            onLoginClick = { currentScreen = "login" }
                        )

                        // --- HALAMAN BUYER ---
                        "home" -> HomeScreen(
                            onProfileClick = { currentScreen = "profile" },
                            onCartClick = { currentScreen = "cart" }, // Masuk ke Keranjang
                            onAddProductClick = { },
                            onEditClick = { },
                            onDeleteClick = { },
                            onProductClick = { product ->
                                selectedProduct = product
                                selectedLat = ""
                                selectedLong = ""
                                currentScreen = "detail_product"
                            },
                            onBuyerHistoryClick = { currentScreen = "buyer_orders" }
                        )

                        // --- HALAMAN KERANJANG ---
                        "cart" -> CartScreen(
                            onBackClick = { currentScreen = "home" },
                            onCheckoutClick = {
                                // Reset koordinat agar user memilih lokasi baru khusus checkout massal
                                selectedLat = ""
                                selectedLong = ""
                                currentScreen = "mass_checkout" // Pindah ke Mass Checkout
                            }
                        )

                        // --- HALAMAN CHECKOUT MASSAL (BARU) ---
                        "mass_checkout" -> MassCheckoutScreen(
                            initialLat = selectedLat,
                            initialLong = selectedLong,
                            onBackClick = { currentScreen = "cart" },
                            onOpenMap = { currentScreen = "map_picker_mass" }, // Peta khusus
                            onSuccess = { currentScreen = "home" } // Sukses bayar -> Home
                        )

                        // --- MAP PICKER KHUSUS MASS CHECKOUT ---
                        "map_picker_mass" -> MapPickerScreen(
                            onLocationSelected = { lat, long ->
                                selectedLat = lat.toString()
                                selectedLong = long.toString()
                                currentScreen = "mass_checkout" // Kembali ke Mass Checkout
                            }
                        )

                        // --- HALAMAN UTAMA ADMIN ---
                        "admin_main" -> AdminMainScreen(
                            onNavigateToProfile = { currentScreen = "profile" },
                            onLogOut = {
                                FirebaseAuth.getInstance().signOut()
                                currentScreen = "login"
                            },
                            onAddProduct = { currentScreen = "add_product" },
                            onEditProduct = { product ->
                                selectedProduct = product
                                currentScreen = "edit_product"
                            },
                            onOrderDetailClick = { currentScreen = "admin_order_detail" }
                        )

                        // --- HALAMAN DETAIL ORDER ADMIN ---
                        "admin_order_detail" -> AdminOrderDetailScreen(
                            onBackClick = { currentScreen = "admin_main" }
                        )

                        // --- HALAMAN UMUM ---
                        "add_product" -> AddProductScreen(onBackClick = { currentScreen = "admin_main" })

                        "edit_product" -> selectedProduct?.let {
                            EditProductScreen(productToEdit = it, onBackClick = { currentScreen = "admin_main" })
                        }

                        "detail_product" -> selectedProduct?.let { product ->
                            ProductDetailScreen(
                                product = product,
                                initialLat = selectedLat,
                                initialLong = selectedLong,
                                onBackClick = { currentScreen = "home" },
                                onOpenMap = { currentScreen = "map_picker" }
                            )
                        }

                        "map_picker" -> MapPickerScreen(
                            onLocationSelected = { lat, long ->
                                selectedLat = lat.toString()
                                selectedLong = long.toString()
                                currentScreen = "detail_product"
                            }
                        )

                        "profile" -> ProfileScreen(
                            onBackClick = {
                                navigateBasedOnRole()
                            },
                            onLogoutSuccess = { currentScreen = "login" }
                        )

                        "buyer_orders" -> BuyerOrderScreen(onBackClick = { currentScreen = "home" })
                    }
                }
            }
        }
    }
}