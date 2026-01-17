package com.example.crabsupply.ui.buyer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.crabsupply.R
import com.example.crabsupply.viewmodel.CartViewModel
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MassCheckoutScreen(
    initialLat: String,
    initialLong: String,
    onBackClick: () -> Unit,
    onOpenMap: () -> Unit,
    onSuccess: () -> Unit
) {
    val viewModel: CartViewModel = viewModel() // Menggunakan CartViewModel yg sama
    val cartItems by viewModel.cartItems.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val shippingCost by viewModel.shippingCost.collectAsState()
    val distance by viewModel.distanceKm.collectAsState()
    val status by viewModel.cartStatus.collectAsState()

    val context = LocalContext.current

    // State Input
    var address by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(initialLat) }
    var long by remember { mutableStateOf(initialLong) }

    // State Pembayaran
    var paymentUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPayment by remember { mutableStateOf("Tunai") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> paymentUri = uri }

    // Hitung Ongkir saat lokasi berubah
    LaunchedEffect(initialLat, initialLong) {
        if (initialLat.isNotEmpty() && initialLong.isNotEmpty()) {
            val l = initialLat.toDoubleOrNull() ?: 0.0
            val lo = initialLong.toDoubleOrNull() ?: 0.0
            viewModel.calculateShipping(l, lo)
        }
    }

    // Pantau Status Sukses
    LaunchedEffect(status) {
        if (status == "CHECKOUT_SUCCESS") {
            Toast.makeText(context, "Semua pesanan berhasil dibuat!", Toast.LENGTH_LONG).show()
            viewModel.resetStatus()
            onSuccess() // Kembali ke Home/Cart
        } else if (status != null && status != "LOADING") {
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            viewModel.resetStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Massal") },
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
            // 1. RINGKASAN BARANG
            Text("Ringkasan Belanja (${cartItems.size} Item)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.productName} (${item.quantity} kg)", fontSize = 14.sp)
                            Text(
                                NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.totalPriceItem),
                                fontSize = 14.sp, fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }

            // 2. ALAMAT & PETA
            Text("Alamat Pengiriman", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            Button(
                onClick = onOpenMap,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                if (lat.isNotEmpty()) Text("Lokasi Terpilih ($distance km)") else Text("Pilih Titik di Peta")
            }
            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Detail Alamat") },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )

            // 3. TOTAL BIAYA
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Ongkir (${String.format("%.1f", distance)} km)")
                        Text(NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(shippingCost))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("GRAND TOTAL", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(grandTotal),
                            fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 4. METODE PEMBAYARAN
            Text("Metode Pembayaran", fontWeight = FontWeight.Bold)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedPayment = "Tunai" }) {
                    RadioButton(selected = selectedPayment == "Tunai", onClick = { selectedPayment = "Tunai" })
                    Text("Tunai")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedPayment = "Non-Tunai" }) {
                    RadioButton(selected = selectedPayment == "Non-Tunai", onClick = { selectedPayment = "Non-Tunai" })
                    Text("Non-Tunai (Transfer/QRIS)")
                }
            }

            // Info Rekening (Simplified)
            if (selectedPayment == "Non-Tunai") {
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("BCA: 3820079107 (Ayyun Izzati)", fontWeight = FontWeight.Bold)
                        Text("Upload bukti di bawah:")
                    }
                }

                // Upload Box
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray).clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (paymentUri != null) {
                        Image(painter = rememberAsyncImagePainter(paymentUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text("Upload Bukti")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TOMBOL BAYAR
            Button(
                onClick = {
                    var finalBase64 = ""
                    if (paymentUri != null) {
                        try {
                            // Gunakan fungsi compress yang sama
                            finalBase64 = compressUriToBase64(context, paymentUri!!)
                        } catch (e: Exception) {}
                    }

                    viewModel.processMassCheckout(address, selectedPayment, finalBase64)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = status != "LOADING"
            ) {
                if (status == "LOADING") CircularProgressIndicator(color = Color.White)
                else Text("BAYAR SEMUA")
            }
        }
    }
}