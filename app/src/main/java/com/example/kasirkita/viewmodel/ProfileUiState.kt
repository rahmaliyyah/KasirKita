package com.example.kasirkita.viewmodel

import com.example.kasirkita.repository.UserProfile

/*
 * State untuk daftar profil.
 * Owner mendapat semua profil, cashier hanya profil miliknya (RLS).
 */
sealed class ProfileListUiState {
    object Loading : ProfileListUiState()
    data class Success(val profiles: List<UserProfile>) : ProfileListUiState()
    data class Error(val message: String) : ProfileListUiState()
}

/*
 * State untuk aksi: create/update profil.
 */
sealed class ProfileActionState {
    object Idle : ProfileActionState()
    object Loading : ProfileActionState()
    object Success : ProfileActionState()
    data class Error(val message: String) : ProfileActionState()
}