package com.example.kasirkita.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String = "",
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("cash_register_id") val cashRegisterId: String = "",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("amount_paid") val amountPaid: Double = 0.0,
    @SerialName("change_amount") val changeAmount: Double = 0.0,
    @SerialName("sold_at") val soldAt: String = "",
    @SerialName("created_by") val createdBy: String = ""
)

@Serializable
data class SaleItem(
    val id: String = "",
    @SerialName("sale_id") val saleId: String = "",
    @SerialName("product_id") val productId: String = "",
    val quantity: Double = 0.0,
    @SerialName("price_at_sale") val priceAtSale: Double = 0.0,
    val subtotal: Double = 0.0
)

@Serializable
data class SaleInsert(
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("cash_register_id") val cashRegisterId: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("amount_paid") val amountPaid: Double,
    @SerialName("change_amount") val changeAmount: Double,
    @SerialName("created_by") val createdBy: String
)

@Serializable
data class SaleItemInsert(
    @SerialName("sale_id") val saleId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Double,
    @SerialName("price_at_sale") val priceAtSale: Double,
    val subtotal: Double
)

data class CartItem(
    val product: Product,
    val quantity: Double,
    val subtotal: Double = product.price * quantity
) {
    fun toSaleItemInsert(saleId: String): SaleItemInsert {
        return SaleItemInsert(
            saleId = saleId,
            productId = product.id,
            quantity = quantity,
            priceAtSale = product.price,
            subtotal = subtotal
        )
    }
}

data class SaleDetail(
    val sale: Sale,
    val items: List<SaleItemWithProduct> = emptyList(),
    val customer: Customer? = null,
    val cashRegister: CashRegister? = null
)

data class SaleItemWithProduct(
    val saleItem: SaleItem,
    val product: Product
)
