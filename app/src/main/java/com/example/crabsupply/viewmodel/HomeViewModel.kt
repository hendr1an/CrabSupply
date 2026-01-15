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

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    init {
        // Langsung pasang "CCTV" saat ViewModel dibuat
        startRealtimeUpdates()
    }

    private fun startRealtimeUpdates() {
        viewModelScope.launch {
            // collect() artinya kita menampung aliran data terus menerus
            repository.getProductsRealtime().collect { updatedList ->
                _products.value = updatedList
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}