package com.example.kasirkita.viewmodel

import com.example.kasirkita.model.InventoryLog
import com.example.kasirkita.model.Product

/**
 * State untuk daftar produk.
 */
sealed class ProductListUiState {
    object Loading : ProductListUiState()
    data class Success(val products: List<Product>) : ProductListUiState()
    data class Error(val message: String) : ProductListUiState()
}

/**
 * State untuk detail produk dengan inventory history.
 */
sealed class ProductDetailUiState {
    object Loading : ProductDetailUiState()
    data class Success(
        val product: Product,
        val inventoryLogs: List<InventoryLog> = emptyList()
    ) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

/**
 * State untuk aksi: tambah, update, deactivate produk.
 */
sealed class ProductActionState {
    object Idle : ProductActionState()
    object Loading : ProductActionState()
    object Success : ProductActionState()
    data class Error(val message: String) : ProductActionState()
}
