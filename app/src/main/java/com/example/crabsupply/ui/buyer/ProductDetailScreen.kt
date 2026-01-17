package com.example.crabsupply.ui.buyer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.crabsupply.R // Pastikan import R ada untuk akses gambar
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    initialLat: String,
    initialLong: String,
    onBackClick: () -> Unit,
    onOpenMap: () -> Unit
) {
    val viewModel: OrderViewModel = viewModel()
    val orderStatus by viewModel.orderStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val calculatedPrice by viewModel.calculatedPrice.collectAsState()
    val isWholesale by viewModel.isWholesale.collectAsState()
    val shippingCost by viewModel.shippingCost.collectAsState()
    val distance by viewModel.distanceKm.collectAsState()
    val finalTotal by viewModel.finalTotal.collectAsState()
    val context = LocalContext.current

    // State Input
    var qty by remember { mutableStateOf("1") }
    var address by remember { mutableStateOf("") }

    // Gunakan initialLat/Long jika ada
    var lat by remember { mutableStateOf(initialLat) }
    var long by remember { mutableStateOf(initialLong) }

    // State Bukti Bayar (Opsional untuk sekarang)
    var paymentUri by remember { mutableStateOf<Uri?>(null) }

    // State Metode Pembayaran
    var selectedPayment by remember { mutableStateOf("Tunai") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> paymentUri = uri }

    // Hitung harga awal saat halaman dibuka
    LaunchedEffect(Unit) {
        viewModel.calculatePrice("1", product)
    }

    // Pantau perubahan Qty -> Hitung Ulang Harga
    LaunchedEffect(qty) {
        viewModel.calculatePrice(qty, product)
    }

    // Pantau Lokasi -> Hitung Ongkir
    LaunchedEffect(initialLat, initialLong) {
        if (initialLat.isNotEmpty() && initialLong.isNotEmpty()) {
            val l = initialLat.toDoubleOrNull() ?: 0.0
            val lo = initialLong.toDoubleOrNull() ?: 0.0
            viewModel.calculateShipping(l, lo)
        }
    }

    // Pantau Status Order
    LaunchedEffect(orderStatus) {
        if (orderStatus == "SUCCESS") {
            Toast.makeText(context, "Pesanan Berhasil Dibuat!", Toast.LENGTH_LONG).show()
            viewModel.resetStatus()
            onBackClick() // Kembali ke Home
        } else if (orderStatus != null) {
            Toast.makeText(context, orderStatus, Toast.LENGTH_SHORT).show()
            viewModel.resetStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Pesanan") },
                navigationIcon = { Button(onClick = onBackClick) { Text("Batal") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. INFO PRODUK
            Text(text = product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            if (product.category == "Kepiting") {
                Text(text = "${product.species} • ${product.condition} • Size ${product.size}")
            } else {
                Text(text = "Fresh Seafood", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl, contentDescription = null,
                    modifier = Modifier.height(150.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 2. INPUT KUANTITAS
            Text("Jumlah Pesanan (Kg)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = qty,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                        qty = input
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contoh: 0.5 atau 1.5") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (isWholesale) {
                Text(
                    text = "🎉 Harga Grosir (≥10kg) Aktif!",
                    color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Info: Beli min. 10kg untuk harga grosir.",
                    color = Color.Gray, fontSize = 12.sp
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 3. ALAMAT & PETA
            Text("Alamat Pengiriman", fontWeight = FontWeight.Bold)
            Button(
                onClick = onOpenMap,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                if (lat.isNotEmpty()) Text("Lokasi Terpilih ($distance km)")
                else Text("Pilih Titik di Peta")
            }

            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Detail Alamat (Jalan, No Rumah)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // 4. RINCIAN BIAYA
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Biaya", fontWeight = FontWeight.Bold)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Harga Barang ($qty kg)")
                        Text(NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(calculatedPrice))
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Ongkir (${String.format("%.1f", distance)} km)")
                        Text(NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(shippingCost))
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("TOTAL BAYAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(finalTotal),
                            fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 5. METODE PEMBAYARAN (FITUR BARU)
            Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Pilihan 1: Tunai
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedPayment = "Tunai" }) {
                    RadioButton(selected = selectedPayment == "Tunai", onClick = { selectedPayment = "Tunai" })
                    Text("Tunai (COD / Bayar di Tempat)")
                }

                // Pilihan 2: Non-Tunai
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedPayment = "Non-Tunai" }) {
                    RadioButton(selected = selectedPayment == "Non-Tunai", onClick = { selectedPayment = "Non-Tunai" })
                    Text("Non-Tunai (Transfer / QRIS)")
                }

                // TAMPILAN KHUSUS NON-TUNAI
                if (selectedPayment == "Non-Tunai") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(8.dp).border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Transfer Bank", fontWeight = FontWeight.Bold)
                            Text("BCA: 3820079107", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("A/N: Ayyun Izzati", fontSize = 14.sp)

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Scan QRIS", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            // GAMBAR QRIS DARI DRAWABLE
                            Image(
                                painter = painterResource(id = R.drawable.qris_code),
                                contentDescription = "QRIS Code",
                                modifier = Modifier.size(250.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Silakan upload bukti bayar setelah pemesanan.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TOMBOL BELI
            Button(
                onClick = {
                    viewModel.submitOrder(
                        product, qty, address, lat, long,
                        hasPaymentProof = true, // Bypass validasi bukti untuk demo
                        paymentMethod = selectedPayment // Kirim Metode ke ViewModel
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("BUAT PESANAN")
            }
        }
    }
}