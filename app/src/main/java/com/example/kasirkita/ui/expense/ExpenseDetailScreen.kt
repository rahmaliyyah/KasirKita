package com.example.kasirkita.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.viewmodel.ExpenseActionState
import com.example.kasirkita.viewmodel.ExpenseViewModel

/*
 * ExpenseDetailScreen menampilkan detail satu pengeluaran.
 * Owner bisa:
 * - Edit deskripsi (jika status='recorded')
 * - Batalkan pengeluaran (jika status='recorded')
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseViewModel: ExpenseViewModel,
    expenseId: String,
    onBackClick: () -> Unit,
    onEditSuccess: () -> Unit
) {
    val selectedExpense = expenseViewModel.selectedExpense.collectAsStateWithLifecycle()
    val actionState = expenseViewModel.actionState.collectAsStateWithLifecycle()
    val editDescription = expenseViewModel.expenseDescription.collectAsStateWithLifecycle()

    var isEditMode by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    // Load expense detail saat screen muncul
    LaunchedEffect(Unit) {
        expenseViewModel.loadExpenseDetail(expenseId)
    }

    // Close screen jika berhasil
    LaunchedEffect(actionState.value) {
        if (actionState.value is ExpenseActionState.Success) {
            expenseViewModel.resetActionState()
            onEditSuccess()
        }
    }

    val expense = selectedExpense.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pengeluaran") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (expense == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Status badge
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status", fontWeight = FontWeight.Bold)
                            Badge(
                                containerColor = if (expense.status == "recorded") {
                                    Color(0xFF4CAF50)
                                } else {
                                    Color(0xFFF44336)
                                }
                            ) {
                                Text(
                                    text = if (expense.status == "recorded") "Tercatat" else "Dibatalkan",
                                    color = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }

                    // Tanggal
                    item {
                        Column {
                            Text("Tanggal", fontWeight = FontWeight.Bold)
                            Text(
                                text = expense.date.formatDate(),
                                color = Color.Gray
                            )
                        }
                    }

                    // Jumlah
                    item {
                        Column {
                            Text("Jumlah", fontWeight = FontWeight.Bold)
                            Text(
                                text = expense.amount.toRupiah(),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                    // Deskripsi / Edit Deskripsi
                    item {
                        Column {
                            Text("Deskripsi", fontWeight = FontWeight.Bold)
                            if (isEditMode && expense.status == "recorded") {
                                OutlinedTextField(
                                    value = editDescription.value,
                                    onValueChange = { expenseViewModel.setExpenseDescription(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                            } else {
                                Text(
                                    text = expense.description,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Error message
                    if (actionState.value is ExpenseActionState.Error) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = (actionState.value as ExpenseActionState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Action buttons (hanya jika status='recorded')
                    if (expense.status == "recorded") {
                        item {
                            if (isEditMode) {
                                // Save / Cancel buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            expenseViewModel.updateExpenseDescription(
                                                expenseId,
                                                editDescription.value
                                            )
                                            isEditMode = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = actionState.value !is ExpenseActionState.Loading
                                    ) {
                                        Text("Simpan")
                                    }
                                    OutlinedButton(
                                        onClick = { isEditMode = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Batal")
                                    }
                                }
                            } else {
                                // Edit / Cancel buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { isEditMode = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null
                                        )

                                        Spacer(
                                            modifier = Modifier.width(4.dp)
                                        )

                                        Text("Edit Deskripsi")
                                    }
                                    OutlinedButton(
                                        onClick = { showCancelConfirm = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Batalkan")
                                    }
                                }
                            }
                        }
                    }
                }

                // Confirmation dialog untuk cancel
                if (showCancelConfirm) {
                    AlertDialog(
                        onDismissRequest = { showCancelConfirm = false },
                        title = { Text("Batalkan Pengeluaran?") },
                        text = {
                            Text(
                                "Membatalkan pengeluaran akan mengembalikan saldo kas sebesar ${expense.amount.toRupiah()}. " +
                                        "Lanjutkan?"
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    expenseViewModel.cancelExpense(expenseId)
                                    showCancelConfirm = false
                                }
                            ) {
                                Text("Ya, Batalkan")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCancelConfirm = false }) {
                                Text("Tidak")
                            }
                        }
                    )
                }
            }
        }
    }
}
