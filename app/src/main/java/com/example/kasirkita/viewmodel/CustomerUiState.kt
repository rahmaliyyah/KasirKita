package com.example.kasirkita.viewmodel

import com.example.kasirkita.model.Customer
import com.example.kasirkita.model.CustomerLog

/*
 * State untuk daftar pelanggan.
 */
sealed class CustomerListUiState {
    object Loading : CustomerListUiState()
    data class Success(val customers: List<Customer>) : CustomerListUiState()
    data class Error(val message: String) : CustomerListUiState()
}

/*
 * State untuk log riwayat perubahan pelanggan.
 */
sealed class CustomerLogUiState {
    object Loading : CustomerLogUiState()
    data class Success(val logs: List<CustomerLog>) : CustomerLogUiState()
    data class Error(val message: String) : CustomerLogUiState()
}

/*
 * State untuk aksi: tambah, update pelanggan.
 */
sealed class CustomerActionState {
    object Idle : CustomerActionState()
    object Loading : CustomerActionState()
    object Success : CustomerActionState()
    data class Error(val message: String) : CustomerActionState()
}