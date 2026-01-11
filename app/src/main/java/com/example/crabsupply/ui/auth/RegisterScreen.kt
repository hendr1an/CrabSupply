package com.example.crabsupply.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Import ini penting
import com.example.crabsupply.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    // Panggil ViewModel
    val viewModel: AuthViewModel = viewModel()

    // Pantau data dari ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val status by viewModel.authStatus.collectAsState()

    val context = LocalContext.current

    // Cek Status: Kalau sukses, pindah layar. Kalau gagal, munculkan Toast.
    LaunchedEffect(status) {
        status?.let { msg ->
            if (msg == "SUCCESS") {
                Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess() // Pindah ke Login
            } else {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            viewModel.resetStatus()
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Daftar Akun Baru",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Nomor HP (WhatsApp)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (Min 6 Karakter)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Validasi sederhana sebelum kirim ke Firebase
                if (name.isNotEmpty() && email.isNotEmpty() && password.length >= 6) {
                    viewModel.register(name, email, phone, password)
                } else {
                    Toast.makeText(context, "Data belum lengkap / Password < 6", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading // Matikan tombol kalau lagi loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = "DAFTAR SEKARANG")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onLoginClick() }) {
            Text("Sudah punya akun? Login di sini")
        }
    }
}