package com.example.kasirkita.viewmodel

/*
 * Sealed class digunakan untuk membatasi kemungkinan state.
 * Dalam kasus auth, state-nya hanya:
 * Idle, Loading, Success, atau Error.
 */
sealed class AuthUiState {

    /*
     * Kondisi awal, belum ada proses autentikasi.
     */
    object Idle : AuthUiState()

    /*
     * Kondisi ketika proses autentikasi sedang berjalan.
     */
    object Loading : AuthUiState()

    /*
     * Kondisi ketika autentikasi berhasil.
     */
    object Success : AuthUiState()

    /*
     * Kondisi ketika autentikasi gagal.
     * message digunakan untuk menyimpan pesan error.
     */
    data class Error(val message: String) : AuthUiState()
}
