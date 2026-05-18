package com.example.kasirkita.model

import kotlinx.serialization.Serializable

/*
 * Model untuk INSERT profile baru (hanya oleh owner,
 * setelah registerKasir berhasil membuat akun auth).
 */
@Serializable
data class ProfileInsert(
    val id: String,
    val name: String,
    val role: String    // "owner" atau "cashier"
)

/*
 * Model untuk UPDATE profile.
 * name dan role bersifat opsional.
 */
@Serializable
data class ProfileUpdate(
    val name: String? = null,
    val role: String? = null    // "owner" atau "cashier"
)