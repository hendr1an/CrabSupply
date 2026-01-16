package com.example.crabsupply.data.repository

import com.example.crabsupply.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Fungsi Kirim Pesanan
    fun createOrder(order: Order, onResult: (Boolean, String) -> Unit) {
        firestore.collection("orders")
            .add(order)
            .addOnSuccessListener { docRef ->
                // Update ID dokumen agar sama
                docRef.update("id", docRef.id)
                onResult(true, "Pesanan Berhasil Dibuat!")
            }
            .addOnFailureListener { e ->
                onResult(false, "Gagal memesan: ${e.message}")
            }
    }
}