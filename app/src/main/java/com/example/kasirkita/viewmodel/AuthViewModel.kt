package com.example.kasirkita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasirkita.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    // State baru untuk role
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    // State untuk form tambah kasir
    private val _kasirEmail = MutableStateFlow("")
    val kasirEmail: StateFlow<String> = _kasirEmail

    private val _kasirPassword = MutableStateFlow("")
    val kasirPassword: StateFlow<String> = _kasirPassword

    private val _kasirName = MutableStateFlow("")
    val kasirName: StateFlow<String> = _kasirName

    init {
        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                _authCheckState.value = when (status) {
                    is SessionStatus.Authenticated -> {
                        // Ambil role setelah authenticated
                        fetchUserRole()
                        AuthCheckState.Authenticated
                    }
                    is SessionStatus.NotAuthenticated -> AuthCheckState.NotAuthenticated
                    is SessionStatus.Initializing -> AuthCheckState.Checking
                    is SessionStatus.RefreshFailure -> {
                        if (repository.isLoggedIn()) AuthCheckState.Authenticated
                        else AuthCheckState.NotAuthenticated
                    }
                }
            }
        }
    }

    private fun fetchUserRole() {
        viewModelScope.launch {
            try {
                _userRole.value = repository.getUserRole()
            } catch (e: Exception) {
                _userRole.value = null
            }
        }
    }

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onKasirEmailChange(value: String) { _kasirEmail.value = value }
    fun onKasirPasswordChange(value: String) { _kasirPassword.value = value }
    fun onKasirNameChange(value: String) { _kasirName.value = value }

    fun login() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.login(email = _email.value, password = _password.value)
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "Login gagal")
            }
        }
    }

    fun registerKasir() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.registerKasir(
                    email = _kasirEmail.value,
                    password = _kasirPassword.value,
                    name = _kasirName.value
                )
                _uiState.value = AuthUiState.Success
                // Reset form
                _kasirEmail.value = ""
                _kasirPassword.value = ""
                _kasirName.value = ""
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "Gagal tambah kasir")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _userRole.value = null
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}