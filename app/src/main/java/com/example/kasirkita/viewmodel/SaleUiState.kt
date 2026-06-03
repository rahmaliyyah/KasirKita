package com.example.kasirkita.viewmodel

import com.example.kasirkita.model.Sale
import com.example.kasirkita.model.SaleItemWithProduct

/**
 * State untuk daftar penjualan.
 */
sealed class SaleListUiState {
    object Loading : SaleListUiState()
    data class Success(val sales: List<Sale>) : SaleListUiState()
    data class Error(val message: String) : SaleListUiState()
}

/**
 * State untuk detail penjualan dengan items.
 */
sealed class SaleDetailUiState {
    object Loading : SaleDetailUiState()
    data class Success(
        val sale: Sale,
        val items: List<SaleItemWithProduct> = emptyList()
    ) : SaleDetailUiState()
    data class Error(val message: String) : SaleDetailUiState()
}

/**
 * State untuk proses transaksi penjualan (checkout).
 */
sealed class SaleTransactionUiState {
    object Idle : SaleTransactionUiState()
    object Loading : SaleTransactionUiState()
    data class Success(val saleId: String) : SaleTransactionUiState()
    data class Error(val message: String) : SaleTransactionUiState()
}
