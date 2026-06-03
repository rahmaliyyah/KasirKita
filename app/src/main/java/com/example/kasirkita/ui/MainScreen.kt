package com.example.kasirkita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kasirkita.navigation.Screen
import com.example.kasirkita.ui.theme.GoldPrimary
import com.example.kasirkita.ui.theme.TextSecondary
import com.example.kasirkita.viewmodel.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kasirkita.ui.product.ProductListScreen
import com.example.kasirkita.ui.sale.SaleFormScreen
import com.example.kasirkita.ui.sale.SaleListScreen
import com.example.kasirkita.ui.profile.MyProfileScreen

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    rootNavController: androidx.navigation.NavHostController
) {
    val navController = rememberNavController()
    val userRole by authViewModel.userRole.collectAsState()

    val items = listOf(
        NavigationItem("Beranda", Screen.OwnerDashboard.route, Icons.Default.Home, Icons.Outlined.Home),
        NavigationItem("Stok", Screen.ProductList.route, Icons.Default.Inventory2, Icons.Outlined.Inventory2),
        NavigationItem("Kasir", Screen.SaleForm.route, Icons.Default.PointOfSale, Icons.Outlined.PointOfSale),
        NavigationItem("Transaksi", Screen.SaleList.route, Icons.Default.Assessment, Icons.Outlined.Assessment),
        NavigationItem("Profil", "my_profile", Icons.Default.Person, Icons.Outlined.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost mengisi seluruh layar (background bisa tembus bawah navbar)
        // Tiap screen akan mengatur padding-nya sendiri sesuai kebutuhan
        NavHost(
            navController = navController,
            startDestination = Screen.OwnerDashboard.route,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            composable(Screen.OwnerDashboard.route) {
                val userName by authViewModel.userName.collectAsState()
                val userEmail by authViewModel.email.collectAsState()
                val kasirName by authViewModel.kasirName.collectAsState()
                val kasirEmail by authViewModel.kasirEmail.collectAsState()
                val kasirPassword by authViewModel.kasirPassword.collectAsState()
                val authUiState by authViewModel.uiState.collectAsState()

                val kasViewModel: KasViewModel = viewModel()
                val saleViewModel: SaleViewModel = viewModel()
                val expenseViewModel: ExpenseViewModel = viewModel()

                OwnerDashboardScreen(
                    userName = userName ?: "Owner",
                    userEmail = userEmail,
                    kasirName = kasirName,
                    kasirEmail = kasirEmail,
                    kasirPassword = kasirPassword,
                    uiState = authUiState,
                    kasViewModel = kasViewModel,
                    saleViewModel = saleViewModel,
                    expenseViewModel = expenseViewModel,
                    onKasirNameChange = authViewModel::onKasirNameChange,
                    onKasirEmailChange = authViewModel::onKasirEmailChange,
                    onKasirPasswordChange = authViewModel::onKasirPasswordChange,
                    onTambahKasirClick = authViewModel::registerKasir,
                    onLogoutClick = {
                        authViewModel.logout()
                        rootNavController.navigate(Screen.Login.route) { popUpTo(0) }
                    },
                    onKelolaKasClick = { rootNavController.navigate(Screen.KasList.route) },
                    onKelolaPelangganClick = { rootNavController.navigate(Screen.CustomerList.route) },
                    onKelolaKaryawanClick = { rootNavController.navigate(Screen.ProfileList.route) },
                    onKelolaProfilClick = { navController.navigate("my_profile") },
                    onKelolaPengeluaranClick = { rootNavController.navigate(Screen.ExpenseList.route) },
                    onLihatSemuaTransaksiClick = {
                        navController.navigate(Screen.SaleList.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSaleClick = { saleId -> 
                        rootNavController.navigate(Screen.SaleDetail.createRoute(saleId))
                    },
                    onExpenseClick = { expenseId ->
                        rootNavController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                    },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }

            composable(Screen.ProductList.route) {
                val productViewModel: ProductViewModel = viewModel()
                ProductListScreen(
                    productViewModel = productViewModel,
                    onProductClick = { product ->
                        rootNavController.navigate(Screen.ProductDetail.createRoute(product.id))
                    },
                    onAddProductClick = { rootNavController.navigate(Screen.ProductForm.route) },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }

            composable(Screen.SaleForm.route) {
                val saleViewModel: SaleViewModel = viewModel()
                val productViewModel: ProductViewModel = viewModel()
                val customerViewModel: CustomerViewModel = viewModel()
                val kasViewModel: KasViewModel = viewModel()
                SaleFormScreen(
                    saleViewModel = saleViewModel,
                    productViewModel = productViewModel,
                    customerViewModel = customerViewModel,
                    kasViewModel = kasViewModel,
                    onSuccess = { _ -> 
                        // Pindah ke tab transaksi setelah sukses
                        navController.navigate(Screen.SaleList.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }

            composable(Screen.SaleList.route) {
                val saleViewModel: SaleViewModel = viewModel()
                val expenseViewModel: ExpenseViewModel = viewModel()
                SaleListScreen(
                    saleViewModel = saleViewModel,
                    expenseViewModel = expenseViewModel,
                    onSaleClick = { saleId ->
                        rootNavController.navigate(Screen.SaleDetail.createRoute(saleId))
                    },
                    onExpenseClick = { expenseId ->
                        rootNavController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                    },
                    onAddSaleClick = { navController.navigate(Screen.SaleForm.route) },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }

            composable("my_profile") {
                MyProfileScreen(
                    authViewModel = authViewModel,
                    onLogoutClick = {
                        authViewModel.logout()
                        rootNavController.navigate(Screen.Login.route) { popUpTo(0) }
                    },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
