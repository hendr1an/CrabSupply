package com.example.crabsupply.ui.buyer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
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

    // State Bukti Bayar
    var paymentUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher Gambar (Bukti Bayar)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> paymentUri = uri }

    // Hitung harga awal saat halaman dibuka
    LaunchedEffect(Unit) {
        viewModel.calculatePrice("1", product)
    }

    // Pantau perubahan Qty -> Hitung Ulang Harga Barang
    LaunchedEffect(qty) {
        viewModel.calculatePrice(qty, product)
    }

    // Pantau Lat/Long -> Hitung Ongkir jika lokasi sudah dipilih
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
            Text(text = "${product.species} • ${product.condition} • Size ${product.size}")
            Spacer(modifier = Modifier.height(8.dp))

            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl, contentDescription = null,
                    modifier = Modifier.height(150.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 2. INPUT KUANTITAS & HARGA BERTINGKAT
            Text("Jumlah Pesanan (Kg)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = qty,
                onValueChange = { if (it.all { char -> char.isDigit() }) qty = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (isWholesale) {
                Text(
                    text = "🎉 SELAMAT! Anda mendapatkan Harga Grosir (≥10kg)",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Info: Beli min. 10kg untuk harga grosir lebih murah!",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 3. LOKASI PENGIRIMAN & ONGKIR (Baru)
            Text("Alamat Pengiriman", fontWeight = FontWeight.Bold)

            // TOMBOL BUKA PETA (OSM)
            Button(
                onClick = onOpenMap,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                if (lat.isNotEmpty()) Text("Lokasi Terpilih ($distance km)")
                else Text("Pilih Titik Pengantaran di Peta")
            }

            // Koordinat Read-Only
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = lat, onValueChange = {},
                    label = { Text("Lat") }, modifier = Modifier.weight(1f),
                    readOnly = true, enabled = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = long, onValueChange = {},
                    label = { Text("Long") }, modifier = Modifier.weight(1f),
                    readOnly = true, enabled = false
                )
            }

            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Detail Alamat (Jalan, No Rumah)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // --- RINCIAN BIAYA ---
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

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 4. PEMBAYARAN & BUKTI TRANSFER
            Text("Pembayaran", fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Bank BCA: 123-456-7890 (A.n CrabSupply)", fontWeight = FontWeight.Bold)
                    Text("Silakan transfer sesuai total dan upload bukti di bawah.")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (paymentUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(paymentUri),
                        contentDescription = "Bukti Bayar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Text("Klik untuk Upload Bukti Transfer")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.submitOrder(
                        product, qty, address, lat, long,
                        hasPaymentProof = (paymentUri != null)
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("KONFIRMASI PESANAN")
            }
        }
    }
}