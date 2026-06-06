package com.example.kasirkita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasirkita.repository.ProfileRepository
import com.example.kasirkita.repository.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()
    private val authRepository = com.example.kasirkita.repository.AuthRepository()

    // ── State untuk daftar profil ───────────────────────────────────
    private val _profileListState = MutableStateFlow<ProfileListUiState>(ProfileListUiState.Loading)
    val profileListState: StateFlow<ProfileListUiState> = _profileListState

    // ── State untuk aksi (create, update, delete) ───────────────────
    private val _actionState = MutableStateFlow<ProfileActionState>(ProfileActionState.Idle)
    val actionState: StateFlow<ProfileActionState> = _actionState

    // ── Profil yang sedang dibuka detailnya ─────────────────────────
    private val _selectedProfile = MutableStateFlow<UserProfile?>(null)
    val selectedProfile: StateFlow<UserProfile?> = _selectedProfile

    // ── Input form ──────────────────────────────────────────────────
    private val _profileName = MutableStateFlow("")
    val profileName: StateFlow<String> = _profileName

    private val _profileEmail = MutableStateFlow("")
    val profileEmail: StateFlow<String> = _profileEmail

    private val _profilePassword = MutableStateFlow("")
    val profilePassword: StateFlow<String> = _profilePassword

    private val _profileRole = MutableStateFlow("cashier")
    val profileRole: StateFlow<String> = _profileRole

    // ── Fungsi update input dari UI ─────────────────────────────────
    fun onProfileNameChange(value: String) { _profileName.value = value }
    fun onProfileEmailChange(value: String) { _profileEmail.value = value }
    fun onProfilePasswordChange(value: String) { _profilePassword.value = value }
    fun onProfileRoleChange(value: String) { _profileRole.value = value }

    fun resetActionState() { _actionState.value = ProfileActionState.Idle }

    fun resetProfileForm() {
        _profileName.value = ""
        _profileEmail.value = ""
        _profilePassword.value = ""
        _profileRole.value = "cashier"
    }

    // ── Set profil yang dipilih ─────────────────────────────────────
    fun selectProfile(profile: UserProfile) {
        _selectedProfile.value = profile
    }

    // ── Load semua profil ───────────────────────────────────────────
    fun loadProfiles() {
        viewModelScope.launch {
            try {
                if (_profileListState.value !is ProfileListUiState.Success) {
                    _profileListState.value = ProfileListUiState.Loading
                }
                val list = repository.getProfiles()
                _profileListState.value = ProfileListUiState.Success(list)
            } catch (e: Exception) {
                _profileListState.value = ProfileListUiState.Error(
                    e.message ?: "Gagal memuat data profil"
                )
            }
        }
    }

    /**
     * Tambah kasir baru.
     * Menggunakan Shadow Client di AuthRepository agar Owner tidak logout.
     */
    fun addCashier() {
        val name = _profileName.value.trim()
        val email = _profileEmail.value.trim()
        val password = _profilePassword.value

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _actionState.value = ProfileActionState.Error("Semua field harus diisi")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = ProfileActionState.Loading
                authRepository.registerKasir(email, password, name)
                _actionState.value = ProfileActionState.Success
                resetProfileForm()
                loadProfiles()
            } catch (e: Exception) {
                _actionState.value = ProfileActionState.Error(
                    e.message ?: "Gagal mendaftarkan kasir"
                )
            }
        }
    }

    // ── Update nama/role profil — hanya owner ───────────────────────
    fun updateProfile(id: String) {
        val name = _profileName.value.trim()
        if (name.isEmpty()) {
            _actionState.value = ProfileActionState.Error("Nama tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = ProfileActionState.Loading
                repository.updateProfile(
                    id = id,
                    name = name,
                    role = _profileRole.value
                )
                _actionState.value = ProfileActionState.Success
                resetProfileForm()
                loadProfiles()
            } catch (e: Exception) {
                _actionState.value = ProfileActionState.Error(
                    e.message ?: "Gagal memperbarui profil"
                )
            }
        }
    }

    /**
     * Hapus profil.
     * Saat ini hanya menghapus dari tabel profiles.
     * Jika trigger delete terpasang di Supabase, menghapus dari auth.users 
     * akan lebih baik, tapi membutuhkan Service Role Key.
     * Jadi kita hapus dari public.profiles saja, atau mark inactive.
     */
    fun deleteProfile(id: String) {
        viewModelScope.launch {
            try {
                _actionState.value = ProfileActionState.Loading
                repository.deleteProfile(id)
                _actionState.value = ProfileActionState.Success
                loadProfiles()
            } catch (e: Exception) {
                _actionState.value = ProfileActionState.Error(
                    e.message ?: "Gagal menghapus profil"
                )
            }
        }
    }
}