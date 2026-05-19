package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import com.example.kasirkita.model.Expense
import com.example.kasirkita.model.ExpenseInsert
import com.example.kasirkita.model.ExpenseUpdate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ExpenseRepository {

    private val supabase = SupabaseClientProvider.client

    /*
     * Ambil ID user yang sedang login.
     * Dipakai untuk mengisi field created_by.
     */
    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }

    /*
     * Ambil semua pengeluaran (hanya Owner via RLS).
     */
    suspend fun getExpenses(): List<Expense> {
        return supabase
            .from("expenses")
            .select()
            .decodeList<Expense>()
    }

    /*
     * Ambil pengeluaran untuk satu kas, diurutkan dari yang terbaru.
     */
    suspend fun getExpensesByCashRegister(cashRegisterId: String): List<Expense> {
        return supabase
            .from("expenses")
            .select {
                filter { eq("cash_register_id", cashRegisterId) }
                order("date", order = Order.DESCENDING)
            }
            .decodeList<Expense>()
    }

    /*
     * Ambil satu pengeluaran berdasarkan id.
     */
    suspend fun getExpenseById(id: String): Expense {
        return supabase
            .from("expenses")
            .select { filter { eq("id", id) } }
            .decodeSingle<Expense>()
    }

    /*
     * Catat pengeluaran baru.
     *
     * Alur:
     * 1. Hanya Owner yang bisa (dijaga RLS)
     * 2. INSERT ke expenses dengan status='recorded'
     * 3. Trigger fn_log_cash_expense di Supabase otomatis:
     *    - Kurangi current_balance di cash_registers
     *    - Buat cash_logs entry dengan type='expense' dan reference_id=expense.id
     */
    suspend fun createExpense(
        cashRegisterId: String,
        date: String,  // Format: "2024-01-15T10:30:00Z"
        description: String,
        amount: Double
    ) {
        if (amount <= 0) throw Exception("Jumlah pengeluaran harus lebih dari 0")

        val userId = getCurrentUserId()
        supabase
            .from("expenses")
            .insert(
                ExpenseInsert(
                    cashRegisterId = cashRegisterId,
                    createdBy = userId,
                    date = date,
                    description = description,
                    amount = amount
                )
            )
    }

    /*
     * Update deskripsi pengeluaran.
     * Hanya bisa update jika status='recorded'.
     * (Validasi bisa di Kotlin atau ditambahkan di RLS PostgreSQL)
     */
    suspend fun updateExpense(
        id: String,
        description: String? = null
    ) {
        supabase
            .from("expenses")
            .update(ExpenseUpdate(description = description)) {
                filter { eq("id", id) }
            }
    }

    /*
     * Batalkan pengeluaran dengan mengubah status menjadi 'cancelled'.
     *
     * Alur:
     * 1. Update status dari 'recorded' menjadi 'cancelled'
     * 2. Trigger fn_log_cash_expense_cancelled di Supabase otomatis:
     *    - Tambah kembali saldo kas (balance_before + amount)
     *    - Buat cash_logs entry tipe 'manual_in' dengan deskripsi "Pembatalan pengeluaran: ..."
     */
    suspend fun cancelExpense(id: String) {
        supabase
            .from("expenses")
            .update(ExpenseUpdate(status = "cancelled")) {
                filter { eq("id", id) }
            }
    }
}
