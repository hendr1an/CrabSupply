package com.example.crabsupply.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.crabsupply.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import com.example.crabsupply.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onAddProductClick: () -> Unit,
    onEditClick: (Product) -> Unit = {},
    onDeleteClick: (Product) -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onAdminDashboardClick: () -> Unit = {},
    onBuyerHistoryClick: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel()

    // --- PERBAIKAN: Refresh Role Setiap Kali Halaman Dibuka ---
    LaunchedEffect(Unit) {
        viewModel.refreshUserRole()
    }
    // ----------------------------------------------------------

    val productList by viewModel.filteredProducts.collectAsState()
    val role by viewModel.userRole.collectAsState()
    val searchText by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf("Semua", "Bakau", "Rajungan", "Telur", "Daging")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog ($role)") },
                navigationIcon = {
                    if (role == "admin") {
                        IconButton(onClick = onAdminDashboardClick) {
                            Icon(Icons.Default.Info, contentDescription = "Dashboard Admin")
                        }
                    } else {
                        IconButton(onClick = onBuyerHistoryClick) {
                            Icon(Icons.Default.DateRange, contentDescription = "Riwayat")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profil")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. PENCARIAN TEKS
            OutlinedTextField(
                value = searchText,
                onValueChange = { viewModel.onSearchTextChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari nama kepiting...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. FILTER CHIPS
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = (selectedCategory == category)
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                    val txtColor = if (isSelected) Color.White else Color.Black

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgColor)
                            .clickable { viewModel.onCategoryChange(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = category, color = txtColor, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. DAFTAR PRODUK
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (productList.isEmpty()) {
                    item { Text("Produk tidak ditemukan.", modifier = Modifier.padding(8.dp)) }
                }
                items(productList) { product ->
                    ProductCard(
                        product = product,
                        isAdmin = (role == "admin"),
                        onEdit = { onEditClick(product) },
                        onDelete = { onDeleteClick(product) },
                        onClick = { onProductClick(product) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: Product, isAdmin: Boolean, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray)
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Img", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    if (isAdmin) {
                        Row {
                            IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary) }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
                Text(text = "${product.species} • ${product.condition}", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(product.priceRetail)
                Text(text = formatRp, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "Stok: ${product.stock} kg", fontSize = 12.sp)
            }
        }
    }
}