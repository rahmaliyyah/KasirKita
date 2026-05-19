package com.example.kasirkita.ui.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.Expense
import com.example.kasirkita.viewmodel.ExpenseListUiState
import com.example.kasirkita.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/*
 * Format angka ke Rupiah
 */
fun Double.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this)
}

/*
 * Format tanggal ISO ke format lokal (dd/MM/yyyy)
 */
fun String.formatDate(): String {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = isoFormat.parse(this) ?: return this
        displayFormat.format(date)
    } catch (e: Exception) {
        this
    }
}

/*
 * ExpenseListScreen menampilkan laporan daftar pengeluaran.
 * Owner bisa tambah pengeluaran baru (FAB).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    expenseViewModel: ExpenseViewModel,
    onExpenseClick: (Expense) -> Unit,
    onAddExpenseClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val expenseListState = expenseViewModel.expenseListState.collectAsStateWithLifecycle()

    // Load data saat screen pertama kali tampil
    LaunchedEffect(Unit) {
        expenseViewModel.loadAllExpenses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Pengeluaran") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpenseClick) {
                Icon(Icons.Default.Add, contentDescription = "Catat Pengeluaran")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = expenseListState.value) {
                is ExpenseListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ExpenseListUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is ExpenseListUiState.Success -> {
                    if (state.expenses.isEmpty()) {
                        Text(
                            text = "Belum ada pengeluaran. Tap + untuk menambah.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.expenses) { expense ->
                                ExpenseItem(
                                    expense = expense,
                                    onClick = { onExpenseClick(expense) }
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
 * Satu item pengeluaran dalam list
 */
@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Deskripsi & Status
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.description,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // Status badge
                Badge(
                    modifier = Modifier.align(Alignment.Top),
                    containerColor = if (expense.status == "recorded") {
                        Color(0xFF4CAF50)  // Green
                    } else {
                        Color(0xFFF44336)  // Red
                    }
                ) {
                    Text(
                        text = if (expense.status == "recorded") "Tercatat" else "Dibatalkan",
                        color = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Amount
            Text(
                text = "Jumlah: ${expense.amount.toRupiah()}",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Row 3: Tanggal
            Text(
                text = "Tanggal: ${expense.date.formatDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
