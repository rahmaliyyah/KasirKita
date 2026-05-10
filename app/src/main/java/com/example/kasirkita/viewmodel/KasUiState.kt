package com.example.kasirkita.viewmodel

import com.example.kasirkita.model.CashLog
import com.example.kasirkita.model.CashRegister

/*
 * State untuk halaman daftar kas.
 */
sealed class KasListUiState {
    object Loading : KasListUiState()
    data class Success(val registers: List<CashRegister>) : KasListUiState()
    data class Error(val message: String) : KasListUiState()
}

/*
 * State untuk log transaksi kas.
 */
sealed class KasLogUiState {
    object Loading : KasLogUiState()
    data class Success(val logs: List<CashLog>) : KasLogUiState()
    data class Error(val message: String) : KasLogUiState()
}

/*
 * State untuk aksi: tambah kas, update, transaksi manual.
 * Idle = belum ada aksi
 * Loading = sedang proses
 * Success = berhasil
 * Error = gagal
 */
sealed class KasActionState {
    object Idle : KasActionState()
    object Loading : KasActionState()
    object Success : KasActionState()
    data class Error(val message: String) : KasActionState()
}