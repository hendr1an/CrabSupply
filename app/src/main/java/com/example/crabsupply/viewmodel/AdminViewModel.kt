package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _uploadStatus = MutableStateFlow<String?>(null)
    val uploadStatus: StateFlow<String?> = _uploadStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // FUNGSI 1: Upload (Simpan Produk Baru)
    fun uploadProduct(
        name: String,
        species: String,
        condition: String,
        size: String,
        priceRetail: String, // String dulu biar gampang diinput
        stock: String
    ) {
        _isLoading.value = true

        // Konversi String ke Angka (Handle error kalau user input huruf)
        val priceInt = priceRetail.toIntOrNull() ?: 0
        val stockInt = stock.toIntOrNull() ?: 0

        // Masukkan ke wadah Model Data
        val newProduct = Product(
            name = name,
            species = species,
            condition = condition,
            size = size,
            priceRetail = priceInt,
            priceWholesale = priceInt - 10000, // Logika dummy: Grosir lebih murah 10rb
            stock = stockInt,
            isAvailable = stockInt > 0
        )

        repository.addProduct(newProduct) { success, message ->
            _isLoading.value = false
            if (success) {
                _uploadStatus.value = "SUCCESS"
            } else {
                _uploadStatus.value = message
            }
        }
    }


    fun deleteProduct(productId: String) {
        _isLoading.value = true
        repository.deleteProduct(productId) { success, message ->
            _isLoading.value = false
            if (success) {
                _uploadStatus.value = "DELETE_SUCCESS" // Kode khusus
            } else {
                _uploadStatus.value = message
            }
        }
    }

    // FUNGSI 3: Reset Status
    fun resetStatus() {
        _uploadStatus.value = null
    }

    // ... (kode sebelumnya)

    // FUNGSI 3: Update (Edit Produk Lama)
    fun updateExistingProduct(
        id: String, // Kita butuh ID untuk tahu produk mana yang diedit
        name: String,
        species: String,
        condition: String,
        size: String,
        priceRetail: String,
        stock: String
    ) {
        _isLoading.value = true

        val priceInt = priceRetail.toIntOrNull() ?: 0
        val stockInt = stock.toIntOrNull() ?: 0

        // Masukkan data baru ke objek Product (ID tetap sama)
        val updatedProduct = Product(
            id = id, // PENTING: ID tidak boleh berubah!
            name = name,
            species = species,
            condition = condition,
            size = size,
            priceRetail = priceInt,
            priceWholesale = priceInt - 10000,
            stock = stockInt,
            isAvailable = stockInt > 0
        )

        repository.updateProduct(updatedProduct) { success, message ->
            _isLoading.value = false
            if (success) {
                _uploadStatus.value = "UPDATE_SUCCESS" // Kode khusus update
            } else {
                _uploadStatus.value = message
            }
        }
    }
}