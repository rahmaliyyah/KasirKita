package com.example.kasirkita.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KasirDashboardScreen(
    onLogoutClick: () -> Unit,
    onKelolaKasClick: () -> Unit,
    onKelolaPelangganClick: () -> Unit,
    onLihatProfilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Dashboard Kasir", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Selamat datang, Kasir!")

        Spacer(modifier = Modifier.height(32.dp))

        // ── Menu navigasi kasir ──────────────────────────────────────
        Button(
            onClick = onKelolaKasClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kelola Kas")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onKelolaPelangganClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kelola Pelanggan")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLihatProfilClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Profil Saya")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }
    }
}