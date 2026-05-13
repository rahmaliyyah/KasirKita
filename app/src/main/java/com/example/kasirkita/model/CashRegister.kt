package com.example.kasirkita.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Data class ini merepresentasikan satu baris dari tabel cash_registers.
 * Nama field Kotlin (camelCase) dipetakan ke nama kolom database (snake_case)
 * menggunakan @SerialName.
 */
@Serializable
data class CashRegister(
    val id: String = "",
    val name: String = "",
    @SerialName("current_balance") val currentBalance: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("created_by") val createdBy: String = ""
)

/*
 * Model untuk INSERT kas baru.
 * Tidak perlu id (auto UUID), created_at (auto NOW()), updated_at (auto NOW()).
 */
@Serializable
data class CashRegisterInsert(
    val name: String,
    @SerialName("current_balance") val currentBalance: Double,
    @SerialName("created_by") val createdBy: String
)

/*
 * Model untuk UPDATE kas.
 * Semua field nullable — hanya field yang tidak null yang dikirim ke database.
 */
@Serializable
data class CashRegisterUpdate(
    val name: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)