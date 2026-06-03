package com.example.kasirkita.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.repository.UserProfile
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.ProfileListUiState
import com.example.kasirkita.viewmodel.ProfileViewModel

@Composable
fun ProfileListScreen(
    profileViewModel: ProfileViewModel,
    onProfileClick: (UserProfile) -> Unit,
    onBackClick: () -> Unit
) {
    val profileListState = profileViewModel.profileListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfiles()
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                title = "Manajemen Karyawan",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = profileListState.value) {
                is ProfileListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is ProfileListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Error)
                    }
                }
                is ProfileListUiState.Success -> {
                    if (state.profiles.isEmpty()) {
                        EmptyProfileState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.profiles) { profile ->
                                ProfileItemModern(
                                    profile = profile,
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

@Composable
fun ProfileItemModern(profile: UserProfile, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = profile.role.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (profile.role == "owner") GoldPrimary else TextSecondary
                    )
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyProfileState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum Ada Karyawan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
