package com.example.kasirkita.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kasirkita.ui.theme.KasirDashboardScreen
import com.example.kasirkita.ui.theme.LoginScreen
import com.example.kasirkita.ui.theme.OwnerDashboardScreen
import com.example.kasirkita.viewmodel.AuthCheckState
import com.example.kasirkita.viewmodel.AuthUiState
import com.example.kasirkita.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val authCheckState = authViewModel.authCheckState.collectAsStateWithLifecycle()
    val userRole = authViewModel.userRole.collectAsStateWithLifecycle()
    val isRoleLoaded = authViewModel.isRoleLoaded.collectAsStateWithLifecycle()

    when (authCheckState.value) {
        is AuthCheckState.Checking -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthCheckState.Authenticated -> {
            // Tunggu sampai role selesai di-fetch
            if (!isRoleLoaded.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val startDestination = if (userRole.value == "owner") {
                    Screen.OwnerDashboard.route
                } else {
                    Screen.KasirDashboard.route
                }
                MainNavHost(authViewModel = authViewModel, startDestination = startDestination)
            }
        }
        is AuthCheckState.NotAuthenticated -> {
            MainNavHost(authViewModel = authViewModel, startDestination = Screen.Login.route)
        }
    }
}

@Composable
fun MainNavHost(
    authViewModel: AuthViewModel,
    startDestination: String
) {
    val navController = rememberNavController()
    val email = authViewModel.email.collectAsStateWithLifecycle()
    val password = authViewModel.password.collectAsStateWithLifecycle()
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()
    val kasirEmail = authViewModel.kasirEmail.collectAsStateWithLifecycle()
    val kasirPassword = authViewModel.kasirPassword.collectAsStateWithLifecycle()
    val kasirName = authViewModel.kasirName.collectAsStateWithLifecycle()
    val userRole = authViewModel.userRole.collectAsStateWithLifecycle()
    val isRoleLoaded = authViewModel.isRoleLoaded.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.value, isRoleLoaded.value) {
        if (uiState.value is AuthUiState.Success && isRoleLoaded.value) {
            val destination = if (userRole.value == "owner") {
                Screen.OwnerDashboard.route
            } else {
                Screen.KasirDashboard.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            authViewModel.resetState()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
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

        composable(Screen.OwnerDashboard.route) {
            OwnerDashboardScreen(
                kasirName = kasirName.value,
                kasirEmail = kasirEmail.value,
                kasirPassword = kasirPassword.value,
                uiState = uiState.value,
                onKasirNameChange = authViewModel::onKasirNameChange,
                onKasirEmailChange = authViewModel::onKasirEmailChange,
                onKasirPasswordChange = authViewModel::onKasirPasswordChange,
                onTambahKasirClick = { authViewModel.registerKasir() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.OwnerDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.KasirDashboard.route) {
            KasirDashboardScreen(
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.KasirDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}