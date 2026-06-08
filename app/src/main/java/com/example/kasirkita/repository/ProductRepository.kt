package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import com.example.kasirkita.model.InventoryLog
import com.example.kasirkita.model.Product
import com.example.kasirkita.model.ProductInsert
import com.example.kasirkita.model.ProductUpdate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryLogInsert(
    @SerialName("product_id") val productId: String,
    @SerialName("quantity_change") val quantityChange: Double,
    @SerialName("stock_before") val stockBefore: Double,
    @SerialName("stock_after") val stockAfter: Double,
    val type: String,
    @SerialName("reference_id") val referenceId: String? = null,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String
)

class ProductRepository {

    private val supabase = SupabaseClientProvider.client

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }

    suspend fun getActiveProducts(): List<Product> {
        return supabase
            .from("products")
            .select {
                filter { eq("is_active", true) }
                order("name", order = Order.ASCENDING)
            }
            .decodeList<Product>()
    }

    suspend fun getAllProducts(): List<Product> {
        return supabase
            .from("products")
            .select {
                order("name", order = Order.ASCENDING)
            }
            .decodeList<Product>()
    }

    suspend fun getProductById(id: String): Product {
        return supabase
            .from("products")
            .select { filter { eq("id", id) } }
            .decodeSingle<Product>()
    }

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
     * Update stok produk sekaligus mencatat log penyesuaian manual.
     */
    suspend fun adjustStockWithLog(
        productId: String,
        stockBefore: Double,
        newStock: Double,
        amount: Double
    ) {
        val userId = getCurrentUserId()

        // Update stok produk
        supabase
            .from("products")
            .update(ProductUpdate(stock = newStock)) {
                filter { eq("id", productId) }
            }

        // Catat log penyesuaian stok manual
        val description = if (amount > 0) "Penambahan stok manual" else "Pengurangan stok manual"
        supabase
            .from("inventory_logs")
            .insert(
                InventoryLogInsert(
                    productId = productId,
                    quantityChange = amount,
                    stockBefore = stockBefore,
                    stockAfter = newStock,
                    type = "adjustment",
                    description = description,
                    createdBy = userId
                )
            )
    }

    /**
     * Update status aktif/nonaktif produk.
     * Hanya update kolom is_active, tidak mencatat ke inventory_logs
     * karena perubahan status bukan pergerakan stok.
     */
    suspend fun toggleProductActive(id: String, isActive: Boolean) {
        supabase
            .from("products")
            .update(mapOf("is_active" to isActive)) {
                filter { eq("id", id) }
            }
    }

    suspend fun getInventoryLogsByProduct(productId: String): List<InventoryLog> {
        return supabase
            .from("inventory_logs")
            .select {
                filter { eq("product_id", productId) }
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<InventoryLog>()
    }

    suspend fun getAllInventoryLogs(): List<InventoryLog> {
        return supabase
            .from("inventory_logs")
            .select {
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<InventoryLog>()
    }
}