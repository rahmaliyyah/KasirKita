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

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _isRoleLoaded = MutableStateFlow(false)
    val isRoleLoaded: StateFlow<Boolean> = _isRoleLoaded

    private val _kasirEmail = MutableStateFlow("")
    val kasirEmail: StateFlow<String> = _kasirEmail

    private val _kasirPassword = MutableStateFlow("")
    val kasirPassword: StateFlow<String> = _kasirPassword

    private val _kasirName = MutableStateFlow("")
    val kasirName: StateFlow<String> = _kasirName

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    init {
        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                _authCheckState.value = when (status) {
                    is SessionStatus.Authenticated -> {
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
                _isRoleLoaded.value = false
                val profile = repository.getUserProfile()
                android.util.Log.d("AUTH_DEBUG", "Profile fetched: '${profile?.name}' role: '${profile?.role}'")
                _userRole.value = profile?.role
                _userName.value = profile?.name
            } catch (e: Exception) {
                android.util.Log.e("AUTH_DEBUG", "Error fetch profile: ${e.message}")
                _userRole.value = null
                _userName.value = null
            } finally {
                _isRoleLoaded.value = true
            }
        }
    }

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onKasirEmailChange(value: String) { _kasirEmail.value = value }
    fun onKasirPasswordChange(value: String) { _kasirPassword.value = value }
    fun onKasirNameChange(value: String) { _kasirName.value = value }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

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
                _kasirEmail.value = ""
                _kasirPassword.value = ""
                _kasirName.value = ""
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "Gagal tambah kasir")
            }
        }
    }

    fun updateCurrentUserName(newName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.updateProfileName(newName)
                _userName.value = newName
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(message = e.message ?: "Gagal update nama")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _userRole.value = null
            _isRoleLoaded.value = false
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}