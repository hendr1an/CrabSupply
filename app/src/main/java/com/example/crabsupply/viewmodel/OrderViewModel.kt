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
import kotlin.math.*

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Variable Status & Loading
    private val _orderStatus = MutableStateFlow<String?>(null)
    val orderStatus: StateFlow<String?> = _orderStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- LOGIKA HARGA BARANG ---
    private val _calculatedPrice = MutableStateFlow(0)
    val calculatedPrice: StateFlow<Int> = _calculatedPrice

    private val _isWholesale = MutableStateFlow(false)
    val isWholesale: StateFlow<Boolean> = _isWholesale

    // Koordinat Toko
    private val STORE_LAT = -7.7817801321614635
    private val STORE_LONG = 110.31995696627142

    private val _shippingCost = MutableStateFlow(0)
    val shippingCost: StateFlow<Int> = _shippingCost

    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm

    private val _finalTotal = MutableStateFlow(0)
    val finalTotal: StateFlow<Int> = _finalTotal

    // 1. FUNGSI HITUNG HARGA BARANG
    fun calculatePrice(qtyInput: String, product: Product) {
        val qty = qtyInput.toDoubleOrNull() ?: 0.0

        if (qty >= 10.0) {
            _calculatedPrice.value = (product.priceWholesale * qty).toInt()
            _isWholesale.value = true
        } else {
            _calculatedPrice.value = (product.priceRetail * qty).toInt()
            _isWholesale.value = false
        }
        updateFinalTotal()
    }

    // 2. Fungsi Hitung Jarak & Ongkir
    fun calculateShipping(userLat: Double, userLong: Double) {
        val r = 6371.0
        val dLat = Math.toRadians(userLat - STORE_LAT)
        val dLon = Math.toRadians(userLong - STORE_LONG)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(STORE_LAT)) * cos(Math.toRadians(userLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c

        _distanceKm.value = distance
        val cost = (ceil(distance / 2.0) * 5000).toInt()
        _shippingCost.value = cost
        updateFinalTotal()
    }

    private fun updateFinalTotal() {
        _finalTotal.value = _calculatedPrice.value + _shippingCost.value
    }

    // 4. FUNGSI SUBMIT ORDER (REVISI: TERIMA GAMBAR BASE64)
    fun submitOrder(
        product: Product,
        qtyInput: String,
        address: String,
        latitude: String,
        longitude: String,
        hasPaymentProof: Boolean,
        paymentMethod: String,
        paymentProofBase64: String // <--- PARAMETER BARU (String Gambar)
    ) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _orderStatus.value = "User error, login ulang."
            _isLoading.value = false
            return
        }

        val qty = qtyInput.toDoubleOrNull() ?: 0.0

        if (qty <= 0.0) {
            _orderStatus.value = "Minimal pembelian 0.1 kg!"
            _isLoading.value = false
            return
        }

        if (address.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
            _orderStatus.value = "Alamat dan Lokasi Peta wajib diisi!"
            _isLoading.value = false
            return
        }

        // Validasi Bukti Bayar: Jika Non-Tunai, gambar tidak boleh kosong
        if (paymentMethod == "Non-Tunai" && paymentProofBase64.isEmpty()) {
            _orderStatus.value = "Wajib upload bukti transfer!"
            _isLoading.value = false
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val buyerName = document.getString("name") ?: "Tanpa Nama"
                val buyerPhone = document.getString("phone") ?: "-"

                val finalPricePerKg = if (qty >= 10.0) product.priceWholesale else product.priceRetail
                val totalBayar = _finalTotal.value

                val newOrder = Order(
                    buyerId = userId,
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    productName = product.name,
                    productSpecies = product.species,
                    pricePerKg = finalPricePerKg,
                    quantity = qty,
                    totalPrice = totalBayar,
                    address = "$address (Jarak: ${String.format("%.1f", _distanceKm.value)} km)",
                    status = "pending",

                    // --- SIMPAN DATA BARU ---
                    paymentMethod = paymentMethod,
                    paymentProofImage = paymentProofBase64, // Simpan Gambar String ke Database
                    dateCreated = System.currentTimeMillis()
                )

                repository.createOrder(newOrder) { success, msg ->
                    _isLoading.value = false
                    if (success) _orderStatus.value = "SUCCESS"
                    else _orderStatus.value = msg
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
                _orderStatus.value = "Gagal koneksi database."
            }
    }

    // --- BAGIAN LAIN ---
    private val _buyerOrders = MutableStateFlow<List<Order>>(emptyList())
    val buyerOrders: StateFlow<List<Order>> = _buyerOrders

    fun loadOrdersForBuyer() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                repository.getOrdersByBuyerId(userId).collect { list -> _buyerOrders.value = list }
            }
        }
    }

    private val _adminOrders = MutableStateFlow<List<Order>>(emptyList())
    val adminOrders: StateFlow<List<Order>> = _adminOrders

    fun loadOrdersForAdmin() {
        viewModelScope.launch {
            repository.getAllOrdersRealtime().collect { list -> _adminOrders.value = list }
        }
    }

    fun updateStatus(orderId: String, newStatus: String) {
        _isLoading.value = true
        repository.updateOrderStatus(orderId, newStatus) { success ->
            _isLoading.value = false
            if (success) _orderStatus.value = "Status Berubah: $newStatus"
            else _orderStatus.value = "Gagal update status"
        }
    }

    fun resetStatus() {
        _orderStatus.value = null
    }
}