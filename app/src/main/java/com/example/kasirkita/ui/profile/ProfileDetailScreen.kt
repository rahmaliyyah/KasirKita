package com.example.kasirkita.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.viewmodel.ProfileActionState
import com.example.kasirkita.viewmodel.ProfileViewModel

/*
 * ProfileDetailScreen menampilkan detail profil satu user.
 * Owner bisa mengedit nama dan role.
 * Cashier hanya bisa melihat profil miliknya sendiri (read-only).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    profileViewModel: ProfileViewModel,
    isOwner: Boolean,
    onBackClick: () -> Unit
) {
    val selectedProfile = profileViewModel.selectedProfile.collectAsStateWithLifecycle()
    val actionState = profileViewModel.actionState.collectAsStateWithLifecycle()
    val profileName = profileViewModel.profileName.collectAsStateWithLifecycle()
    val profileRole = profileViewModel.profileRole.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }

    val profile = selectedProfile.value

    // Reset state setelah aksi berhasil
    LaunchedEffect(actionState.value) {
        if (actionState.value is ProfileActionState.Success) {
            showEditDialog = false
            profileViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile?.name ?: "Detail Profil") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (profile == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Profil tidak ditemukan")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Card info profil ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Role: ${profile.role.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ID: ${profile.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Tombol edit — hanya untuk owner ─────────────────────
            if (isOwner) {
                Button(
                    onClick = {
                        profileViewModel.onProfileNameChange(profile.name)
                        profileViewModel.onProfileRoleChange(profile.role)
                        showEditDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Profil")
                }
            }
        }
    }

    // ── Dialog edit profil ───────────────────────────────────────────
    if (showEditDialog && profile != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                profileViewModel.resetProfileForm()
                profileViewModel.resetActionState()
            },
            title = { Text("Edit Profil") },
            text = {
                Column {
                    OutlinedTextField(
                        value = profileName.value,
                        onValueChange = profileViewModel::onProfileNameChange,
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Role",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Pilihan role dengan RadioButton
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = profileRole.value == "owner",
                            onClick = { profileViewModel.onProfileRoleChange("owner") }
                        )
                        Text("Owner", modifier = Modifier.padding(start = 4.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = profileRole.value == "cashier",
                            onClick = { profileViewModel.onProfileRoleChange("cashier") }
                        )
                        Text("Cashier", modifier = Modifier.padding(start = 4.dp))
                    }
                    if (actionState.value is ProfileActionState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (actionState.value as ProfileActionState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { profileViewModel.updateProfile(profile.id) },
                    enabled = actionState.value !is ProfileActionState.Loading
                ) {
                    if (actionState.value is ProfileActionState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Simpan")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    profileViewModel.resetProfileForm()
                    profileViewModel.resetActionState()
                }) {
                    Text("Batal")
                }
            }
        )
    }
}