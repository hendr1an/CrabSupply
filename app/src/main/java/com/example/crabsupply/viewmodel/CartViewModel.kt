package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crabsupply.data.model.CartItem
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CartViewModel : ViewModel() {
    private val repository = CartRepository()

    // State List Barang di Keranjang
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    // State Total Harga Belanjaan
    private val _grandTotal = MutableStateFlow(0)
    val grandTotal: StateFlow<Int> = _grandTotal

    // State Loading & Pesan
    private val _cartStatus = MutableStateFlow<String?>(null)
    val cartStatus: StateFlow<String?> = _cartStatus

    init {
        loadCartItems()
    }

    // Load data Realtime
    private fun loadCartItems() {
        viewModelScope.launch {
            repository.getCartItems().collect { items ->
                _cartItems.value = items
                calculateGrandTotal(items)
            }
        }
    }

    // Hitung Total Semua Barang
    private fun calculateGrandTotal(items: List<CartItem>) {
        var total = 0
        items.forEach { total += it.totalPriceItem }
        _grandTotal.value = total
    }

    // Fungsi Tambah Barang (Dipanggil dari Detail Produk)
    fun addToCart(product: Product, qtyStr: String) {
        val qty = qtyStr.toDoubleOrNull() ?: 0.0
        if (qty <= 0.0) {
            _cartStatus.value = "Jumlah tidak valid"
            return
        }

        // Logika Harga Grosir
        val priceUsed = if (qty >= 10.0) product.priceWholesale else product.priceRetail
        val totalItem = (priceUsed * qty).toInt()

        val newItem = CartItem(
            id = UUID.randomUUID().toString(), // ID Unik
            productId = product.id,
            productName = product.name,
            productPrice = product.priceRetail,
            productWholesalePrice = product.priceWholesale,
            productImageUrl = product.imageUrl,
            quantity = qty,
            totalPriceItem = totalItem
        )

        repository.addToCart(newItem) { success, msg ->
            _cartStatus.value = msg
        }
    }

    // Fungsi Hapus Barang
    fun removeItem(itemId: String) {
        repository.removeFromCart(itemId) { success ->
            if (success) _cartStatus.value = "Barang dihapus"
        }
    }

    fun resetStatus() {
        _cartStatus.value = null
    }
}