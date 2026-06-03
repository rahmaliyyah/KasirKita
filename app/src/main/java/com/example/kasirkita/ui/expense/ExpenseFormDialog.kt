package com.example.kasirkita.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.ExpenseActionState
import com.example.kasirkita.viewmodel.ExpenseViewModel
import com.example.kasirkita.viewmodel.KasListUiState
import com.example.kasirkita.viewmodel.KasViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormDialog(
    expenseViewModel: ExpenseViewModel,
    kasViewModel: KasViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val date by expenseViewModel.expenseDate.collectAsStateWithLifecycle()
    val description by expenseViewModel.expenseDescription.collectAsStateWithLifecycle()
    val amount by expenseViewModel.expenseAmount.collectAsStateWithLifecycle()
    val selectedKasId by expenseViewModel.selectedCashRegisterId.collectAsStateWithLifecycle()
    val actionState by expenseViewModel.actionState.collectAsStateWithLifecycle()
    val kasListState by kasViewModel.kasListState.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedKasDropdown by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    LaunchedEffect(Unit) {
        kasViewModel.loadKasRegisters()
        if (date.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            expenseViewModel.setExpenseDate(sdf.format(Date()))
        }
    }

    LaunchedEffect(actionState) {
        if (actionState is ExpenseActionState.Success) {
            onSuccess()
            expenseViewModel.resetActionState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Catat Pengeluaran",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.5f))

                // Tanggal
                OutlinedTextField(
                    value = date.split("T").first(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GoldPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Pilih Tanggal", tint = GoldPrimary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )

                // Deskripsi
                OutlinedTextField(
                    value = description,
                    onValueChange = expenseViewModel::setExpenseDescription,
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Contoh: Bayar Listrik") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = GoldPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )

                // Jumlah
                OutlinedTextField(
                    value = amount,
                    onValueChange = expenseViewModel::setExpenseAmount,
                    label = { Text("Jumlah (Rp)") },
                    placeholder = { Text("0") },
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = GoldPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )

                // Pilih Kas (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedKasDropdown,
                    onExpandedChange = { expandedKasDropdown = !expandedKasDropdown }
                ) {
                    val currentKasName = if (kasListState is KasListUiState.Success) {
                        (kasListState as KasListUiState.Success).registers.find { it.id == selectedKasId }?.name ?: "Pilih Kas"
                    } else "Memuat Kas..."

                    OutlinedTextField(
                        value = currentKasName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gunakan Saldo Kas") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKasDropdown) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedKasDropdown,
                        onDismissRequest = { expandedKasDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (kasListState is KasListUiState.Success) {
                            (kasListState as KasListUiState.Success).registers.filter { it.isActive }.forEach { kas ->
                                DropdownMenuItem(
                                    text = { Text(kas.name) },
                                    onClick = {
                                        expenseViewModel.setSelectedCashRegister(kas.id)
                                        expandedKasDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (actionState is ExpenseActionState.Error) {
                    Text(
                        text = (actionState as ExpenseActionState.Error).message,
                        color = Error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = { expenseViewModel.createExpense() },
                        modifier = Modifier.weight(1.5f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        enabled = actionState !is ExpenseActionState.Loading && description.isNotBlank() && amount.isNotBlank() && selectedKasId.isNotBlank()
                    ) {
                        if (actionState is ExpenseActionState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        expenseViewModel.setExpenseDate(sdf.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
