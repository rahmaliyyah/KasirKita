package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import com.example.kasirkita.model.CashLog
import com.example.kasirkita.model.CashLogInsert
import com.example.kasirkita.model.CashRegister
import com.example.kasirkita.model.CashRegisterInsert
import com.example.kasirkita.model.CashRegisterUpdate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class KasRepository {

    private val supabase = SupabaseClientProvider.client

    /*
     * Ambil ID user yang sedang login.
     * Dipakai untuk mengisi field created_by saat INSERT.
     */
    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }

    /*
     * Ambil semua kas.
     * RLS otomatis membatasi:
     *   - Owner: lihat semua kas
     *   - Kasir: hanya kas yang is_active = true
     */
    suspend fun getCashRegisters(): List<CashRegister> {
        return supabase
            .from("cash_registers")
            .select()
            .decodeList<CashRegister>()
    }

    /*
     * Buat kas baru.
     * Hanya owner yang bisa (dijaga oleh RLS di Supabase).
     */
    suspend fun createCashRegister(name: String, initialBalance: Double) {
        val userId = getCurrentUserId()
        supabase
            .from("cash_registers")
            .insert(
                CashRegisterInsert(
                    name = name,
                    currentBalance = initialBalance,
                    createdBy = userId
                )
            )
    }

    /*
     * Update nama atau status aktif kas.
     * Hanya owner yang bisa (dijaga oleh RLS).
     */
    suspend fun updateCashRegister(
        id: String,
        name: String? = null,
        isActive: Boolean? = null
    ) {
        supabase
            .from("cash_registers")
            .update(CashRegisterUpdate(name = name, isActive = isActive)) {
                filter { eq("id", id) }
            }
    }

    /*
     * Ambil log transaksi untuk satu kas, diurutkan dari yang terbaru.
     */
    suspend fun getCashLogs(cashRegisterId: String): List<CashLog> {
        return supabase
            .from("cash_logs")
            .select {
                filter { eq("cash_register_id", cashRegisterId) }
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<CashLog>()
    }

    /*
     * Transaksi manual masuk atau keluar.
     *
     * Alur:
     * 1. Kita hitung balance_before dan balance_after
     * 2. INSERT ke cash_logs
     * 3. Trigger fn_update_balance_manual di Supabase otomatis
     *    mengupdate current_balance di cash_registers
     *
     * Parameter type: "manual_in" atau "manual_out"
     */
    suspend fun manualTransaction(
        cashRegisterId: String,
        amount: Double,
        type: String,
        description: String?,
        currentBalance: Double
    ) {
        if (amount <= 0) throw Exception("Jumlah harus lebih dari 0")

        val balanceBefore = currentBalance
        val balanceAfter = if (type == "manual_in") {
            currentBalance + amount
        } else {
            if (currentBalance - amount < 0) throw Exception("Saldo tidak mencukupi")
            currentBalance - amount
        }

        val userId = getCurrentUserId()

        supabase
            .from("cash_logs")
            .insert(
                CashLogInsert(
                    cashRegisterId = cashRegisterId,
                    amount = amount,
                    balanceBefore = balanceBefore,
                    balanceAfter = balanceAfter,
                    type = type,
                    description = description,
                    createdBy = userId
                )
            )
    }

    // Ambil satu kas berdasarkan id untuk dapat saldo terbaru
    suspend fun getCashRegisterById(id: String): CashRegister {
        return supabase
            .from("cash_registers")
            .select { filter { eq("id", id) } }
            .decodeSingle<CashRegister>()
    }
}