package com.example.kasirkita.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.ui.components.BalanceCard
import com.example.kasirkita.ui.components.DashboardTransaction
import com.example.kasirkita.ui.components.RecentTransactionsSection
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.*

@Composable
fun KasirDashboardScreen(
    userName: String,
    saleViewModel: SaleViewModel,
    kasViewModel: KasViewModel,
    onStartSaleClick: () -> Unit,
    onManageCustomerClick: () -> Unit,
    onViewProductClick: () -> Unit,
    onSaleClick: (String) -> Unit,
    onLihatTransaksiClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val saleListState by saleViewModel.saleListState.collectAsStateWithLifecycle()
    val kasListState by kasViewModel.kasListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        saleViewModel.loadAllSales()
        kasViewModel.loadKasRegisters()
    }

    val recentSales = remember(saleListState) {
        if (saleListState is SaleListUiState.Success) {
            (saleListState as SaleListUiState.Success).sales
                .take(3)
                .map { DashboardTransaction.SaleTx(it) }
        } else emptyList()
    }

    val totalBalance = remember(kasListState) {
        if (kasListState is KasListUiState.Success) {
            (kasListState as KasListUiState.Success).registers
                .filter { it.isActive }
                .sumOf { it.currentBalance }
        } else 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        KasirTopBar(userName)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            BalanceCard(
                totalBalance = totalBalance,
                title = "Saldo Kas Aktif",
                subtitle = "Semangat melayani pelanggan!"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Akses Cepat",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallMenuCard("Pelanggan", Icons.Default.Groups, Info, onManageCustomerClick, Modifier.weight(1f))
                SmallMenuCard("Cek Stok", Icons.Default.Inventory2, GoldPrimary, onViewProductClick, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            PosQuickAccessCard(onStartSaleClick)

            Spacer(modifier = Modifier.height(32.dp))

            RecentTransactionsSection(
                recentSales, 
                onSeeAllClick = onLihatTransaksiClick,
                onSaleClick = onSaleClick,
                onExpenseClick = {}
            )
            
            Spacer(modifier = Modifier.height(bottomPadding + 30.dp))
        }
    }
}

@Composable
fun KasirTopBar(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Selamat Bekerja,",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun PosQuickAccessCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "KASIR",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Klik untuk membuat transaksi baru",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun SmallMenuCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}
