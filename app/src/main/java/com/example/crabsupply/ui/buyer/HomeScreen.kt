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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import com.example.crabsupply.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit = {},
    onAddProductClick: () -> Unit,
    onEditClick: (Product) -> Unit = {},
    onDeleteClick: (Product) -> Unit = {},
    // PARAMETER BARU: Aksi saat kartu diklik
    onProductClick: (Product) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel()

    val productList by viewModel.products.collectAsState()
    val role by viewModel.userRole.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Kepiting ($role)") },
                actions = {
                    TextButton(onClick = {
                        viewModel.logout()
                        onLogoutClick()
                    }) {
                        Text("Keluar", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            if (role == "admin") {
                FloatingActionButton(
                    onClick = onAddProductClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(productList) { product ->
                ProductCard(
                    product = product,
                    isAdmin = (role == "admin"),
                    onEdit = { onEditClick(product) },
                    onDelete = { onDeleteClick(product) },
                    // SAMBUNGKAN AKSI KLIK DI SINI
                    onClick = { onProductClick(product) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Tambahkan anotasi ini karena Card(onClick) eksperimental di beberapa versi
@Composable
fun ProductCard(
    product: Product,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit // PARAMETER BARU DI KARTU
) {
    Card(
        onClick = onClick, // PASANG AKSI KLIK PADA KARTU
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nama Produk
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Tombol Edit/Hapus (Khusus Admin)
                if (isAdmin) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Text(
                text = "${product.species} • ${product.condition} • Size ${product.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(product.priceRetail)
                Text(text = formatRp, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "Stok: ${product.stock} kg", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}