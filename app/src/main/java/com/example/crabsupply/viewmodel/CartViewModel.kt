package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crabsupply.data.model.CartItem
import com.example.crabsupply.data.model.Order
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.CartRepository
import com.example.crabsupply.data.repository.OrderRepository // Tambahkan ini
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.*

class CartViewModel : ViewModel() {
    private val repository = CartRepository()
    private val orderRepository = OrderRepository() // Butuh ini untuk create order
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // State Cart
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems
    private val _grandTotal = MutableStateFlow(0)
    val grandTotal: StateFlow<Int> = _grandTotal
    private val _cartStatus = MutableStateFlow<String?>(null)
    val cartStatus: StateFlow<String?> = _cartStatus

    // State Shipping Massal
    private val _shippingCost = MutableStateFlow(0)
    val shippingCost: StateFlow<Int> = _shippingCost
    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm

    // Koordinat Toko
    private val STORE_LAT = -7.7817801321614635
    private val STORE_LONG = 110.31995696627142

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            repository.getCartItems().collect { items ->
                _cartItems.value = items
                calculateGrandTotal()
            }
        }
    }

    // Hitung Total (Barang + Ongkir)
    private fun calculateGrandTotal() {
        var totalProduct = 0
        _cartItems.value.forEach { totalProduct += it.totalPriceItem }
        _grandTotal.value = totalProduct + _shippingCost.value
    }

    // Hitung Ongkir Sekali untuk Semua
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
        // Ongkir: 5000 per 2km
        val cost = (ceil(distance / 2.0) * 5000).toInt()
        _shippingCost.value = cost

        calculateGrandTotal() // Update total akhir
    }

    fun addToCart(product: Product, qtyStr: String) {
        val qty = qtyStr.toDoubleOrNull() ?: 0.0
        if (qty <= 0.0) { _cartStatus.value = "Qty invalid"; return }

        val priceUsed = if (qty >= 10.0) product.priceWholesale else product.priceRetail
        val totalItem = (priceUsed * qty).toInt()

        val newItem = CartItem(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            productName = product.name,
            productPrice = priceUsed, // Simpan harga fix saat itu
            productImageUrl = product.imageUrl,
            quantity = qty,
            totalPriceItem = totalItem
        )
        repository.addToCart(newItem) { success, msg -> _cartStatus.value = msg }
    }

    fun removeItem(itemId: String) {
        repository.removeFromCart(itemId) { if(it) _cartStatus.value = "Item dihapus" }
    }

    // --- LOGIKA CHECKOUT MASSAL ---
    fun processMassCheckout(
        address: String,
        paymentMethod: String,
        paymentProofBase64: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        val items = _cartItems.value

        if (items.isEmpty()) return
        if (address.isEmpty() || _distanceKm.value == 0.0) {
            _cartStatus.value = "Alamat wajib diisi!"
            return
        }
        if (paymentMethod == "Non-Tunai" && paymentProofBase64.isEmpty()) {
            _cartStatus.value = "Bukti Transfer Wajib!"
            return
        }

        _cartStatus.value = "LOADING" // Indikator loading custom

        // Ambil Data User Sekali
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val buyerName = document.getString("name") ?: "Tanpa Nama"
                val buyerPhone = document.getString("phone") ?: "-"
                val currentTime = System.currentTimeMillis()
                val fullAddress = "$address (Jarak: ${String.format("%.1f", _distanceKm.value)} km)"

                // LOOPING: Buat Order untuk setiap item
                // Note: Ongkir kita tempelkan ke Item Pertama saja agar total revenue admin valid
                // Atau, kita bagi rata. Biar gampang: Item pertama nanggung ongkir, sisanya harga produk saja.

                var processedCount = 0

                items.forEachIndexed { index, item ->
                    // Logika Ongkir: Hanya ditambahkan ke item index ke-0
                    val finalPrice = if (index == 0) (item.totalPriceItem + _shippingCost.value) else item.totalPriceItem

                    val newOrder = Order(
                        buyerId = userId,
                        buyerName = buyerName,
                        buyerPhone = buyerPhone,
                        productName = item.productName,
                        productSpecies = "Mass Order", // Penanda
                        pricePerKg = item.productPrice,
                        quantity = item.quantity,
                        totalPrice = finalPrice, // Harga item + Ongkir (jika item pertama)
                        address = fullAddress,
                        status = "pending",
                        paymentMethod = paymentMethod,
                        paymentProofImage = paymentProofBase64, // Gambar sama untuk semua order
                        dateCreated = currentTime
                    )

                    orderRepository.createOrder(newOrder) { success, _ ->
                        processedCount++
                        // Jika semua sudah diproses
                        if (processedCount == items.size) {
                            // KOSONGKAN KERANJANG
                            repository.clearCart {
                                _cartStatus.value = "CHECKOUT_SUCCESS"
                            }
                        }
                    }
                }
            }
    }

    fun resetStatus() { _cartStatus.value = null }
}