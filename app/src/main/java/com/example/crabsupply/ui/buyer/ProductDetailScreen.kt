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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart // Import Icon Keranjang
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.crabsupply.R
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.viewmodel.CartViewModel // Import CartViewModel
import com.example.crabsupply.viewmodel.OrderViewModel
import java.io.ByteArrayOutputStream
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
    // ViewModel Order (Beli Langsung)
    val viewModel: OrderViewModel = viewModel()
    val orderStatus by viewModel.orderStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val calculatedPrice by viewModel.calculatedPrice.collectAsState()
    val isWholesale by viewModel.isWholesale.collectAsState()
    val shippingCost by viewModel.shippingCost.collectAsState()
    val distance by viewModel.distanceKm.collectAsState()
    val finalTotal by viewModel.finalTotal.collectAsState()

    // ViewModel Cart (Keranjang) - BARU
    val cartViewModel: CartViewModel = viewModel()
    val cartStatus by cartViewModel.cartStatus.collectAsState()

    val context = LocalContext.current

    // State Input
    var qty by remember { mutableStateOf("1") }
    var address by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(initialLat) }
    var long by remember { mutableStateOf(initialLong) }

    // State Pembayaran
    var paymentUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPayment by remember { mutableStateOf("Tunai") }

    // State Pop-up QRIS
    var showQrisDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> paymentUri = uri }

    LaunchedEffect(Unit) { viewModel.calculatePrice("1", product) }
    LaunchedEffect(qty) { viewModel.calculatePrice(qty, product) }
    LaunchedEffect(initialLat, initialLong) {
        if (initialLat.isNotEmpty() && initialLong.isNotEmpty()) {
            val l = initialLat.toDoubleOrNull() ?: 0.0
            val lo = initialLong.toDoubleOrNull() ?: 0.0
            viewModel.calculateShipping(l, lo)
        }
    }

    // Pantau Status Order (Beli Langsung)
    LaunchedEffect(orderStatus) {
        if (orderStatus == "SUCCESS") {
            Toast.makeText(context, "Pesanan Berhasil Dibuat!", Toast.LENGTH_LONG).show()
            viewModel.resetStatus()
            onBackClick()
        } else if (orderStatus != null) {
            Toast.makeText(context, orderStatus, Toast.LENGTH_SHORT).show()
            viewModel.resetStatus()
        }
    }

    // Pantau Status Cart (Keranjang) - BARU
    LaunchedEffect(cartStatus) {
        if (cartStatus != null) {
            Toast.makeText(context, cartStatus, Toast.LENGTH_SHORT).show()
            cartViewModel.resetStatus()
        }
    }

    // --- DIALOG ZOOM QRIS ---
    if (showQrisDialog) {
        Dialog(onDismissRequest = { showQrisDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(500.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.qris_code),
                        contentDescription = "QRIS Full",
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { showQrisDialog = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = { Button(onClick = onBackClick) { Text("Kembali") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // INFO PRODUK
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

            // INPUT QTY
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
                Text("🎉 Harga Grosir (≥10kg) Aktif!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text("Info: Beli min. 10kg untuk harga grosir.", color = Color.Gray, fontSize = 12.sp)
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // ALAMAT
            Text("Alamat Pengiriman", fontWeight = FontWeight.Bold)
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
                label = { Text("Detail Alamat (Jalan, No Rumah)") },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )

            // RINCIAN BIAYA
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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

            // ---------------- METODE PEMBAYARAN ----------------
            Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // 1. Opsi Tunai
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if(selectedPayment == "Tunai") MaterialTheme.colorScheme.primaryContainer else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedPayment = "Tunai" }
                        .border(1.dp, if(selectedPayment == "Tunai") MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        RadioButton(selected = selectedPayment == "Tunai", onClick = { selectedPayment = "Tunai" })
                        Text("Tunai (COD / Bayar di Tempat)")
                    }
                }

                // 2. Opsi Non-Tunai
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if(selectedPayment == "Non-Tunai") MaterialTheme.colorScheme.primaryContainer else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedPayment = "Non-Tunai" }
                        .border(1.dp, if(selectedPayment == "Non-Tunai") MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        RadioButton(selected = selectedPayment == "Non-Tunai", onClick = { selectedPayment = "Non-Tunai" })
                        Text("Non-Tunai (Transfer / QRIS)")
                    }
                }

                // 3. Detail Non-Tunai
                if (selectedPayment == "Non-Tunai") {
                    Spacer(modifier = Modifier.height(8.dp))
                    // CARD INFO REKENING
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🏦 Transfer Bank", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("BCA", fontWeight = FontWeight.Bold)
                            Text("3820079107", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Text("A/N: Ayyun Izzati", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // CARD INFO QRIS
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📱 Scan QRIS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("(Klik gambar untuk memperbesar)", fontSize = 10.sp, color = Color.Gray)
                            Box(modifier = Modifier.clickable { showQrisDialog = true }) {
                                Image(
                                    painter = painterResource(id = R.drawable.qris_code),
                                    contentDescription = "QRIS Code",
                                    modifier = Modifier.size(200.dp).padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            // ---------------- UPLOAD BUKTI ----------------
            if (selectedPayment == "Non-Tunai") {
                Text("Bukti Transfer (Wajib)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 8.dp).clickable { galleryLauncher.launch("image/*") },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (paymentUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(paymentUri),
                                contentDescription = "Bukti Bayar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                Text("Ketuk untuk ganti foto", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                Text("Upload Bukti Transfer", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- TOMBOL AKSI (KERANJANG + BELI) ----------------
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // 1. TOMBOL KERANJANG
                OutlinedButton(
                    onClick = { cartViewModel.addToCart(product, qty) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                }

                // 2. TOMBOL BELI LANGSUNG
                Button(
                    onClick = {
                        var finalBase64 = ""
                        if (paymentUri != null) {
                            try {
                                finalBase64 = compressUriToBase64(context, paymentUri!!)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal memproses gambar.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }
                        if (paymentUri != null) {
                            Toast.makeText(context, "Size: ${finalBase64.length} chars", Toast.LENGTH_SHORT).show()
                        }

                        viewModel.submitOrder(
                            product, qty, address, lat, long,
                            hasPaymentProof = (paymentUri != null),
                            paymentMethod = selectedPayment,
                            paymentProofBase64 = finalBase64
                        )
                    },
                    modifier = Modifier.weight(3f).height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White)
                    else Text("BELI LANGSUNG")
                }
            }
        }
    }
}

// --- FUNGSI BANTUAN ---
fun compressUriToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeStream(inputStream, null, options)
    inputStream?.close()

    var scale = 1
    while (options.outWidth / scale > 600 || options.outHeight / scale > 600) {
        scale *= 2
    }

    val options2 = BitmapFactory.Options()
    options2.inSampleSize = scale
    val inputStream2 = context.contentResolver.openInputStream(uri)
    val resizedBitmap = BitmapFactory.decodeStream(inputStream2, null, options2)
    inputStream2?.close()

    val outputStream = ByteArrayOutputStream()
    resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    val byteArr = outputStream.toByteArray()

    val base64String = Base64.encodeToString(byteArr, Base64.DEFAULT)
    return "data:image/jpeg;base64,$base64String"
}