package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import com.example.kasirkita.model.Sale
import com.example.kasirkita.model.SaleInsert
import com.example.kasirkita.model.SaleItem
import com.example.kasirkita.model.SaleItemInsert
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SaleRepository {

    private val supabase = SupabaseClientProvider.client

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }

    /**
     * Ambil semua penjualan.
     * Hanya Owner yang bisa akses (RLS).
     */
    suspend fun getAllSales(): List<Sale> {
        return supabase
            .from("sales")
            .select {
                order("sold_at", order = Order.DESCENDING)
            }
            .decodeList<Sale>()
    }

    /**
     * Ambil penjualan untuk user tertentu (cashier melihat transaksinya sendiri).
     */
    suspend fun getSalesByUser(userId: String): List<Sale> {
        return supabase
            .from("sales")
            .select {
                filter { eq("created_by", userId) }
                order("sold_at", order = Order.DESCENDING)
            }
            .decodeList<Sale>()
    }

    /**
     * Ambil penjualan untuk satu kas.
     */
    suspend fun getSalesByCashRegister(cashRegisterId: String): List<Sale> {
        return supabase
            .from("sales")
            .select {
                filter { eq("cash_register_id", cashRegisterId) }
                order("sold_at", order = Order.DESCENDING)
            }
            .decodeList<Sale>()
    }

    /**
     * Ambil satu penjualan berdasarkan id.
     */
    suspend fun getSaleById(id: String): Sale {
        return supabase
            .from("sales")
            .select { filter { eq("id", id) } }
            .decodeSingle<Sale>()
    }

    /**
     * Ambil sale items untuk satu penjualan.
     */
    suspend fun getSaleItems(saleId: String): List<SaleItem> {
        return supabase
            .from("sale_items")
            .select {
                filter { eq("sale_id", saleId) }
            }
            .decodeList<SaleItem>()
    }

    /**
     * Catat penjualan baru.
     *
     * Alur:
     * 1. INSERT ke sales dengan data transaksi
     * 2. Trigger fn_log_cash_sale otomatis:
     *    - Tambah current_balance di cash_registers
     *    - Buat cash_logs entry dengan type='sale' dan reference_id=sale.id
     * 3. Kemudian INSERT sale_items
     * 4. Trigger fn_log_inventory_sold otomatis untuk setiap item:
     *    - Kurangi stok produk
     *    - Buat inventory_logs entry dengan type='sold'
     *
     * Return: Sale ID yang baru dibuat
     */
    suspend fun createSale(
        customerId: String? = null,
        cashRegisterId: String,
        totalAmount: Double,
        amountPaid: Double,
        changeAmount: Double,
        items: List<SaleItemInsert>
    ): String {
        if (totalAmount <= 0) throw Exception("Total harus lebih dari 0")
        if (amountPaid < totalAmount) throw Exception("Jumlah pembayaran kurang dari total")

        val userId = getCurrentUserId()

        // 1. Insert sale
        val saleResponse = supabase
            .from("sales")
            .insert(
                SaleInsert(
                    customerId = customerId,
                    cashRegisterId = cashRegisterId,
                    totalAmount = totalAmount,
                    amountPaid = amountPaid,
                    changeAmount = changeAmount,
                    createdBy = userId
                )
            ) {
                select()
            }
        
        val saleId = saleResponse.decodeList<Sale>().first().id

        // 2. Insert sale items
        val itemsWithSaleId = items.map {
            it.copy(saleId = saleId)
        }
        supabase
            .from("sale_items")
            .insert(itemsWithSaleId)

        return saleId
    }

    /**
     * Ambil penjualan untuk satu customer.
     */
    suspend fun getSalesByCustomer(customerId: String): List<Sale> {
        return supabase
            .from("sales")
            .select {
                filter { eq("customer_id", customerId) }
                order("sold_at", order = Order.DESCENDING)
            }
            .decodeList<Sale>()
    }
}
