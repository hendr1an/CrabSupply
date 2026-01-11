package com.example.crabsupply.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit = {} // Nanti dipakai buat navigasi
) {
    // Variabel untuk menyimpan apa yang diketik user
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Column = Menyusun elemen dari atas ke bawah (Vertikal)
    Column(
        modifier = Modifier
            .fillMaxSize() // Memenuhi layar HP
            .padding(24.dp), // Jarak dari pinggir layar
        verticalArrangement = Arrangement.Center, // Konten di tengah vertikal
        horizontalAlignment = Alignment.CenterHorizontally // Konten di tengah horizontal
    ) {
        // 1. Judul Aplikasi
        Text(
            text = "CrabSupply Login",
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp)) // Jarak kosong

        // 2. Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Input Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation() // Biar huruf jadi bintang2/titik
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Tombol Masuk
        Button(
            onClick = {
                // Nanti kita isi logika Firebase Login di sini
                isLoading = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading // Tombol mati kalau lagi loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = "MASUK (LOGIN)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Link ke Register (Teks biasa dulu)
        TextButton(onClick = { /* Nanti arahkan ke Register */ }) {
            Text("Belum punya akun? Daftar di sini")
        }
    }
}

// Fitur Preview: Biar bisa lihat desain tanpa jalankan Emulator
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}