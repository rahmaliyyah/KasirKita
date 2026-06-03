package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import com.example.kasirkita.model.InventoryLog
import com.example.kasirkita.model.Product
import com.example.kasirkita.model.ProductInsert
import com.example.kasirkita.model.ProductUpdate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ProductRepository {

    private val supabase = SupabaseClientProvider.client

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }

    /**
     * Ambil semua produk yang aktif.
     * Owner bisa lihat semua produk.
     * Cashier hanya bisa lihat produk yang is_active=true.
     */
    suspend fun getActiveProducts(): List<Product> {
        return supabase
            .from("products")
            .select {
                filter { eq("is_active", true) }
                order("name", order = Order.ASCENDING)
            }
            .decodeList<Product>()
    }

    /**
     * Ambil semua produk (Owner only - RLS).
     */
    suspend fun getAllProducts(): List<Product> {
        return supabase
            .from("products")
            .select {
                order("name", order = Order.ASCENDING)
            }
            .decodeList<Product>()
    }

    /**
     * Ambil satu produk berdasarkan id.
     */
    suspend fun getProductById(id: String): Product {
        return supabase
            .from("products")
            .select { filter { eq("id", id) } }
            .decodeSingle<Product>()
    }

    /**
     * Tambah produk baru.
     * Hanya Owner yang bisa (dijaga RLS).
     */
    suspend fun createProduct(
        name: String,
        price: Double,
        stock: Double = 0.0
    ) {
        if (name.isBlank()) throw Exception("Nama produk tidak boleh kosong")
        if (price <= 0) throw Exception("Harga harus lebih dari 0")
        if (stock < 0) throw Exception("Stok tidak boleh negatif")

        val userId = getCurrentUserId()
        supabase
            .from("products")
            .insert(
                ProductInsert(
                    createdBy = userId,
                    name = name,
                    price = price,
                    stock = stock
                )
            )
    }

    /**
     * Update produk (nama, harga, stok, status aktif).
     * Hanya Owner yang bisa (dijaga RLS).
     */
    suspend fun updateProduct(
        id: String,
        name: String? = null,
        price: Double? = null,
        stock: Double? = null,
        isActive: Boolean? = null
    ) {
        supabase
            .from("products")
            .update(
                ProductUpdate(
                    name = name,
                    price = price,
                    stock = stock,
                    isActive = isActive
                )
            ) {
                filter { eq("id", id) }
            }
    }

    /**
     * Update status aktif/nonaktif produk.
     */
    suspend fun toggleProductActive(id: String, isActive: Boolean) {
        supabase
            .from("products")
            .update(mapOf("is_active" to isActive)) {
                filter { eq("id", id) }
            }
    }

    /**
     * Ambil inventory logs untuk satu produk.
     * Diurutkan dari yang terbaru.
     */
    suspend fun getInventoryLogsByProduct(productId: String): List<InventoryLog> {
        return supabase
            .from("inventory_logs")
            .select {
                filter { eq("product_id", productId) }
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<InventoryLog>()
    }

    /**
     * Ambil semua inventory logs.
     * Untuk report keperluan owner.
     */
    suspend fun getAllInventoryLogs(): List<InventoryLog> {
        return supabase
            .from("inventory_logs")
            .select {
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<InventoryLog>()
    }
}
