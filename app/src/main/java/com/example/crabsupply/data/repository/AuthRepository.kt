package com.example.crabsupply.data.repository

import com.example.crabsupply.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Fungsi untuk Mendaftar (Register)
    fun registerUser(
        name: String,
        email: String,
        phone: String,
        pass: String,
        onResult: (Boolean, String) -> Unit // Callback: (Sukses?, Pesan)
    ) {
        // 1. Buat Akun di Auth (Email & Password)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // 2. Siapkan data user untuk disimpan ke Database
                        // Default role kita set "buyer" sesuai SRS
                        val newUser = User(
                            id = userId,
                            name = name,
                            email = email,
                            phone = phone,
                            role = "buyer",
                            address = "",
                            latitude = 0.0,
                            longitude = 0.0
                        )

                        // 3. Simpan ke Firestore (Koleksi "users")
                        firestore.collection("users").document(userId)
                            .set(newUser)
                            .addOnSuccessListener {
                                onResult(true, "Registrasi Berhasil! Silakan Login.")
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Gagal simpan data: ${e.message}")
                            }
                    }
                } else {
                    // Jika email sudah dipakai atau password kurang dari 6 karakter
                    onResult(false, "Register Gagal: ${task.exception?.message}")
                }
            }
    }
    fun loginUser(
        email: String,
        pass: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login Berhasil!")
                } else {
                    onResult(false, "Login Gagal: ${task.exception?.message}")
                }
            }
    }

    fun getUserRole(onResult: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Ambil tulisan di kolom 'role'
                        val role = document.getString("role") ?: "buyer"
                        onResult(role)
                    } else {
                        onResult("buyer") // Default kalau tidak ketemu
                    }
                }
                .addOnFailureListener {
                    onResult("buyer")
                }
        } else {
            onResult("buyer")
        }
    }
}