package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crabsupply.data.model.Product
import com.example.crabsupply.data.repository.AuthRepository
import com.example.crabsupply.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val authRepository = AuthRepository()

    // 1. Data Mentah
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    // 2. Input User
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 3. Filter Kategori
    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // 4. Hasil Akhir
    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts

    private val _userRole = MutableStateFlow("buyer")
    val userRole: StateFlow<String> = _userRole

    init {
        startRealtimeUpdates()
        checkUserRole()
        observeFilters()
    }

    // --- PERBAIKAN: FUNGSI UNTUK MEMAKSA CEK ROLE ---
    fun refreshUserRole() {
        checkUserRole()
    }

    private fun checkUserRole() {
        authRepository.getUserRole { role -> _userRole.value = role }
    }
    // ------------------------------------------------

    private fun startRealtimeUpdates() {
        viewModelScope.launch {
            productRepository.getProductsRealtime().collect { updatedList ->
                _allProducts.value = updatedList
            }
        }
    }

    // LOGIKA FILTER
    private fun observeFilters() {
        viewModelScope.launch {
            combine(_allProducts, _searchQuery, _selectedCategory) { list, query, category ->
                filterList(list, query, category)
            }.collect { result ->
                _filteredProducts.value = result
            }
        }
    }

    private fun filterList(list: List<Product>, query: String, category: String): List<Product> {
        return list.filter { product ->
            val matchCategory = if (category == "Semua") {
                true
            } else {
                product.species.equals(category, ignoreCase = true) ||
                        product.condition.contains(category, ignoreCase = true)
            }

            val matchQuery = if (query.isEmpty()) {
                true
            } else {
                product.name.contains(query, ignoreCase = true)
            }
            matchCategory && matchQuery
        }
    }

    fun onSearchTextChange(text: String) {
        _searchQuery.value = text
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}