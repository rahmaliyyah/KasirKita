package com.example.kasirkita.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.CashRegister
import com.example.kasirkita.viewmodel.ExpenseActionState
import com.example.kasirkita.viewmodel.ExpenseViewModel
import com.example.kasirkita.viewmodel.KasViewModel
import java.util.Calendar

/*
 * ExpenseFormScreen untuk catat pengeluaran baru.
 * Form input: Tanggal, Deskripsi, Jumlah, Pilih Kas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    expenseViewModel: ExpenseViewModel,
    kasViewModel: KasViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val expenseDate = expenseViewModel.expenseDate.collectAsStateWithLifecycle()
    val expenseDescription = expenseViewModel.expenseDescription.collectAsStateWithLifecycle()
    val expenseAmount = expenseViewModel.expenseAmount.collectAsStateWithLifecycle()
    val selectedCashRegister = expenseViewModel.selectedCashRegisterId.collectAsStateWithLifecycle()
    val actionState = expenseViewModel.actionState.collectAsStateWithLifecycle()

    // Untuk dropdown pilih kas
    var showCashRegisterDropdown by remember { mutableStateOf(false) }
    var cashRegisterList by remember { mutableStateOf<List<CashRegister>>(emptyList()) }

    // Untuk date picker
    var showDatePicker by remember { mutableStateOf(false) }

    // Load kas list
    LaunchedEffect(Unit) {
        kasViewModel.loadKasRegisters()
    }

    // Observe kas list
    val kasListState by kasViewModel.kasListState.collectAsStateWithLifecycle()

    // Tutup form kalau berhasil
    LaunchedEffect(actionState.value) {
        if (actionState.value is ExpenseActionState.Success) {
            expenseViewModel.resetActionState()
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catat Pengeluaran") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Batal")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Section: Tanggal
                item {
                    Text("Tanggal Pengeluaran", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (expenseDate.value.isEmpty())
                                "Pilih Tanggal (tap untuk membuka)"
                            else
                                expenseDate.value.formatDate()
                        )
                    }
                }

                // Section: Deskripsi
                item {
                    Text("Deskripsi Pengeluaran", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    OutlinedTextField(
                        value = expenseDescription.value,
                        onValueChange = { expenseViewModel.setExpenseDescription(it) },
                        label = { Text("Contoh: Membayar operasional, Zakat, dll") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                // Section: Jumlah
                item {
                    Text("Jumlah Pengeluaran (Rp)", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    OutlinedTextField(
                        value = expenseAmount.value,
                        onValueChange = { expenseViewModel.setExpenseAmount(it) },
                        label = { Text("Contoh: 50000") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Pilih Kas
                item {
                    Text("Pilih Kas yang Digunakan", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showCashRegisterDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selectedCashRegister.value.isEmpty())
                                    "Pilih Kas..."
                                else
                                    cashRegisterList.find { it.id == selectedCashRegister.value }?.name
                                        ?: "Pilih Kas..."
                            )
                        }

                        DropdownMenu(
                            expanded = showCashRegisterDropdown,
                            onDismissRequest = { showCashRegisterDropdown = false }
                        ) {
                            cashRegisterList.forEach { kas ->
                                DropdownMenuItem(
                                    text = { Text(kas.name) },
                                    onClick = {
                                        expenseViewModel.setSelectedCashRegister(kas.id)
                                        showCashRegisterDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Error message
                if (actionState.value is ExpenseActionState.Error) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
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

                // Button Simpan
                item {
                    Button(
                        onClick = {
                            expenseViewModel.createExpense(
                                cashRegisterId = selectedCashRegister.value,
                                date = expenseDate.value,
                                description = expenseDescription.value,
                                amountStr = expenseAmount.value
                            )
                        },
                        enabled = actionState.value !is ExpenseActionState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (actionState.value is ExpenseActionState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Simpan Pengeluaran")
                    }
                }
            }

            // Loading overlay
            if (actionState.value is ExpenseActionState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                expenseViewModel.setExpenseDate(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Update cash register list when loaded
    if (kasListState is com.example.kasirkita.viewmodel.KasListUiState.Success) {
        val currentCashList = (kasListState as com.example.kasirkita.viewmodel.KasListUiState.Success).registers
        if (cashRegisterList != currentCashList) {
            cashRegisterList = currentCashList
        }
    }
}

/*
 * Simple Date Picker Dialog
 */
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Tanggal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Year input
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { year = it.toIntOrNull() ?: year },
                    label = { Text("Tahun") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                // Month input
                OutlinedTextField(
                    value = (month + 1).toString(),
                    onValueChange = { newMonth ->
                        val m = newMonth.toIntOrNull() ?: (month + 1)
                        if (m in 1..12) month = m - 1
                    },
                    label = { Text("Bulan (1-12)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                // Day input
                OutlinedTextField(
                    value = day.toString(),
                    onValueChange = { newDay ->
                        val d = newDay.toIntOrNull() ?: day
                        if (d in 1..31) day = d
                    },
                    label = { Text("Hari (1-31)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val dateStr = "%04d-%02d-%02dT00:00:00Z".format(year, month + 1, day)
                onDateSelected(dateStr)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
