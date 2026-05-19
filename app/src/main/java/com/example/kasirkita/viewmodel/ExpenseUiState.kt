package com.example.kasirkita.viewmodel

import com.example.kasirkita.model.Expense

/*
 * State untuk daftar pengeluaran.
 */
sealed class ExpenseListUiState {
    object Loading : ExpenseListUiState()
    data class Success(val expenses: List<Expense>) : ExpenseListUiState()
    data class Error(val message: String) : ExpenseListUiState()
}

/*
 * State untuk aksi: tambah, update, batalkan pengeluaran.
 */
sealed class ExpenseActionState {
    object Idle : ExpenseActionState()
    object Loading : ExpenseActionState()
    object Success : ExpenseActionState()
    data class Error(val message: String) : ExpenseActionState()
}
