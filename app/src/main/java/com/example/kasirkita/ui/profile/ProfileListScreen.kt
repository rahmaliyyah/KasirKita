package com.example.kasirkita.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.repository.UserProfile
import com.example.kasirkita.viewmodel.ProfileListUiState
import com.example.kasirkita.viewmodel.ProfileViewModel

/*
 * ProfileListScreen menampilkan daftar profil.
 * Owner: melihat semua profil user
 * Cashier: hanya melihat profil miliknya (RLS otomatis memfilter)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    profileViewModel: ProfileViewModel,
    isOwner: Boolean,
    onProfileClick: (UserProfile) -> Unit,
    onBackClick: () -> Unit
) {
    val profileListState = profileViewModel.profileListState.collectAsStateWithLifecycle()

    // Load data saat screen pertama kali tampil
    LaunchedEffect(Unit) {
        profileViewModel.loadProfiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isOwner) "Manajemen Profil" else "Profil Saya") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = profileListState.value) {
                is ProfileListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProfileListUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is ProfileListUiState.Success -> {
                    if (state.profiles.isEmpty()) {
                        Text(
                            text = "Tidak ada data profil.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.profiles) { profile ->
                                ProfileItem(
                                    profile = profile,
                                    isOwner = isOwner,
                                    onClick = { onProfileClick(profile) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
 * Card satu item profil.
 */
@Composable
fun ProfileItem(
    profile: UserProfile,
    isOwner: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                // Hanya owner yang bisa klik untuk edit
                if (isOwner) Modifier.clickable { onClick() } else Modifier
            ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: ${profile.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Badge role
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (profile.role == "owner") {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = profile.role.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (profile.role == "owner") {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}