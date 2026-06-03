package com.example.kasirkita.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String = "",
    @SerialName("created_by") val createdBy: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class ProductInsert(
    @SerialName("created_by") val createdBy: String,
    val name: String,
    val price: Double,
    val stock: Double = 0.0
)

@Serializable
data class ProductUpdate(
    val name: String? = null,
    val price: Double? = null,
    val stock: Double? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)

@Serializable
data class InventoryLog(
    val id: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("quantity_change") val quantityChange: Double = 0.0,
    @SerialName("stock_before") val stockBefore: Double = 0.0,
    @SerialName("stock_after") val stockAfter: Double = 0.0,
    val type: String = "",
    @SerialName("reference_id") val referenceId: String? = null,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("created_by") val createdBy: String = ""
)
