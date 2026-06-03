package com.example.kasirkita.ui

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.Expense
import com.example.kasirkita.model.Sale
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.formatDate
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

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

    // Hitung Total Saldo dari semua kas
    val totalBalance = remember(kasListState) {
        if (kasListState is KasListUiState.Success) {
            (kasListState as KasListUiState.Success).registers.sumOf { it.currentBalance }
        } else 0.0
    }

    // Persiapkan data transaksi gabungan (Sale & Expense) untuk Recent Transactions
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
        // 1. Top Bar
        HomeTopBar(userName, userEmail)

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
        ) {
            // 2. Balance Card
            BalanceCard(totalBalance)

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Navigation Menu Section
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

            // 4. Recent Transactions
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

@Composable
fun BalanceCard(totalBalance: Double) {
    var isVisible by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(12.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GoldGradientStart, GoldGradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total Saldo Toko",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { isVisible = !isVisible }
                        )
                    }
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "IDR",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Rp ",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = if (isVisible) totalBalance.toRupiah().replace("Rp", "").trim() else "••••••",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kelola finansialmu dengan bijak",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(
    transactions: List<DashboardTransaction>, 
    onSeeAllClick: () -> Unit,
    onSaleClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaksi Terakhir",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (transactions.isEmpty()) {
            Text("Belum ada transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 20.dp))
        } else {
            transactions.forEach { tx ->
                TransactionItem(
                    title = tx.title,
                    subtitle = tx.subtitle,
                    amount = tx.amountStr,
                    icon = tx.icon,
                    isIncome = tx is DashboardTransaction.SaleTx,
                    isCancelled = tx.isCancelled,
                    onClick = {
                        if (tx is DashboardTransaction.SaleTx) onSaleClick(tx.sale.id)
                        else if (tx is DashboardTransaction.ExpenseTx) onExpenseClick(tx.expense.id)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    subtitle: String,
    amount: String,
    icon: ImageVector,
    isIncome: Boolean,
    isCancelled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCancelled) 0.dp else 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .graphicsLayer(alpha = if (isCancelled) 0.4f else 1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) Success.copy(alpha = 0.1f) 
                            else Error.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isIncome) Success else Error,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                
                Text(
                    text = (if (isIncome) "+" else "-") + amount,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isIncome) Success else Error
                    )
                )
            }

            if (isCancelled) {
                Text(
                    text = "Dibatalkan",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 12.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

sealed class DashboardTransaction {
    abstract val title: String
    abstract val subtitle: String
    abstract val amountStr: String
    abstract val icon: ImageVector
    abstract val timestamp: String
    abstract val isCancelled: Boolean

    data class SaleTx(val sale: Sale) : DashboardTransaction() {
        override val title = "Penjualan #${sale.id.take(6).uppercase()}"
        override val subtitle = sale.soldAt.formatDate()
        override val amountStr = sale.totalAmount.toRupiah()
        override val icon = Icons.Default.ShoppingBag
        override val timestamp = sale.soldAt
        override val isCancelled = false
    }

    data class ExpenseTx(val expense: Expense) : DashboardTransaction() {
        override val title = expense.description
        override val subtitle = expense.date.formatDate()
        override val amountStr = expense.amount.toRupiah()
        override val icon = Icons.AutoMirrored.Filled.ReceiptLong
        override val timestamp = expense.date
        override val isCancelled = expense.status == "cancelled"
    }
}
