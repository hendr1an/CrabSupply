package com.example.crabsupply.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items // Import untuk LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Import untuk LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.ui.theme.AccentOrange
import com.example.crabsupply.ui.theme.BackgroundGray
import com.example.crabsupply.ui.theme.PrimaryTeal
import com.example.crabsupply.ui.theme.TextGray
import com.example.crabsupply.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onEditClick: (Product) -> Unit = {},
    onDeleteClick: (Product) -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onBuyerHistoryClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    isAdminMode: Boolean = false
) {
    val viewModel: HomeViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.refreshUserRole() }

    val productList by viewModel.filteredProducts.collectAsState()
    val searchText by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf("Semua", "Bakau", "Rajungan", "Telur", "Daging")

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            // CUSTOM TOP BAR (FIXED MATERIAL 3)
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(bottom = 8.dp)
                    .shadow(elevation = 4.dp)
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if(isAdminMode) "Admin Panel" else "Crab Supply",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = PrimaryTeal,
                        actionIconContentColor = PrimaryTeal
                    ),
                    navigationIcon = {
                        if (isAdminMode) {
                            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color.Black) }
                        } else {
                            IconButton(onClick = {}) { Icon(Icons.Default.Home, null, tint = PrimaryTeal) }
                        }
                    },
                    actions = {
                        if (!isAdminMode) {
                            IconButton(onClick = onBuyerHistoryClick) { Icon(Icons.Default.DateRange, null, tint = Color.Gray) }
                            IconButton(onClick = onCartClick) {
                                Icon(Icons.Default.ShoppingCart, null, tint = PrimaryTeal)
                            }
                            IconButton(onClick = onProfileClick) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                )

                // SEARCH BAR KAPSUL (FIXED COLORS)
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.onSearchTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    placeholder = { Text("Cari kepiting segar...", fontSize = 14.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BackgroundGray,
                        unfocusedContainerColor = BackgroundGray,
                        disabledContainerColor = BackgroundGray,
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            if (isAdminMode) {
                FloatingActionButton(
                    onClick = onAddProductClick,
                    containerColor = PrimaryTeal,
                    contentColor = Color.White
                ) { Text("+", fontSize = 24.sp) }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // FILTER KATEGORI (FIXED LAZY ROW ITEMS)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.background(BackgroundGray)
            ) {
                items(categories) { category ->
                    val isSelected = (selectedCategory == category)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategoryChange(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryTeal,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if(isSelected) PrimaryTeal else Color.Transparent,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // GRID PRODUK (2 KOLOM)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.background(BackgroundGray)
            ) {
                // BANNER PROMO
                item(span = { GridItemSpan(2) }) {
                    if (!isAdminMode && searchText.isEmpty() && selectedCategory == "Semua") {
                        BannerCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (productList.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Produk tidak ditemukan 🦀", color = Color.Gray)
                        }
                    }
                }

                items(productList) { product ->
                    ProductGridCard(
                        product = product,
                        isAdmin = isAdminMode,
                        onEdit = { onEditClick(product) },
                        onDelete = { onDeleteClick(product) },
                        onClick = {
                            if (isAdminMode) onEditClick(product) else onProductClick(product)
                        }
                    )
                }
            }
        }
    }
}

// --- WIDGET BANNER CANTIK ---
@Composable
fun BannerCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().height(140.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryTeal, Color(0xFF26A69A))
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Promo Hari Ini!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Diskon Spesial Grosir", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryTeal),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Cek Sekarang", fontSize = 12.sp)
                    }
                }
                Text("🦀", fontSize = 48.sp)
            }
        }
    }
}

// --- KARTU PRODUK MODERN (GRID STYLE) ---
@Composable
fun ProductGridCard(
    product: Product,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // GAMBAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.LightGray)
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Image", fontSize = 10.sp, color = Color.White)
                    }
                }

                if (product.stock <= 0) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                        Text("HABIS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // INFO
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (product.category == "Kepiting") {
                    Text(
                        text = "${product.species}",
                        fontSize = 10.sp,
                        color = TextGray,
                        modifier = Modifier.background(BackgroundGray, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(product.priceRetail)
                val cleanRp = formatRp.replace(",00", "")

                Text(
                    text = cleanRp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentOrange
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TextGray, modifier = Modifier.size(10.dp))
                    Text(" Stok: ${product.stock} kg", fontSize = 10.sp, color = TextGray)
                }

                if (isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, null, tint = PrimaryTeal)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}