package com.example.kasirkita.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kasirkita.ui.*
import com.example.kasirkita.ui.customer.*
import com.example.kasirkita.ui.expense.*
import com.example.kasirkita.ui.kas.*
import com.example.kasirkita.ui.product.*
import com.example.kasirkita.ui.profile.*
import com.example.kasirkita.ui.sale.*
import com.example.kasirkita.viewmodel.*
import com.example.kasirkita.ui.theme.GoldPrimary

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val authCheckState = authViewModel.authCheckState.collectAsStateWithLifecycle()
    val isRoleLoaded = authViewModel.isRoleLoaded.collectAsStateWithLifecycle()
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()
    
    val navController = rememberNavController()

    // Global ViewModels for multi-screen state
    val kasViewModel: KasViewModel = viewModel()
    val customerViewModel: CustomerViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val expenseViewModel: ExpenseViewModel = viewModel()
    val saleViewModel: SaleViewModel = viewModel()

    LaunchedEffect(authCheckState.value, isRoleLoaded.value) {
        when (authCheckState.value) {
            is AuthCheckState.Authenticated -> {
                if (isRoleLoaded.value) {
                    if (navController.currentDestination?.route == Screen.Login.route || 
                        navController.currentDestination == null) {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            is AuthCheckState.NotAuthenticated -> {
                if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavHost(
            navController = navController, 
            startDestination = if (authCheckState.value is AuthCheckState.Authenticated) Screen.Main.route else Screen.Login.route
        ) {
            // ── Auth ──────────────────────────────────────────────────────
            composable(Screen.Login.route) {
                val email = authViewModel.email.collectAsStateWithLifecycle()
                val password = authViewModel.password.collectAsStateWithLifecycle()
                LoginScreen(
                    email = email.value,
                    password = password.value,
                    uiState = uiState.value,
                    onEmailChange = authViewModel::onEmailChange,
                    onPasswordChange = authViewModel::onPasswordChange,
                    onLoginClick = { authViewModel.login() },
                    onNavigateToRegister = {}
                )
            }

            // ── Main Shell with Bottom Nav ────────────────────────────────
            composable(Screen.Main.route) {
                MainScreen(
                    authViewModel = authViewModel,
                    rootNavController = navController
                )
            }

            // ── Management Pages (Hidden Navbar) ──────────────────────────
            composable(Screen.KasList.route) {
                KasListScreen(
                    kasViewModel = kasViewModel,
                    isOwner = authViewModel.userRole.collectAsStateWithLifecycle().value == "owner",
                    onKasClick = { kas ->
                        kasViewModel.selectKas(kas)
                        navController.navigate(Screen.KasDetail.route)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.KasDetail.route) {
                KasDetailScreen(
                    kasViewModel = kasViewModel,
                    isOwner = authViewModel.userRole.collectAsStateWithLifecycle().value == "owner",
                    onBackClick = { navController.popBackStack() },
                    onSaleClick = { saleId ->
                        navController.navigate(Screen.SaleDetail.createRoute(saleId))
                    },
                    onExpenseClick = { expenseId ->
                        navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                    }
                )
            }

            composable(Screen.CustomerList.route) {
                CustomerListScreen(
                    customerViewModel = customerViewModel,
                    onCustomerClick = { customer ->
                        customerViewModel.selectCustomer(customer)
                        navController.navigate(Screen.CustomerDetail.route)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.CustomerDetail.route) {
                CustomerDetailScreen(
                    customerViewModel = customerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ProfileList.route) {
                ProfileListScreen(
                    profileViewModel = profileViewModel,
                    onProfileClick = { profile ->
                        profileViewModel.selectProfile(profile)
                        navController.navigate(Screen.ProfileDetail.route)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ProfileDetail.route) {
                ProfileDetailScreen(
                    profileViewModel = profileViewModel,
                    isOwner = authViewModel.userRole.collectAsStateWithLifecycle().value == "owner",
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ExpenseList.route) {
                ExpenseListScreen(
                    expenseViewModel = expenseViewModel,
                    kasViewModel = kasViewModel,
                    onExpenseClick = { expense ->
                        navController.navigate(Screen.ExpenseDetail.createRoute(expense.id))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ExpenseDetail.route) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
                ExpenseDetailScreen(
                    expenseViewModel = expenseViewModel,
                    expenseId = expenseId,
                    onBackClick = { navController.popBackStack() },
                    onEditSuccess = { /* Handle reload if needed */ }
                )
            }

            composable(Screen.SaleDetail.route) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId") ?: return@composable
                SaleDetailScreen(
                    saleViewModel = saleViewModel,
                    saleId = saleId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ProductDetail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                val productViewModel: ProductViewModel = viewModel()
                ProductDetailScreen(
                    productViewModel = productViewModel,
                    productId = productId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { /* No edit mode yet */ }
                )
            }

            composable(Screen.ProductForm.route) {
                val productViewModel: ProductViewModel = viewModel()
                ProductFormScreen(
                    productViewModel = productViewModel,
                    onSuccess = {
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // Overlay Loading if initial check in progress
        if (authCheckState.value is AuthCheckState.Checking || 
           (authCheckState.value is AuthCheckState.Authenticated && !isRoleLoaded.value)) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        }
    }
}
