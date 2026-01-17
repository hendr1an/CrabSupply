package com.example.crabsupply.data.repository

import com.example.crabsupply.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CartRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Ambil User ID saat ini
    private val userId: String?
        get() = auth.currentUser?.uid

    // 1. TAMBAH KE KERANJANG
    fun addToCart(item: CartItem, callback: (Boolean, String) -> Unit) {
        if (userId == null) {
            callback(false, "User belum login")
            return
        }

        // Simpan ke path: users/{userId}/cart/{itemId}
        firestore.collection("users").document(userId!!)
            .collection("cart").document(item.id)
            .set(item)
            .addOnSuccessListener { callback(true, "Masuk Keranjang!") }
            .addOnFailureListener { callback(false, it.message ?: "Gagal") }
    }

    // 2. AMBIL LIST KERANJANG (REALTIME)
    fun getCartItems(): Flow<List<CartItem>> = callbackFlow {
        if (userId == null) {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId!!)
            .collection("cart")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.toObjects(CartItem::class.java)
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }

    // 3. HAPUS DARI KERANJANG
    fun removeFromCart(itemId: String, callback: (Boolean) -> Unit) {
        if (userId == null) return

        firestore.collection("users").document(userId!!)
            .collection("cart").document(itemId)
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // 4. UPDATE QUANTITY (Opsional, untuk tombol + / - di keranjang)
    fun updateCartQty(itemId: String, newQty: Double, newTotal: Int) {
        if (userId == null) return

        val updates = mapOf(
            "quantity" to newQty,
            "totalPriceItem" to newTotal
        )

        firestore.collection("users").document(userId!!)
            .collection("cart").document(itemId)
            .update(updates)
    }

    fun clearCart(callback: () -> Unit) {
        if (userId == null) return

        // Ambil semua dokumen dulu, baru hapus satu-satu (Batch Delete)
        firestore.collection("users").document(userId!!)
            .collection("cart")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener { callback() }
            }
    }
}