package com.example.kasirkita.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Merepresentasikan satu baris dari tabel cash_logs.
 * Tabel ini TIDAK pernah diupdate atau didelete — hanya INSERT.
 */
@Serializable
data class CashLog(
    val id: String = "",
    @SerialName("cash_register_id") val cashRegisterId: String = "",
    val amount: Double = 0.0,
    @SerialName("balance_before") val balanceBefore: Double = 0.0,
    @SerialName("balance_after") val balanceAfter: Double = 0.0,
    val type: String = "",        // "manual_in", "manual_out", "sale", "expense"
    @SerialName("reference_id") val referenceId: String? = null,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

/*
 * Model untuk INSERT cash_log manual (masuk/keluar manual oleh owner).
 * Tipe hanya boleh "manual_in" atau "manual_out" — sesuai RLS.
 */
@Serializable
data class CashLogInsert(
    @SerialName("cash_register_id") val cashRegisterId: String,
    val amount: Double,
    @SerialName("balance_before") val balanceBefore: Double,
    @SerialName("balance_after") val balanceAfter: Double,
    val type: String,
    val description: String?,
    @SerialName("created_by") val createdBy: String
)