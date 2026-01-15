package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = ProductRepository()

    // Data List Produk untuk ditampilkan
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    init {
        loadProducts() // Otomatis ambil data saat Home dibuka
    }

    fun loadProducts() {
        viewModelScope.launch {
            val result = repository.getAllProducts()
            _products.value = result
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}