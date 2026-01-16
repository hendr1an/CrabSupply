package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import com.example.crabsupply.data.model.Order
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _orderStatus = MutableStateFlow<String?>(null)
    val orderStatus: StateFlow<String?> = _orderStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Fungsi Membuat Pesanan
    fun submitOrder(product: Product, qtyInput: String, address: String) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _orderStatus.value = "User tidak ditemukan, harap login ulang."
            _isLoading.value = false
            return
        }

        val qty = qtyInput.toIntOrNull() ?: 0
        if (qty <= 0) {
            _orderStatus.value = "Jumlah pesanan minimal 1 kg!"
            _isLoading.value = false
            return
        }

        if (address.isEmpty()) {
            _orderStatus.value = "Alamat pengiriman wajib diisi!"
            _isLoading.value = false
            return
        }

        // 1. Ambil Data Diri Pembeli dulu (Nama & HP) dari Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val buyerName = document.getString("name") ?: "Tanpa Nama"
                val buyerPhone = document.getString("phone") ?: "-"

                // 2. Buat Objek Order
                val newOrder = Order(
                    buyerId = userId,
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    productName = product.name,
                    productSpecies = product.species,
                    pricePerKg = product.priceRetail, // Asumsi harga eceran
                    quantity = qty,
                    totalPrice = product.priceRetail * qty,
                    address = address
                )

                // 3. Kirim ke Repository
                repository.createOrder(newOrder) { success, msg ->
                    _isLoading.value = false
                    if (success) _orderStatus.value = "SUCCESS"
                    else _orderStatus.value = msg
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
                _orderStatus.value = "Gagal mengambil data user."
            }
    }

    fun resetStatus() {
        _orderStatus.value = null
    }
}