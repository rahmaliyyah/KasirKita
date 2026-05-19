package com.example.kasirkita.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Merepresentasikan satu baris dari tabel expenses.
 */
@Serializable
data class Expense(
    val id: String = "",
    @SerialName("cash_register_id") val cashRegisterId: String = "",
    @SerialName("created_by") val createdBy: String = "",
    val date: String = "",  // ISO format TIMESTAMPTZ
    val description: String = "",
    val amount: Double = 0.0,
    val status: String = "recorded",  // "recorded" atau "cancelled"
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/**
 * Model untuk INSERT pengeluaran baru.
 * Trigger di DB otomatis:
 * 1. Kurangi saldo kas
 * 2. Buat cash_logs entry dengan type='expense'
 */
@Serializable
data class ExpenseInsert(
    @SerialName("cash_register_id") val cashRegisterId: String,
    @SerialName("created_by") val createdBy: String,
    val date: String,  // Tanggal pengeluaran
    val description: String,
    val amount: Double
)

/**
 * Model untuk UPDATE pengeluaran.
 * Hanya description yang bisa diubah jika status='recorded'.
 * Untuk cancel: ubah status menjadi 'cancelled' — trigger otomatis mengembalikan saldo.
 */
@Serializable
data class ExpenseUpdate(
    val description: String? = null,
    val status: String? = null  // Untuk "cancelled"
)
