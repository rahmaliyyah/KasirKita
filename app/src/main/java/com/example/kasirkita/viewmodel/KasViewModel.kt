package com.example.kasirkita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasirkita.model.CashRegister
import com.example.kasirkita.repository.KasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KasViewModel : ViewModel() {

    private val repository = KasRepository()

    // ── State untuk daftar kas ──────────────────────────────────────
    private val _kasListState = MutableStateFlow<KasListUiState>(KasListUiState.Loading)
    val kasListState: StateFlow<KasListUiState> = _kasListState

    // ── State untuk log kas ─────────────────────────────────────────
    private val _kasLogState = MutableStateFlow<KasLogUiState>(KasLogUiState.Loading)
    val kasLogState: StateFlow<KasLogUiState> = _kasLogState

    // ── State untuk hasil aksi (tambah, edit, transaksi) ────────────
    private val _actionState = MutableStateFlow<KasActionState>(KasActionState.Idle)
    val actionState: StateFlow<KasActionState> = _actionState

    // ── Kas yang sedang dibuka detailnya ────────────────────────────
    private val _selectedKas = MutableStateFlow<CashRegister?>(null)
    val selectedKas: StateFlow<CashRegister?> = _selectedKas

    // ── Input form tambah/edit kas ──────────────────────────────────
    private val _kasName = MutableStateFlow("")
    val kasName: StateFlow<String> = _kasName

    private val _kasBalance = MutableStateFlow("")
    val kasBalance: StateFlow<String> = _kasBalance

    // ── Input transaksi manual ──────────────────────────────────────
    private val _transactionAmount = MutableStateFlow("")
    val transactionAmount: StateFlow<String> = _transactionAmount

    private val _transactionDescription = MutableStateFlow("")
    val transactionDescription: StateFlow<String> = _transactionDescription


    // ── Fungsi untuk update input dari UI ──────────────────────────
    fun onKasNameChange(value: String) { _kasName.value = value }
    fun onKasBalanceChange(value: String) { _kasBalance.value = value }
    fun onTransactionAmountChange(value: String) { _transactionAmount.value = value }
    fun onTransactionDescriptionChange(value: String) { _transactionDescription.value = value }

    fun resetActionState() { _actionState.value = KasActionState.Idle }

    fun resetKasForm() {
        _kasName.value = ""
        _kasBalance.value = ""
    }

    fun resetTransactionForm() {
        _transactionAmount.value = ""
        _transactionDescription.value = ""
    }

    // ── Set kas yang sedang dipilih ─────────────────────────────────
    fun selectKas(kas: CashRegister) {
        _selectedKas.value = kas
    }
    private fun refreshSelectedKas(updatedList: List<CashRegister>) {
        val currentId = _selectedKas.value?.id ?: return
        _selectedKas.value = updatedList.find { it.id == currentId }
    }

    // ── Load semua kas ──────────────────────────────────────────────
    fun loadKasRegisters() {
        viewModelScope.launch {
            try {
                if (_kasListState.value !is KasListUiState.Success) {
                    _kasListState.value = KasListUiState.Loading
                }
                val list = repository.getCashRegisters()
                _kasListState.value = KasListUiState.Success(list)
                refreshSelectedKas(list) // ← tambah baris ini
            } catch (e: Exception) {
                _kasListState.value = KasListUiState.Error(
                    e.message ?: "Gagal memuat data kas"
                )
            }
        }
    }

    // ── Load log transaksi untuk kas yang dipilih ───────────────────
    fun loadKasLogs(cashRegisterId: String) {
        viewModelScope.launch {
            try {
                _kasLogState.value = KasLogUiState.Loading
                val logs = repository.getCashLogs(cashRegisterId)
                _kasLogState.value = KasLogUiState.Success(logs)
            } catch (e: Exception) {
                _kasLogState.value = KasLogUiState.Error(
                    e.message ?: "Gagal memuat log kas"
                )
            }
        }
    }

    // ── Buat kas baru ───────────────────────────────────────────────
    fun createKas() {
        val name = _kasName.value.trim()
        val balance = _kasBalance.value.toDoubleOrNull() ?: 0.0

        if (name.isEmpty()) {
            _actionState.value = KasActionState.Error("Nama kas tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = KasActionState.Loading
                repository.createCashRegister(name, balance)
                _actionState.value = KasActionState.Success
                resetKasForm()
                loadKasRegisters()
            } catch (e: Exception) {
                _actionState.value = KasActionState.Error(
                    e.message ?: "Gagal membuat kas"
                )
            }
        }
    }

    // ── Update nama kas ─────────────────────────────────────────────
    fun updateKasName(id: String) {
        val name = _kasName.value.trim()
        if (name.isEmpty()) {
            _actionState.value = KasActionState.Error("Nama kas tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            try {
                _actionState.value = KasActionState.Loading
                repository.updateCashRegister(id = id, name = name)
                _actionState.value = KasActionState.Success
                resetKasForm()
                loadKasRegisters()
            } catch (e: Exception) {
                _actionState.value = KasActionState.Error(
                    e.message ?: "Gagal memperbarui nama kas"
                )
            }
        }
    }

    // ── Aktifkan / nonaktifkan kas ──────────────────────────────────
    fun toggleKasActive(id: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                _actionState.value = KasActionState.Loading
                repository.updateCashRegister(id = id, isActive = !currentStatus)
                _actionState.value = KasActionState.Success
                loadKasRegisters()
            } catch (e: Exception) {
                _actionState.value = KasActionState.Error(
                    e.message ?: "Gagal mengubah status kas"
                )
            }
        }
    }

    // ── Transaksi manual masuk atau keluar ──────────────────────────
    // type: "manual_in" untuk tambah saldo, "manual_out" untuk kurangi
    fun manualTransaction(type: String) {
        val amount = _transactionAmount.value.toDoubleOrNull()
        val kas = _selectedKas.value

        if (amount == null || amount <= 0) {
            _actionState.value = KasActionState.Error("Jumlah harus lebih dari 0")
            return
        }
        if (kas == null) {
            _actionState.value = KasActionState.Error("Kas tidak ditemukan")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = KasActionState.Loading

                // Ambil saldo terbaru dari DB, bukan dari state yang mungkin basi
                val freshKas = repository.getCashRegisterById(kas.id)

                repository.manualTransaction(
                    cashRegisterId = freshKas.id,
                    amount = amount,
                    type = type,
                    description = _transactionDescription.value.ifBlank { null },
                    currentBalance = freshKas.currentBalance // ← pakai saldo fresh
                )
                _actionState.value = KasActionState.Success
                resetTransactionForm()
                loadKasRegisters()
                loadKasLogs(kas.id)
            } catch (e: Exception) {
                _actionState.value = KasActionState.Error(
                    e.message ?: "Transaksi gagal"
                )
            }
        }
    }
}