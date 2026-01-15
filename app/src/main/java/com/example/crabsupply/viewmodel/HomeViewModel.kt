package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.AuthRepository // Tambah Import ini
import com.example.crabsupply.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val authRepository = AuthRepository() // Panggil AuthRepo

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    // Variabel untuk menyimpan Role User
    private val _userRole = MutableStateFlow("buyer") // Default buyer
    val userRole: StateFlow<String> = _userRole

    init {
        startRealtimeUpdates()
        checkUserRole() // Cek role saat aplikasi dibuka
    }

    private fun checkUserRole() {
        authRepository.getUserRole { role ->
            _userRole.value = role
        }
    }

    private fun startRealtimeUpdates() {
        viewModelScope.launch {
            productRepository.getProductsRealtime().collect { updatedList ->
                _products.value = updatedList
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}