package com.example.crabsupply.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crabsupply.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    onSeeOrdersClick: () -> Unit // Tombol ke daftar pesanan
) {
    val viewModel: AdminViewModel = viewModel()
    val revenue by viewModel.totalRevenue.collectAsState()
    val totalOrders by viewModel.totalOrders.collectAsState()
    val totalProducts by viewModel.totalProducts.collectAsState()

    // Load Data Statistik
    LaunchedEffect(Unit) {
        viewModel.loadDashboardStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Bisnis") },
                navigationIcon = {
                    Button(onClick = onBackClick) { Text("Kembali") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Ringkasan Hari Ini", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // KARTU 1: TOTAL PENDAPATAN
            val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(revenue)
            StatCard(title = "Total Omset (Selesai)", value = formatRp, color = Color(0xFF4CAF50)) // Hijau

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // KARTU 2: TOTAL PESANAN
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(title = "Total Pesanan", value = totalOrders.toString(), color = Color(0xFF2196F3)) // Biru
                }
                Spacer(modifier = Modifier.width(16.dp))
                // KARTU 3: TOTAL PRODUK
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(title = "Stok Produk", value = totalProducts.toString(), color = Color(0xFFFF9800)) // Oranye
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // TOMBOL MENU CEPAT
            Text("Menu Cepat", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSeeOrdersClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("KELOLA PESANAN MASUK")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}