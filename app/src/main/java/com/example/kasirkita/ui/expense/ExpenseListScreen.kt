package com.example.kasirkita.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.Expense
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.formatDate
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.ExpenseListUiState
import com.example.kasirkita.viewmodel.ExpenseViewModel
import com.example.kasirkita.viewmodel.KasViewModel

@Composable
fun ExpenseListScreen(
    expenseViewModel: ExpenseViewModel,
    kasViewModel: KasViewModel,
    onExpenseClick: (Expense) -> Unit,
    onBackClick: () -> Unit
) {
    val expenseListState by expenseViewModel.expenseListState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showFormDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        expenseViewModel.loadAllExpenses()
    }

    val filteredExpenses = remember(expenseListState, searchQuery) {
        if (expenseListState is ExpenseListUiState.Success) {
            (expenseListState as ExpenseListUiState.Success).expenses.filter {
                it.description.contains(searchQuery, ignoreCase = true)
            }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            Column {
                ModernTopBar(
                    title = "Pengeluaran",
                    onBackClick = onBackClick
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        if (expenseListState is ExpenseListUiState.Loading && filteredExpenses.isNotEmpty()) {
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
                            placeholder = { Text("Cari pengeluaran...", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
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
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFormDialog = true },
                containerColor = GoldPrimary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (expenseListState is ExpenseListUiState.Loading && filteredExpenses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                if (filteredExpenses.isEmpty() && expenseListState !is ExpenseListUiState.Loading) {
                    EmptyExpenseState()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredExpenses) { expense ->
                            ExpenseItem(expense) { onExpenseClick(expense) }
                        }
                    }
                }
            }

            if (expenseListState is ExpenseListUiState.Error) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((expenseListState as ExpenseListUiState.Error).message, color = Error)
                }
            }
        }
    }

    if (showFormDialog) {
        ExpenseFormDialog(
            expenseViewModel = expenseViewModel,
            kasViewModel = kasViewModel,
            onDismiss = { 
                showFormDialog = false 
                expenseViewModel.resetFormFields()
            },
            onSuccess = {
                showFormDialog = false
                expenseViewModel.loadAllExpenses()
            }
        )
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit
) {
    val isCancelled = expense.status == "cancelled"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        .background(Error.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = expense.date.formatDate(),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Text(
                    text = "-${expense.amount.toRupiah()}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Error
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
                        color = Error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyExpenseState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum Ada Pengeluaran", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("Catat pengeluaran operasional di sini", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}
