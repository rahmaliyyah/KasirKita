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
import com.example.kasirkita.viewmodel.ProfileActionState
import com.example.kasirkita.viewmodel.ProfileListUiState
import com.example.kasirkita.viewmodel.ProfileViewModel

@Composable
fun ProfileListScreen(
    profileViewModel: ProfileViewModel,
    onProfileClick: (UserProfile) -> Unit,
    onBackClick: () -> Unit
) {
    val profileListState by profileViewModel.profileListState.collectAsStateWithLifecycle()
    val actionState by profileViewModel.actionState.collectAsStateWithLifecycle()
    
    val profileName by profileViewModel.profileName.collectAsStateWithLifecycle()
    val profileEmail by profileViewModel.profileEmail.collectAsStateWithLifecycle()
    val profilePassword by profileViewModel.profilePassword.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfiles()
    }

    LaunchedEffect(actionState) {
        if (actionState is ProfileActionState.Success) {
            showAddDialog = false
            profileViewModel.resetActionState()
        }
    }

    val filteredProfiles = remember(profileListState, searchQuery) {
        if (profileListState is ProfileListUiState.Success) {
            val profiles = (profileListState as ProfileListUiState.Success).profiles
            profiles.filter { it.name.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            Column {
                ModernTopBar(
                    title = "Manajemen Karyawan",
                    onBackClick = onBackClick
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama atau role...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    profileViewModel.resetProfileForm()
                    showAddDialog = true 
                },
                containerColor = GoldPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (profileListState) {
                is ProfileListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is ProfileListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (profileListState as ProfileListUiState.Error).message, color = Error)
                    }
                }
                is ProfileListUiState.Success -> {
                    if (filteredProfiles.isEmpty()) {
                        EmptyProfileState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProfiles) { profile ->
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

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Karyawan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Daftarkan akun kasir baru. Password dapat diubah kemudian oleh karyawan.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = profileViewModel::onProfileNameChange,
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )

                    OutlinedTextField(
                        value = profileEmail,
                        onValueChange = profileViewModel::onProfileEmailChange,
                        label = { Text("Email Login") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )

                    OutlinedTextField(
                        value = profilePassword,
                        onValueChange = profileViewModel::onProfilePasswordChange,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    
                    if (actionState is ProfileActionState.Error) {
                        Text((actionState as ProfileActionState.Error).message, color = Error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { profileViewModel.addCashier() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    enabled = actionState !is ProfileActionState.Loading
                ) {
                    if (actionState is ProfileActionState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Daftarkan")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
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
        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum Ada Karyawan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
