package com.example.kasirkita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasirkita.model.Expense
import com.example.kasirkita.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {
    private val expenseRepository = ExpenseRepository()

    // State untuk daftar pengeluaran
    private val _expenseListState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val expenseListState: StateFlow<ExpenseListUiState> = _expenseListState.asStateFlow()

    // State untuk aksi (tambah/edit/cancel)
    private val _actionState = MutableStateFlow<ExpenseActionState>(ExpenseActionState.Idle)
    val actionState: StateFlow<ExpenseActionState> = _actionState.asStateFlow()

    // Form input: untuk tambah/edit pengeluaran
    private val _expenseDate = MutableStateFlow("")
    val expenseDate: StateFlow<String> = _expenseDate.asStateFlow()

    private val _expenseDescription = MutableStateFlow("")
    val expenseDescription: StateFlow<String> = _expenseDescription.asStateFlow()

    private val _expenseAmount = MutableStateFlow("")
    val expenseAmount: StateFlow<String> = _expenseAmount.asStateFlow()

    private val _selectedCashRegisterId = MutableStateFlow("")
    val selectedCashRegisterId: StateFlow<String> = _selectedCashRegisterId.asStateFlow()

    // Detail pengeluaran yang sedang dilihat
    private val _selectedExpense = MutableStateFlow<Expense?>(null)
    val selectedExpense: StateFlow<Expense?> = _selectedExpense.asStateFlow()

    /*
     * Load semua pengeluaran (hanya Owner)
     */
    fun loadAllExpenses() {
        viewModelScope.launch {
            try {
                if (_expenseListState.value !is ExpenseListUiState.Success) {
                    _expenseListState.value = ExpenseListUiState.Loading
                }
                val expenses = expenseRepository.getExpenses()
                _expenseListState.value = ExpenseListUiState.Success(expenses)
            } catch (e: Exception) {
                _expenseListState.value = ExpenseListUiState.Error(e.message ?: "Error loading expenses")
            }
        }
    }

    /*
     * Load pengeluaran untuk satu kas tertentu
     */
    fun loadExpensesByCashRegister(cashRegisterId: String) {
        viewModelScope.launch {
            try {
                if (_expenseListState.value !is ExpenseListUiState.Success) {
                    _expenseListState.value = ExpenseListUiState.Loading
                }
                val expenses = expenseRepository.getExpensesByCashRegister(cashRegisterId)
                _expenseListState.value = ExpenseListUiState.Success(expenses)
            } catch (e: Exception) {
                _expenseListState.value = ExpenseListUiState.Error(e.message ?: "Error loading expenses")
            }
        }
    }

    /*
     * Load detail pengeluaran by ID
     */
    fun loadExpenseDetail(expenseId: String) {
        viewModelScope.launch {
            try {
                val expense = expenseRepository.getExpenseById(expenseId)
                _selectedExpense.value = expense
                // Populate form untuk edit
                _expenseDate.value = expense.date
                _expenseDescription.value = expense.description
                _expenseAmount.value = expense.amount.toString()
                _selectedCashRegisterId.value = expense.cashRegisterId
            } catch (e: Exception) {
                _actionState.value = ExpenseActionState.Error(e.message ?: "Error loading expense")
            }
        }
    }

    /*
     * Catat pengeluaran baru
     */
    fun createExpense() {
        val cashRegisterId = _selectedCashRegisterId.value
        val date = _expenseDate.value
        val description = _expenseDescription.value
        val amountStr = _expenseAmount.value

        // Validasi
        if (cashRegisterId.isBlank()) {
            _actionState.value = ExpenseActionState.Error("Pilih kas terlebih dahulu")
            return
        }

        if (description.isBlank()) {
            _actionState.value = ExpenseActionState.Error("Deskripsi tidak boleh kosong")
            return
        }

        // Bersihkan titik/koma jika ada (format ribuan Indonesia)
        val cleanAmountStr = amountStr.replace(".", "").replace(",", "")
        val amount = cleanAmountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _actionState.value = ExpenseActionState.Error("Jumlah harus angka positif")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = ExpenseActionState.Loading
                expenseRepository.createExpense(
                    cashRegisterId = cashRegisterId,
                    date = date,
                    description = description,
                    amount = amount
                )
                _actionState.value = ExpenseActionState.Success
                resetFormFields()
                // Reload list
                loadAllExpenses()
            } catch (e: Exception) {
                _actionState.value = ExpenseActionState.Error(e.message ?: "Error creating expense")
            }
        }
    }

    /*
     * Batalkan pengeluaran (ubah status ke 'cancelled')
     */
    fun cancelExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                _actionState.value = ExpenseActionState.Loading
                expenseRepository.cancelExpense(expenseId)
                _actionState.value = ExpenseActionState.Success
                loadAllExpenses()
            } catch (e: Exception) {
                _actionState.value = ExpenseActionState.Error(e.message ?: "Error cancelling expense")
            }
        }
    }

    /*
     * Edit deskripsi pengeluaran
     */
    fun updateExpenseDescription(expenseId: String, newDescription: String) {
        if (newDescription.isBlank()) {
            _actionState.value = ExpenseActionState.Error("Deskripsi tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = ExpenseActionState.Loading
                expenseRepository.updateExpense(expenseId, description = newDescription)
                _actionState.value = ExpenseActionState.Success
                loadExpenseDetail(expenseId)
                loadAllExpenses()
            } catch (e: Exception) {
                _actionState.value = ExpenseActionState.Error(e.message ?: "Error updating expense")
            }
        }
    }

    /*
     * Set field form untuk input
     */
    fun setExpenseDate(date: String) {
        _expenseDate.value = date
    }

    fun setExpenseDescription(description: String) {
        _expenseDescription.value = description
    }

    fun setExpenseAmount(amount: String) {
        _expenseAmount.value = amount
    }

    fun setSelectedCashRegister(cashRegisterId: String) {
        _selectedCashRegisterId.value = cashRegisterId
    }

    /*
     * Reset form fields setelah berhasil submit
     */
    fun resetFormFields() {
        _expenseDate.value = ""
        _expenseDescription.value = ""
        _expenseAmount.value = ""
        _selectedCashRegisterId.value = ""
    }

    /*
     * Reset action state ke Idle
     */
    fun resetActionState() {
        _actionState.value = ExpenseActionState.Idle
    }
}
