package com.example.crabsupply.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crabsupply.data.model.Order
import com.example.crabsupply.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(
    onBackClick: () -> Unit
) {
    val viewModel: OrderViewModel = viewModel()
    val orderList by viewModel.adminOrders.collectAsState()
    val status by viewModel.orderStatus.collectAsState()
    val context = LocalContext.current

    // Load data saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.loadOrdersForAdmin()
    }

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Pesanan Masuk") },
                navigationIcon = {
                    Button(onClick = onBackClick) { Text("Kembali") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp)
        ) {
            if (orderList.isEmpty()) {
                item { Text("Belum ada pesanan masuk.") }
            }

            items(orderList) { order ->
                OrderCard(
                    order = order,
                    onUpdateStatus = { newStatus ->
                        viewModel.updateStatus(order.id, newStatus)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onUpdateStatus: (String) -> Unit) {
    // Tentukan warna kartu berdasarkan status
    val cardColor = when (order.status) {
        "pending" -> Color(0xFFFFF3E0) // Oranye Muda
        "proses" -> Color(0xFFE3F2FD)  // Biru Muda
        "selesai" -> Color(0xFFE8F5E9) // Hijau Muda
        else -> Color.LightGray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = order.buyerName, fontWeight = FontWeight.Bold)
                Text(text = order.status.uppercase(), fontWeight = FontWeight.Bold)
            }
            Text("Barang: ${order.productName} (${order.quantity} Kg)")

            val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(order.totalPrice)
            Text("Total: $formatRp", color = MaterialTheme.colorScheme.primary)

            Text("Alamat: ${order.address}", fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Aksi Admin
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (order.status == "pending") {
                    Button(onClick = { onUpdateStatus("proses") }) { Text("Proses") }
                }
                if (order.status == "proses") {
                    Button(
                        onClick = { onUpdateStatus("selesai") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Selesai") }
                }
            }
        }
    }
}