package com.example.kasirkita.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/*
 * Merepresentasikan satu baris dari tabel customer_logs.
 * Tabel ini hanya di-INSERT oleh trigger — tidak ada update/delete.
 * changed_fields berisi JSON dengan detail before/after perubahan.
 */
@Serializable
data class CustomerLog(
    val id: String = "",
    @SerialName("customer_id") val customerId: String = "",
    val type: String = "",          // "created" atau "updated"
    @SerialName("changed_fields") val changedFields: JsonElement? = null,
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = ""
)