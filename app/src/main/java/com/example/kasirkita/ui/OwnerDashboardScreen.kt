package com.example.kasirkita.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kasirkita.viewmodel.AuthUiState

@Composable
fun OwnerDashboardScreen(
    kasirName: String,
    kasirEmail: String,
    kasirPassword: String,
    uiState: AuthUiState,
    onKasirNameChange: (String) -> Unit,
    onKasirEmailChange: (String) -> Unit,
    onKasirPasswordChange: (String) -> Unit,
    onTambahKasirClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onKelolaKasClick: () -> Unit,
    onKelolaPelangganClick: () -> Unit,
    onKelolaProfilClick: () -> Unit,
    onKelolaPengeluaranClick: () -> Unit  // ← TAMBAH INI
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Dashboard Owner",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Tambah Akun Kasir",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = kasirName,
            onValueChange = onKasirNameChange,
            label = { Text("Nama Kasir") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = kasirEmail,
            onValueChange = onKasirEmailChange,
            label = { Text("Email Kasir") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = kasirPassword,
            onValueChange = onKasirPasswordChange,
            label = { Text("Password Kasir") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onTambahKasirClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is AuthUiState.Loading
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Tambah Kasir")
            }
        }

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
        }

        if (uiState is AuthUiState.Success) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Kasir berhasil ditambahkan!", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Menu navigasi owner ──────────────────────────────────────
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
            onClick = onKelolaPengeluaranClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kelola Pengeluaran")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onKelolaProfilClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kelola Profil")
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
