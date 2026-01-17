package com.example.crabsupply.data.model

data class CartItem(
    val id: String = "", // ID unik item di keranjang
    val productId: String = "",
    val productName: String = "",
    val productPrice: Int = 0,
    val productWholesalePrice: Int = 0, // Harga grosir (jika ada)
    val productImageUrl: String = "",
    val quantity: Double = 0.0,

    // Helper untuk hitung total per item
    // Nanti dihitung ulang di ViewModel biar aman
    val totalPriceItem: Int = 0
)