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
import kotlin.math.* // PENTING: Untuk rumus matematika jarak (Haversine)

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Variable Status & Loading
    private val _orderStatus = MutableStateFlow<String?>(null)
    val orderStatus: StateFlow<String?> = _orderStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- LOGIKA HARGA BARANG (SRS 3.2.3 - Tiered Pricing) ---
    private val _calculatedPrice = MutableStateFlow(0)
    val calculatedPrice: StateFlow<Int> = _calculatedPrice

    private val _isWholesale = MutableStateFlow(false)
    val isWholesale: StateFlow<Boolean> = _isWholesale

    private val STORE_LAT = -7.7817801321614635
    private val STORE_LONG = 110.31995696627142

    private val _shippingCost = MutableStateFlow(0)
    val shippingCost: StateFlow<Int> = _shippingCost

    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm

    // TOTAL FINAL (Harga Barang + Ongkir)
    private val _finalTotal = MutableStateFlow(0)
    val finalTotal: StateFlow<Int> = _finalTotal

    // 1. Fungsi Hitung Harga Barang (Grosir vs Eceran)
    fun calculatePrice(qtyInput: String, product: Product) {
        val qty = qtyInput.toIntOrNull() ?: 0

        // Logika SRS: Jika qty >= 10kg, pakai Harga Grosir
        if (qty >= 10) {
            _calculatedPrice.value = product.priceWholesale * qty
            _isWholesale.value = true
        } else {
            _calculatedPrice.value = product.priceRetail * qty
            _isWholesale.value = false
        }

        // Setiap harga barang berubah, update total final
        updateFinalTotal()
    }

    // 2. Fungsi Hitung Jarak & Ongkir (BARU)
    fun calculateShipping(userLat: Double, userLong: Double) {
        // Rumus Haversine (Hitung jarak antar 2 koordinat bumi)
        val r = 6371.0 // Jari-jari bumi (km)
        val dLat = Math.toRadians(userLat - STORE_LAT)
        val dLon = Math.toRadians(userLong - STORE_LONG)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(STORE_LAT)) * cos(Math.toRadians(userLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c // Jarak dalam KM

        _distanceKm.value = distance

        // LOGIKA ONGKIR: Rp 5.000 per 2 KM (Pembulatan ke atas)
        // Contoh: 3.5 km -> dibulatkan jadi 4km -> (4/2)*5000 = 10.000
        val cost = (ceil(distance / 2.0) * 5000).toInt()
        _shippingCost.value = cost

        // Setiap ongkir berubah, update total final
        updateFinalTotal()
    }

    // 3. Update Total Bayar Keseluruhan
    private fun updateFinalTotal() {
        _finalTotal.value = _calculatedPrice.value + _shippingCost.value
    }

    // 4. Fungsi Submit Order (Simpan ke Database)
    fun submitOrder(
        product: Product,
        qtyInput: String,
        address: String,
        latitude: String,
        longitude: String,
        hasPaymentProof: Boolean
    ) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _orderStatus.value = "User error, login ulang."
            _isLoading.value = false
            return
        }

        val qty = qtyInput.toIntOrNull() ?: 0
        if (qty <= 0) {
            _orderStatus.value = "Minimal pembelian 1 kg!"
            _isLoading.value = false
            return
        }

        // Validasi Sesuai SRS
        if (address.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
            _orderStatus.value = "Alamat dan Lokasi Peta wajib diisi!"
            _isLoading.value = false
            return
        }

        if (!hasPaymentProof) {
            _orderStatus.value = "Wajib upload bukti transfer!"
            _isLoading.value = false
            return
        }

        // Ambil data user
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val buyerName = document.getString("name") ?: "Tanpa Nama"
                val buyerPhone = document.getString("phone") ?: "-"

                val finalPricePerKg = if (qty >= 10) product.priceWholesale else product.priceRetail

                // Gunakan FINAL TOTAL (Barang + Ongkir)
                val totalBayar = _finalTotal.value

                val newOrder = Order(
                    buyerId = userId,
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    productName = product.name,
                    productSpecies = product.species,
                    pricePerKg = finalPricePerKg,
                    quantity = qty,
                    totalPrice = totalBayar, // Simpan total yang sudah kena ongkir
                    address = "$address (Jarak: ${String.format("%.1f", _distanceKm.value)} km)",
                    status = "pending"
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

    // --- BAGIAN LAIN (HISTORY & ADMIN) ---
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