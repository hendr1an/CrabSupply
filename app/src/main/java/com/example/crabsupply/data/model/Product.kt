package com.example.crabsupply.data.model


data class Product(
    var id: String = "",             // ID Unik dari Firebase
    var name: String = "",           // Contoh: "Kepiting Bakau Telur 300gr"
    var species: String = "",        // Filter: "Bakau" atau "Rajungan" [SRS 3.1.3]
    var condition: String = "",      // Filter: "Telur", "Daging", "Bancah" [SRS 3.1.3]
    var size: String = "",           // Filter: "200gr", "500gr", dll [SRS 3.1.3]

    // Harga Hybrid (SRS 3.4.3)
    var priceRetail: Int = 0,        // Harga Eceran (Normal)
    var priceWholesale: Int = 0,     // Harga Grosir (Murah)

    var stock: Int = 0,              // Jumlah Stok Fisik
    var isAvailable: Boolean = true, // Toggle Status: Tersedia/Habis [SRS 3.4.3]
    var imageUrl: String = ""        // URL Foto dari Firebase Storage
)