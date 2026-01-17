package com.example.crabsupply.data.repository

import com.example.crabsupply.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

    fun getAllOrdersRealtime(): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("orders")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Urutkan dari yang terbaru
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.map { doc ->
                        doc.toObject(Order::class.java)!!.copy(id = doc.id)
                    }
                    trySend(orders)
                }
            }
        awaitClose { listener.remove() }
    }

    // 2. FUNGSI UPDATE STATUS PESANAN
    // GANTI FUNGSI updateOrderStatus LAMA DENGAN INI:
    fun updateOrderStatus(orderId: String, newStatus: String, callback: (Boolean) -> Unit) {
        val updates = mutableMapOf<String, Any>("status" to newStatus)

        // Jika status berubah jadi "proses", catat waktunya
        if (newStatus == "proses") {
            updates["dateProcessed"] = System.currentTimeMillis()
        }
        // Jika status berubah jadi "selesai", catat waktunya
        if (newStatus == "selesai") {
            updates["dateCompleted"] = System.currentTimeMillis()
        }

        firestore.collection("orders").document(orderId)
            .update(updates)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getOrdersByBuyerId(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereEqualTo("buyerId", userId) // <--- Filter: Hanya ambil punya user ini
            // (Opsional) .orderBy("timestamp", Query.Direction.DESCENDING)
            // Catatan: Jika pakai orderBy + whereEqualTo, Firebase minta buat Index di Console.
            // Biar tidak ribet, kita urutkan manual di kodingan saja (Kotlin).
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.map { doc ->
                        doc.toObject(Order::class.java)!!.copy(id = doc.id)
                    }
                    // Kita urutkan di sini (Terbaru di atas)
                    val sortedOrders = orders.sortedByDescending { it.timestamp }
                    trySend(sortedOrders)
                }
            }
        awaitClose { listener.remove() }
    }
}