package com.example.kasirkita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kasirkita.ui.theme.ModulSupabaseAuthPAMTTheme
import com.example.kasirkita.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
           ModulSupabaseAuthPAMTTheme {
                /*
                 * AppNavigation menjadi root utama aplikasi.
                 * Dari sini, aplikasi bisa pindah ke Login, Register, dan Dashboard.
                 */
                AppNavigation()
            }
        }
    }
}
