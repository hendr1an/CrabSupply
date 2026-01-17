package com.example.crabsupply.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.crabsupply.data.model.CartItem
import com.example.crabsupply.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit // Nanti kita buat logika checkout massal
) {
    val viewModel: CartViewModel = viewModel()
    val cartItems by viewModel.cartItems.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val status by viewModel.cartStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Belanja") },
                navigationIcon = { Button(onClick = onBackClick) { Text("Kembali") } }
            )
        },
        bottomBar = {
            // BAGIAN BAWAH (TOTAL & CHECKOUT)
            if (cartItems.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Belanja:", fontWeight = FontWeight.Bold)
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(grandTotal),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("CHECKOUT (${cartItems.size} Barang)")
                        }
                    }
                }
            }
        }
    ) { padding ->
        // LIST BARANG
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Keranjang masih kosong.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item = item, onDelete = { viewModel.removeItem(item.id) })
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gambar Kecil
            Card(modifier = Modifier.size(70.dp)) {
                if (item.productImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.productImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info Produk
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${item.quantity} kg", fontSize = 14.sp, color = Color.Gray)

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.totalPriceItem)
                Text(formatRp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            // Tombol Hapus
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
            }
        }
    }
}