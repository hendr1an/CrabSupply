package com.example.crabsupply.data.model


data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",        // No HP untuk kurir
    var role: String = "buyer",    // Peran: "admin" atau "buyer"

    // Data Lokasi untuk Fitur Tracking (SRS 3.2.3)
    var address: String = "",      // Alamat Teks Lengkap
    var latitude: Double = 0.0,    // Titik Koordinat Peta
    var longitude: Double = 0.0    // Titik Koordinat Peta
)