package com.example.kasirkita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kasirkita.ui.theme.ModulSupabaseAuthPAMTTheme
import com.example.kasirkita.navigation.AppNavigation
import com.example.kasirkita.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val isDarkMode by authViewModel.isDarkMode.collectAsState()

            ModulSupabaseAuthPAMTTheme(darkTheme = isDarkMode) {
                /*
                 * AppNavigation menjadi root utama aplikasi.
                 * Dari sini, aplikasi bisa pindah ke Login, Register, dan Dashboard.
                 */
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
