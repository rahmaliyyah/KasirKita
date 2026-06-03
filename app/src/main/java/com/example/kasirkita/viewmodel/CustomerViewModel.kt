package com.example.kasirkita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasirkita.model.Customer
import com.example.kasirkita.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerViewModel : ViewModel() {

    private val repository = CustomerRepository()

    // ── State untuk daftar pelanggan ────────────────────────────────
    private val _customerListState = MutableStateFlow<CustomerListUiState>(CustomerListUiState.Loading)
    val customerListState: StateFlow<CustomerListUiState> = _customerListState

    // ── State untuk log riwayat pelanggan ───────────────────────────
    private val _customerLogState = MutableStateFlow<CustomerLogUiState>(CustomerLogUiState.Loading)
    val customerLogState: StateFlow<CustomerLogUiState> = _customerLogState

    // ── State untuk aksi (tambah, update) ──────────────────────────
    private val _actionState = MutableStateFlow<CustomerActionState>(CustomerActionState.Idle)
    val actionState: StateFlow<CustomerActionState> = _actionState

    // ── Pelanggan yang sedang dibuka detailnya ──────────────────────
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    // ── Input form tambah / edit pelanggan ──────────────────────────
    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone

    // ── Fungsi update input dari UI ─────────────────────────────────
    fun onCustomerNameChange(value: String) { _customerName.value = value }
    fun onCustomerPhoneChange(value: String) { _customerPhone.value = value }

    fun resetActionState() { _actionState.value = CustomerActionState.Idle }

    fun resetCustomerForm() {
        _customerName.value = ""
        _customerPhone.value = ""
    }

    // ── Set pelanggan yang dipilih ──────────────────────────────────
    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
    }

    // Refresh selected customer setelah list diperbarui
    private fun refreshSelectedCustomer(updatedList: List<Customer>) {
        val currentId = _selectedCustomer.value?.id ?: return
        _selectedCustomer.value = updatedList.find { it.id == currentId }
    }

    // ── Load semua pelanggan ────────────────────────────────────────
    fun loadCustomers() {
        viewModelScope.launch {
            try {
                if (_customerListState.value !is CustomerListUiState.Success) {
                    _customerListState.value = CustomerListUiState.Loading
                }
                val list = repository.getCustomers()
                _customerListState.value = CustomerListUiState.Success(list)
                refreshSelectedCustomer(list)
            } catch (e: Exception) {
                _customerListState.value = CustomerListUiState.Error(
                    e.message ?: "Gagal memuat data pelanggan"
                )
            }
        }
    }

    // ── Load log riwayat untuk pelanggan yang dipilih ───────────────
    fun loadCustomerLogs(customerId: String) {
        viewModelScope.launch {
            try {
                _customerLogState.value = CustomerLogUiState.Loading
                val logs = repository.getCustomerLogs(customerId)
                _customerLogState.value = CustomerLogUiState.Success(logs)
            } catch (e: Exception) {
                _customerLogState.value = CustomerLogUiState.Error(
                    e.message ?: "Gagal memuat riwayat pelanggan"
                )
            }
        }
    }

    // ── Tambah pelanggan baru ───────────────────────────────────────
    fun createCustomer() {
        val name = _customerName.value.trim()
        if (name.isEmpty()) {
            _actionState.value = CustomerActionState.Error("Nama pelanggan tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = CustomerActionState.Loading
                repository.createCustomer(
                    name = name,
                    phoneNumber = _customerPhone.value.trim().takeIf { it.isNotBlank() }
                )
                _actionState.value = CustomerActionState.Success
                resetCustomerForm()
                loadCustomers()
            } catch (e: Exception) {
                _actionState.value = CustomerActionState.Error(
                    e.message ?: "Gagal menambah pelanggan"
                )
            }
        }
    }

    // ── Update nama / nomor HP pelanggan ────────────────────────────
    fun updateCustomer(id: String) {
        val name = _customerName.value.trim()
        if (name.isEmpty()) {
            _actionState.value = CustomerActionState.Error("Nama pelanggan tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = CustomerActionState.Loading
                repository.updateCustomer(
                    id = id,
                    name = name,
                    phoneNumber = _customerPhone.value.trim().takeIf { it.isNotBlank() }
                )
                _actionState.value = CustomerActionState.Success
                resetCustomerForm()
                loadCustomers()
            } catch (e: Exception) {
                _actionState.value = CustomerActionState.Error(
                    e.message ?: "Gagal memperbarui pelanggan"
                )
            }
        }
    }

    // ── Aktifkan / nonaktifkan pelanggan ────────────────────────────
    fun toggleCustomerActive(id: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                _actionState.value = CustomerActionState.Loading
                repository.updateCustomer(id = id, isActive = !currentStatus)
                _actionState.value = CustomerActionState.Success
                loadCustomers()
            } catch (e: Exception) {
                _actionState.value = CustomerActionState.Error(
                    e.message ?: "Gagal mengubah status pelanggan"
                )
            }
        }
    }
}