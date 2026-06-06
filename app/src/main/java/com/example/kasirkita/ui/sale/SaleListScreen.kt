package com.example.kasirkita.ui.sale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.components.TransactionItem
import com.example.kasirkita.ui.components.DashboardTransaction
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.*

@Composable
fun SaleListScreen(
    saleViewModel: SaleViewModel,
    expenseViewModel: ExpenseViewModel,
    userRole: String? = "owner",
    onSaleClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit,
    onAddSaleClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val saleListState by saleViewModel.saleListState.collectAsStateWithLifecycle()
    val expenseListState by expenseViewModel.expenseListState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    LaunchedEffect(Unit) {
        saleViewModel.loadAllSales()
        if (userRole == "owner") {
            expenseViewModel.loadAllExpenses()
        }
    }

    val combinedTransactions = remember(saleListState, expenseListState, searchQuery, selectedFilter, userRole) {
        val sales = if (saleListState is SaleListUiState.Success) (saleListState as SaleListUiState.Success).sales else emptyList()
        val expenses = if (expenseListState is ExpenseListUiState.Success) (expenseListState as ExpenseListUiState.Success).expenses else emptyList()

        val all = (sales.map { DashboardTransaction.SaleTx(it) } + 
                  (if (userRole == "owner") expenses.map { DashboardTransaction.ExpenseTx(it) } else emptyList()))
            .sortedByDescending { it.timestamp }

        all.filter { tx ->
            val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Penjualan" -> tx is DashboardTransaction.SaleTx
                "Pengeluaran" -> tx is DashboardTransaction.ExpenseTx
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
                        if (saleListState is SaleListUiState.Loading || (userRole == "owner" && expenseListState is ExpenseListUiState.Loading)) {
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
                        if (userRole == "owner") {
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
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
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
                    (userRole == "owner" && expenseListState is ExpenseListUiState.Loading && combinedTransactions.isEmpty())) {
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
                    }
                }
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
        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Transaksi Kosong", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("Belum ada riwayat transaksi yang ditemukan", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}
