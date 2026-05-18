package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import com.example.kasirkita.model.Customer
import com.example.kasirkita.model.CustomerInsert
import com.example.kasirkita.model.CustomerLog
import com.example.kasirkita.model.CustomerUpdate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class CustomerRepository {

    private val supabase = SupabaseClientProvider.client

    /*
     * Ambil ID user yang sedang login.
     */
    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }

    /*
     * Ambil semua pelanggan.
     * RLS mengizinkan owner dan cashier untuk read semua.
     */
    suspend fun getCustomers(): List<Customer> {
        return supabase
            .from("customers")
            .select()
            .decodeList<Customer>()
    }

    /*
     * Ambil satu pelanggan berdasarkan id.
     */
    suspend fun getCustomerById(id: String): Customer {
        return supabase
            .from("customers")
            .select { filter { eq("id", id) } }
            .decodeSingle<Customer>()
    }

    /*
     * Tambah pelanggan baru.
     * Owner dan cashier bisa melakukan ini,
     * tapi cashier hanya boleh isi created_by miliknya sendiri (RLS).
     */
    suspend fun createCustomer(name: String, phoneNumber: String?) {
        val userId = getCurrentUserId()
        supabase
            .from("customers")
            .insert(
                CustomerInsert(
                    name = name,
                    phoneNumber = phoneNumber.takeIf { !it.isNullOrBlank() },
                    createdBy = userId
                )
            )
    }

    /*
     * Update data pelanggan.
     * Owner dan cashier bisa melakukan ini.
     */
    suspend fun updateCustomer(
        id: String,
        name: String? = null,
        phoneNumber: String? = null,
        isActive: Boolean? = null
    ) {
        supabase
            .from("customers")
            .update(
                CustomerUpdate(
                    name = name,
                    phoneNumber = phoneNumber,
                    isActive = isActive
                )
            ) {
                filter { eq("id", id) }
            }
    }

    /*
     * Ambil riwayat log perubahan untuk satu pelanggan,
     * diurutkan dari yang terbaru.
     */
    suspend fun getCustomerLogs(customerId: String): List<CustomerLog> {
        return supabase
            .from("customer_logs")
            .select {
                filter { eq("customer_id", customerId) }
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<CustomerLog>()
    }
}