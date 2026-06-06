package com.example.kasirkita.navigation

/**
 * Sealed class untuk semua route navigasi aplikasi.
 */
sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Main : Screen("main")
    object OwnerDashboard : Screen("owner_dashboard")
    object KasirDashboard : Screen("kasir_dashboard")

    // Kas
    object KasList : Screen("kas_list")
    object KasDetail : Screen("kas_detail")

    // Customer
    object CustomerList : Screen("customer_list")
    object CustomerDetail : Screen("customer_detail")

    // Profile
    object ProfileList : Screen("profile_list")
    object ProfileDetail : Screen("profile_detail")

    // Expense
    object ExpenseList : Screen("expense_list")
    object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }

    // Product
    object ProductList : Screen("product_list")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    // Sale
    object SaleList : Screen("sale_list")
    object SaleForm : Screen("sale_form")
    object SaleDetail : Screen("sale_detail/{saleId}") {
        fun createRoute(saleId: String) = "sale_detail/$saleId"
    }
}
