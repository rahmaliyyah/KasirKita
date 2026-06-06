package com.example.kasirkita.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
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
    val selectedProfile by profileViewModel.selectedProfile.collectAsStateWithLifecycle()
    val actionState by profileViewModel.actionState.collectAsStateWithLifecycle()
    val profileName by profileViewModel.profileName.collectAsStateWithLifecycle()
    val profileRole by profileViewModel.profileRole.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val profile = selectedProfile

    LaunchedEffect(actionState) {
        if (actionState is ProfileActionState.Success) {
            showEditDialog = false
            if (showDeleteConfirm) {
                showDeleteConfirm = false
                onBackClick()
            }
            profileViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                title = "Detail Karyawan",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (profile == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Profile Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.take(1).uppercase(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Surface(
                            color = if (profile.role == "owner") GoldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = profile.role.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.role == "owner") GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Info Section
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Informasi Akun",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoDetailRow("User ID", "#${profile.id.take(8).uppercase()}")
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            InfoDetailRow("Akses", profile.role.replaceFirstChar { it.uppercase() })
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Actions (Only for Owner and not deleting self)
                    if (isOwner) {
                        Button(
                            onClick = {
                                profileViewModel.onProfileNameChange(profile.name)
                                profileViewModel.onProfileRoleChange(profile.role)
                                showEditDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Edit Profil", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Error.copy(alpha = 0.08f),
                                contentColor = Error
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Hapus Karyawan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && profile != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profil Karyawan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = profileViewModel::onProfileNameChange,
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    
                    Text("Role Akses", style = MaterialTheme.typography.labelMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RoleSelectCard(
                            title = "Owner",
                            selected = profileRole == "owner",
                            onClick = { profileViewModel.onProfileRoleChange("owner") },
                            modifier = Modifier.weight(1f)
                        )
                        RoleSelectCard(
                            title = "Kasir",
                            selected = profileRole == "cashier",
                            onClick = { profileViewModel.onProfileRoleChange("cashier") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { profileViewModel.updateProfile(profile.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Batal") }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showDeleteConfirm && profile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Karyawan?", fontWeight = FontWeight.Bold) },
            text = { Text("Akun ${profile.name} akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = { profileViewModel.deleteProfile(profile.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun InfoDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
    }
}

@Composable
fun RoleSelectCard(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) GoldPrimary.copy(alpha = 0.1f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title, color = if (selected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}