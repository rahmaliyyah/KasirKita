// File: ErrorMessageMapper.kt (Baru)
package com.example.kasirkita.ui.utils

object ErrorMessageMapper {
    /**
     * Map error message panjang dari backend ke pesan user-friendly yang singkat
     */
    fun mapErrorMessage(originalMessage: String): String {
        return when {
            // Email validation errors
            originalMessage.contains("email", ignoreCase = true) &&
                    originalMessage.contains("format", ignoreCase = true) -> {
                "Format email tidak valid"
            }
            originalMessage.contains("email", ignoreCase = true) &&
                    originalMessage.contains("already", ignoreCase = true) -> {
                "Email sudah terdaftar"
            }

            // Password errors
            originalMessage.contains("password", ignoreCase = true) &&
                    originalMessage.contains("weak", ignoreCase = true) -> {
                "Password terlalu lemah. Gunakan minimal 6 karakter"
            }
            originalMessage.contains("password", ignoreCase = true) &&
                    originalMessage.contains("short", ignoreCase = true) -> {
                "Password terlalu pendek"
            }
            originalMessage.contains("invalid", ignoreCase = true) &&
                    originalMessage.contains("password", ignoreCase = true) -> {
                "Email atau password salah"
            }

            // Auth errors
            originalMessage.contains("Invalid login credentials", ignoreCase = true) -> {
                "Email atau password salah"
            }
            originalMessage.contains("User not found", ignoreCase = true) -> {
                "Akun tidak ditemukan"
            }
            originalMessage.contains("Authentication failed", ignoreCase = true) -> {
                "Gagal masuk. Coba lagi"
            }
            originalMessage.contains("invalid_request", ignoreCase = true) -> {
                "Permintaan tidak valid"
            }
            originalMessage.contains("conflict", ignoreCase = true) -> {
                "Email sudah terdaftar"
            }

            // Network errors
            originalMessage.contains("Network", ignoreCase = true) ||
                    originalMessage.contains("timeout", ignoreCase = true) -> {
                "Koneksi internet bermasalah"
            }

            // Default case - tampilkan pesan singkat generik
            else -> "Terjadi kesalahan. Silakan coba lagi"
        }
    }
}
