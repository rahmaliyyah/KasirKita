package com.example.kasirkita.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension function untuk format Double ke Rupiah
 */
fun Double.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this)
}

/**
 * Extension function untuk format tanggal ISO ke format lokal (dd/MM/yyyy HH:mm)
 */
fun String.formatDate(): String {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = isoFormat.parse(this) ?: return this
        displayFormat.format(date)
    } catch (e: Exception) {
        this
    }
}
