package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crabsupply.data.model.Order
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Variable Status Umum
    private val _orderStatus = MutableStateFlow<String?>(null)
    val orderStatus: StateFlow<String?> = _orderStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ==========================================
    // BAGIAN 1: LOGIKA PEMBELI (BUAT PESANAN)
    // ==========================================

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

        // 1. Ambil Data Diri Pembeli dulu
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
                    pricePerKg = product.priceRetail,
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

    // ==========================================
    // BAGIAN 2: LOGIKA PEMBELI (LIHAT RIWAYAT) -- [INI YANG BARU]
    // ==========================================

    // List Pesanan Khusus Pembeli (Buyer)
    private val _buyerOrders = MutableStateFlow<List<Order>>(emptyList())
    val buyerOrders: StateFlow<List<Order>> = _buyerOrders

    // Fungsi Load Riwayat Saya
    fun loadOrdersForBuyer() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                // Pastikan fungsi ini sudah ada di OrderRepository (Langkah 1)
                repository.getOrdersByBuyerId(userId).collect { list ->
                    _buyerOrders.value = list
                }
            }
        }
    }

    // ==========================================
    // BAGIAN 3: LOGIKA ADMIN (KELOLA PESANAN)
    // ==========================================

    // List Semua Pesanan untuk Admin
    private val _adminOrders = MutableStateFlow<List<Order>>(emptyList())
    val adminOrders: StateFlow<List<Order>> = _adminOrders

    // Panggil ini saat Halaman Admin dibuka
    fun loadOrdersForAdmin() {
        viewModelScope.launch {
            repository.getAllOrdersRealtime().collect { list ->
                _adminOrders.value = list
            }
        }
    }

    // Fungsi Update Status (Terima/Kirim/Selesai)
    fun updateStatus(orderId: String, newStatus: String) {
        _isLoading.value = true
        repository.updateOrderStatus(orderId, newStatus) { success ->
            _isLoading.value = false
            if (success) _orderStatus.value = "Status Berubah: $newStatus"
            else _orderStatus.value = "Gagal update status"
        }
    }

    // ==========================================
    // UTILS
    // ==========================================

    fun resetStatus() {
        _orderStatus.value = null
    }
}