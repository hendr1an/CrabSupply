package com.example.crabsupply.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crabsupply.data.model.Order
import com.example.crabsupply.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerOrderScreen(
    onBackClick: () -> Unit
) {
    val viewModel: OrderViewModel = viewModel()
    val orders by viewModel.buyerOrders.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrdersForBuyer()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Riwayat Pesanan Saya") }, navigationIcon = { Button(onClick = onBackClick) { Text("Kembali") } })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (orders.isEmpty()) {
                item { Text("Belum ada riwayat pesanan.") }
            }
            items(orders) { order ->
                BuyerOrderCard(order)
            }
        }
    }
}

@Composable
fun BuyerOrderCard(order: Order) {
    var isExpanded by remember { mutableStateOf(false) } // State untuk Expand/Collapse

    // LOGIKA ESTIMASI WAKTU
    // Asumsi: Kita ambil jarak dari string address (ini agak tricky karena kita simpan string gabungan).
    // Idealnya distance disimpan di field terpisah di Order.
    // TAPI, kita bisa ekstrak dari string "Alamat (Jarak: 3.5 km)"

    val distance = extractDistanceFromAddress(order.address)
    val prepTime = 15 // Menit persiapan
    val travelTimePerKm = 3 // Menit per KM
    val totalEstMinutes = prepTime + (distance * travelTimePerKm).toInt()

    // Waktu Sampai = Waktu Proses + Estimasi Durasi
    val arrivalTimeMillis = order.dateProcessed + (totalEstMinutes * 60 * 1000L)

    // Hitung Sisa Waktu (Countdown statis)
    val now = System.currentTimeMillis()
    val minutesLeft = if (arrivalTimeMillis > now) ((arrivalTimeMillis - now) / 60000).toInt() else 0

    val statusColor = when (order.status) {
        "pending" -> Color.Gray
        "proses" -> Color(0xFF2196F3) // Biru
        "selesai" -> Color(0xFF4CAF50) // Hijau
        else -> Color.Black
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // HEADER (SELALU MUNCUL)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = order.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = order.status.uppercase(), color = statusColor, fontWeight = FontWeight.Bold)
            }
            Text("Total: ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(order.totalPrice)}")
            Text("Metode: ${order.paymentMethod}", fontSize = 12.sp, color = Color.Gray)

            // BAGIAN DETAIL (MUNCUL JIKA DIKLIK)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Text("Detail Pengiriman", fontWeight = FontWeight.Bold)
                Text("Alamat: ${order.address}")

                // --- LOGIKA TAMPILAN ESTIMASI ---
                if (order.status == "proses") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📦 Pesanan Sedang Diantar!", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            Spacer(modifier = Modifier.height(4.dp))

                            if (minutesLeft > 0) {
                                Text("Estimasi Tiba: $minutesLeft Menit lagi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                val arrivalDate = Date(arrivalTimeMillis)
                                val formatJam = SimpleDateFormat("HH:mm", Locale.getDefault())
                                Text("(Sekitar pukul ${formatJam.format(arrivalDate)})", fontSize = 12.sp)
                            } else {
                                Text("Kurir sudah dekat / sampai lokasi.", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (order.status == "pending") {
                    Text("Menunggu konfirmasi Admin...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                } else if (order.status == "selesai") {
                    Text("✅ Pesanan Telah Selesai", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }

            // Icon Panah Bawah/Atas
            Icon(
                imageVector = if(isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                tint = Color.LightGray
            )
        }
    }
}

// Fungsi bantu ekstrak jarak dari string "Jalan Mawar (Jarak: 3.2 km)"
fun extractDistanceFromAddress(address: String): Double {
    return try {
        val parts = address.split("Jarak: ")
        if (parts.size > 1) {
            val kmPart = parts[1].split(" ")[0] // Ambil angka "3.2"
            kmPart.replace(",", ".").toDouble()
        } else {
            5.0 // Default jarak jika gagal parsing
        }
    } catch (e: Exception) {
        5.0
    }
}