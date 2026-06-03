package com.example.kasirkita.ui.sale

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
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasirkita.model.Expense
import com.example.kasirkita.model.Sale
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.formatDate
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SaleListScreen(
    saleViewModel: SaleViewModel,
    expenseViewModel: ExpenseViewModel,
    onSaleClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit,
    onAddSaleClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val saleListState by saleViewModel.saleListState.collectAsState()
    val expenseListState by expenseViewModel.expenseListState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    LaunchedEffect(Unit) {
        saleViewModel.loadAllSales()
        expenseViewModel.loadAllExpenses()
    }

    val combinedTransactions = remember(saleListState, expenseListState, searchQuery, selectedFilter) {
        val sales = if (saleListState is SaleListUiState.Success) (saleListState as SaleListUiState.Success).sales else emptyList()
        val expenses = if (expenseListState is ExpenseListUiState.Success) (expenseListState as ExpenseListUiState.Success).expenses else emptyList()

        val all = (sales.map { TransactionWrapper.SaleWrap(it) } + expenses.map { TransactionWrapper.ExpenseWrap(it) })
            .sortedByDescending { it.timestamp }

        all.filter { tx ->
            val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Penjualan" -> tx is TransactionWrapper.SaleWrap
                "Pengeluaran" -> tx is TransactionWrapper.ExpenseWrap
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            Column {
                ModernTopBar(
                    title = "Riwayat Transaksi"
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        if (saleListState is SaleListUiState.Loading || expenseListState is ExpenseListUiState.Loading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(2.dp),
                                color = GoldPrimary,
                                trackColor = GoldPrimary.copy(alpha = 0.1f)
                            )
                        }
                        
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari transaksi...", fontSize = 14.sp) },
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

                        // Filters
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Semua", "Penjualan", "Pengeluaran").forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = Color.White,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedFilter == filter,
                                        borderColor = MaterialTheme.colorScheme.surfaceVariant,
                                        selectedBorderColor = GoldPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = bottomPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (combinedTransactions.isEmpty()) {
                if ((saleListState is SaleListUiState.Loading && combinedTransactions.isEmpty()) || 
                    (expenseListState is ExpenseListUiState.Loading && combinedTransactions.isEmpty())) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                } else {
                    EmptySaleState()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 20.dp, start = 20.dp, end = 20.dp, bottom = bottomPadding + 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(combinedTransactions) { tx ->
                        TransactionCard(tx) {
                            if (tx is TransactionWrapper.SaleWrap) onSaleClick(tx.sale.id)
                            else if (tx is TransactionWrapper.ExpenseWrap) onExpenseClick(tx.expense.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(tx: TransactionWrapper, onClick: () -> Unit) {
    val isCancelled = tx is TransactionWrapper.ExpenseWrap && tx.expense.status == "cancelled"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
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
                        .background(tx.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tx.icon, 
                        contentDescription = null, 
                        tint = tx.color, 
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = tx.timestamp.formatDate(),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                
                Text(
                    text = (if (tx is TransactionWrapper.SaleWrap) "+" else "-") + tx.amount.toRupiah(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = tx.color
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
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun EmptySaleState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Transaksi Kosong", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("Belum ada riwayat transaksi yang ditemukan", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

sealed class TransactionWrapper {
    abstract val title: String
    abstract val amount: Double
    abstract val timestamp: String
    abstract val icon: ImageVector
    abstract val color: Color

    data class SaleWrap(val sale: Sale) : TransactionWrapper() {
        override val title = "Penjualan #${sale.id.take(6).uppercase()}"
        override val amount = sale.totalAmount
        override val timestamp = sale.soldAt
        override val icon = Icons.Default.ShoppingBag
        override val color = Success
    }

    data class ExpenseWrap(val expense: Expense) : TransactionWrapper() {
        override val title = expense.description
        override val amount = expense.amount
        override val timestamp = expense.date
        override val icon = Icons.AutoMirrored.Filled.ReceiptLong
        override val color = Error
    }
}
