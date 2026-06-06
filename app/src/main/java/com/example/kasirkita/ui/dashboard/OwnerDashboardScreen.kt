package com.example.kasirkita.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.kasirkita.viewmodel.*

@Composable
fun OwnerDashboardScreen(
    userName: String,
    userEmail: String,
    kasirName: String,
    kasirEmail: String,
    kasirPassword: String,
    uiState: AuthUiState,
    kasViewModel: KasViewModel,
    saleViewModel: SaleViewModel,
    expenseViewModel: ExpenseViewModel,
    onKasirNameChange: (String) -> Unit,
    onKasirEmailChange: (String) -> Unit,
    onKasirPasswordChange: (String) -> Unit,
    onTambahKasirClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onKelolaPengeluaranClick: () -> Unit,
    onKelolaKasClick: () -> Unit,
    onKelolaPelangganClick: () -> Unit,
    onKelolaKaryawanClick: () -> Unit,
    onKelolaProfilClick: () -> Unit,
    onLihatSemuaTransaksiClick: () -> Unit,
    onSaleClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val kasListState by kasViewModel.kasListState.collectAsStateWithLifecycle()
    val saleListState by saleViewModel.saleListState.collectAsStateWithLifecycle()
    val expenseListState by expenseViewModel.expenseListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        kasViewModel.loadKasRegisters()
        saleViewModel.loadAllSales()
        expenseViewModel.loadAllExpenses()
    }

    val totalBalance = remember(kasListState) {
        if (kasListState is KasListUiState.Success) {
            (kasListState as KasListUiState.Success).registers.sumOf { it.currentBalance }
        } else 0.0
    }

    val recentTransactions = remember(saleListState, expenseListState) {
        val sales = if (saleListState is SaleListUiState.Success) (saleListState as SaleListUiState.Success).sales else emptyList()
        val expenses = if (expenseListState is ExpenseListUiState.Success) (expenseListState as ExpenseListUiState.Success).expenses else emptyList()
        
        (sales.map { DashboardTransaction.SaleTx(it) } + expenses.map { DashboardTransaction.ExpenseTx(it) })
            .sortedByDescending { it.timestamp }
            .take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        HomeTopBar(userName, userEmail)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            BalanceCard(totalBalance)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Manajemen Toko",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LargeMenuCard("Kelola Kas", Icons.Default.AccountBalance, GoldPrimary, "Pantau saldo & mutasi", onKelolaKasClick, Modifier.weight(1f))
                    LargeMenuCard("Pengeluaran", Icons.AutoMirrored.Filled.ReceiptLong, Error, "Catat biaya harian", onKelolaPengeluaranClick, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LargeMenuCard("Pelanggan", Icons.Default.Groups, Info, "Data pelanggan setia", onKelolaPelangganClick, Modifier.weight(1f))
                    LargeMenuCard("Karyawan", Icons.Default.ManageAccounts, Success, "Atur akses kasir", onKelolaKaryawanClick, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            RecentTransactionsSection(
                recentTransactions, 
                onSeeAllClick = onLihatSemuaTransaksiClick,
                onSaleClick = onSaleClick,
                onExpenseClick = onExpenseClick
            )
            
            Spacer(modifier = Modifier.height(bottomPadding + 30.dp))
        }
    }
}

@Composable
fun HomeTopBar(userName: String, userEmail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top 
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = "Halo Owner",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = userEmail,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
fun LargeMenuCard(title: String, icon: ImageVector, color: Color, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
