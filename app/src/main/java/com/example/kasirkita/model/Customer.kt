package com.example.kasirkita.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Merepresentasikan satu baris dari tabel customers.
 */
@Serializable
data class Customer(
    val id: String = "",
    val name: String = "",
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/*
 * Model untuk INSERT pelanggan baru.
 */
@Serializable
data class CustomerInsert(
    val name: String,
    @SerialName("phone_number") val phoneNumber: String?,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_by") val createdBy: String
)

/*
 * Model untuk UPDATE pelanggan.
 * Semua field nullable — hanya field yang tidak null yang dikirim.
 */
@Serializable
data class CustomerUpdate(
    val name: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)