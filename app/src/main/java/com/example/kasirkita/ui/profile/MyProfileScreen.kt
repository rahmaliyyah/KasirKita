package com.example.kasirkita.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.AuthViewModel

@Composable
fun MyProfileScreen(
    authViewModel: AuthViewModel,
    onLogoutClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val userName by authViewModel.userName.collectAsState()
    val email by authViewModel.email.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val isDarkMode by authViewModel.isDarkMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp + 32.dp) // Account for status bar
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(GoldGradientStart, GoldGradientEnd)
                    )
                )
                .statusBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (userName ?: "U").take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userName ?: "User",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = userRole?.uppercase() ?: "OWNER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Informasi Akun",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoCard(
                items = listOf(
                    ProfileInfoItem("Nama Lengkap", userName ?: "-", Icons.Default.Person),
                    ProfileInfoItem("Email", email, Icons.Default.Email),
                    ProfileInfoItem("Role", userRole ?: "Owner", Icons.Default.Badge)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Settings Zone
            Text(
                text = "Pengaturan Aplikasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Mode Gelap", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { authViewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GoldPrimary)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    ProfileMenuButton("Keluar Aplikasi", Icons.AutoMirrored.Filled.Logout, color = Error, onClick = onLogoutClick)
                }
            }
            
            Spacer(modifier = Modifier.height(bottomPadding + 20.dp))
        }
    }
}

@Composable
fun ProfileInfoCard(items: List<ProfileInfoItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(item.icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(item.label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuButton(label: String, icon: ImageVector, color: Color? = null, onClick: () -> Unit) {
    val finalColor = color ?: MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (color == null) GoldPrimary else finalColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = finalColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

data class ProfileInfoItem(val label: String, val value: String, val icon: ImageVector)
