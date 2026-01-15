package com.example.crabsupply.viewmodel

import androidx.lifecycle.ViewModel
import com.example.crabsupply.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Variable untuk memantau status (Loading, Sukses, atau Error)
    private val _authStatus = MutableStateFlow<String?>(null)
    val authStatus: StateFlow<String?> = _authStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun register(name: String, email: String, phone: String, pass: String) {
        _isLoading.value = true // Mulai loading

        repository.registerUser(name, email, phone, pass) { success, message ->
            _isLoading.value = false // Stop loading
            if (success) {
                _authStatus.value = "SUCCESS"
            } else {
                _authStatus.value = message // Tampilkan pesan error
            }
        }
    }

    // Fungsi untuk mereset status setelah pesan tampil
    fun resetStatus() {
        _authStatus.value = null
    }

    fun login(email: String, pass: String) {
        _isLoading.value = true
        repository.loginUser(email, pass) { success, message ->
            _isLoading.value = false
            if (success) {
                _authStatus.value = "LOGIN_SUCCESS" // Kode khusus biar layar tahu
            } else {
                _authStatus.value = message
            }
        }
    }

}