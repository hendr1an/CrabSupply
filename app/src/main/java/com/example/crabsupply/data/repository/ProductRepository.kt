package com.example.crabsupply.data.repository

import com.example.crabsupply.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Fungsi mengambil semua produk
    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            snapshot.documents.map { doc ->
                // Mengubah data Firestore menjadi objek Product kita
                doc.toObject(Product::class.java)!!.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList() // Kalau error, kembalikan list kosong
        }
    }

    fun addProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        // Kita biarkan Firebase membuat ID unik otomatis (.add)
        firestore.collection("products")
            .add(product)
            .addOnSuccessListener { documentReference ->
                // Update ID di dalam dokumen agar sama dengan ID otomatisnya (Opsional tapi rapi)
                documentReference.update("id", documentReference.id)
                onResult(true, "Produk Berhasil Ditambahkan!")
            }
            .addOnFailureListener { e ->
                onResult(false, "Gagal upload: ${e.message}")
            }
    }

    fun updateProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        firestore.collection("products").document(product.id)
            .set(product) // Menimpa data lama dengan data baru
            .addOnSuccessListener {
                onResult(true, "Produk Berhasil Diupdate!")
            }
            .addOnFailureListener { e ->
                onResult(false, "Gagal update: ${e.message}")
            }
    }

    // 2. FUNGSI HAPUS (DELETE)
    fun deleteProduct(productId: String, onResult: (Boolean, String) -> Unit) {
        firestore.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                onResult(true, "Produk Berhasil Dihapus!")
            }
            .addOnFailureListener { e ->
                onResult(false, "Gagal hapus: ${e.message}")
            }
    }
}