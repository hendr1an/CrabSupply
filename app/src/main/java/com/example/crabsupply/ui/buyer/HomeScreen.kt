package com.example.crabsupply.ui.buyer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crabsupply.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel()
    val productList by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crab Supply - Harga Hari Ini") },
                actions = {
                    TextButton(onClick = {
                        viewModel.logout()
                        onLogoutClick()
                    }) {
                        Text("Keluar", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text("Katalog Kepiting Segar:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(productList) { product ->
                ProductCard(
                    name = product.name,
                    species = product.species,
                    condition = product.condition,
                    size = product.size,
                    price = product.priceRetail, // Kita tampilkan harga Eceran dulu
                    stock = product.stock
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// Kartu Produk yang disesuaikan dengan Model Data Anda
@Composable
fun ProductCard(
    name: String,
    species: String,
    condition: String,
    size: String,
    price: Int,
    stock: Int
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nama Produk
            Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Detail Spesies & Kondisi
            Text(
                text = "$species • $condition • Size $size",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Format Rupiah
                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(price)
                Text(
                    text = formatRp,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Stok
                Text(
                    text = "Stok: $stock kg",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}