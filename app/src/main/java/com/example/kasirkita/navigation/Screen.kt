package com.example.kasirkita.navigation

/*
 * Sealed class untuk semua route navigasi aplikasi.
 */
sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
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
    object ExpenseForm : Screen("expense_form")
    object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }
}