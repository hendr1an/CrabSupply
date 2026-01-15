package com.example.crabsupply.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crabsupply.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBackClick: () -> Unit = {}
) {
    val viewModel: AdminViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val status by viewModel.uploadStatus.collectAsState()
    val context = LocalContext.current

    // Notifikasi Sukses/Gagal
    LaunchedEffect(status) {
        status?.let { msg ->
            if (msg == "SUCCESS") {
                Toast.makeText(context, "Produk Disimpan!", Toast.LENGTH_SHORT).show()
                onBackClick() // Kembali ke menu sebelumnya
            } else {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            viewModel.resetStatus()
        }
    }

    // Variabel Input Form (Sesuai Model Data Anda)
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") } // Contoh: Bakau/Rajungan
    var condition by remember { mutableStateOf("") } // Contoh: Telur/Daging
    var size by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tambah Produk Baru") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Agar bisa discroll kalau keypad muncul
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Produk (Ex: Kepiting Super)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = species, onValueChange = { species = it },
                label = { Text("Jenis (Ex: Bakau / Rajungan)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = condition, onValueChange = { condition = it },
                label = { Text("Kondisi (Ex: Telur / Jantan)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = size, onValueChange = { size = it },
                label = { Text("Ukuran (Ex: 500gr up)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Input Angka (Keyboard Angka)
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Harga Eceran (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = stock, onValueChange = { stock = it },
                label = { Text("Stok Awal (Kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && price.isNotEmpty()) {
                        viewModel.uploadProduct(name, species, condition, size, price, stock)
                    } else {
                        Toast.makeText(context, "Nama dan Harga wajib diisi!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                else Text("SIMPAN PRODUK")
            }
        }
    }
}