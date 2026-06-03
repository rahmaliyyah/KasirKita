package com.example.kasirkita.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.CustomerLog
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.viewmodel.CustomerActionState
import com.example.kasirkita.viewmodel.CustomerLogUiState
import com.example.kasirkita.viewmodel.CustomerViewModel

/*
 * CustomerDetailScreen menampilkan:
 * 1. Info detail pelanggan (nama, nomor HP, status)
 * 2. Tombol edit nama & nomor HP
 * 3. Tombol aktifkan / nonaktifkan
 * 4. Riwayat log perubahan (customer_logs)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerViewModel: CustomerViewModel,
    onBackClick: () -> Unit
) {
    val selectedCustomer = customerViewModel.selectedCustomer.collectAsStateWithLifecycle()
    val customerLogState = customerViewModel.customerLogState.collectAsStateWithLifecycle()
    val actionState = customerViewModel.actionState.collectAsStateWithLifecycle()
    val customerName = customerViewModel.customerName.collectAsStateWithLifecycle()
    val customerPhone = customerViewModel.customerPhone.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }

    val customer = selectedCustomer.value

    // Load log saat screen tampil
    LaunchedEffect(customer?.id) {
        customer?.id?.let { customerViewModel.loadCustomerLogs(it) }
    }

    // Reset state setelah aksi berhasil
    LaunchedEffect(actionState.value) {
        if (actionState.value is CustomerActionState.Success) {
            showEditDialog = false
            customerViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                title = customer?.name ?: "Detail Pelanggan",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (customer == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Pelanggan tidak ditemukan")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Card info pelanggan ──────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "HP: ${customer.phoneNumber ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (customer.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                        ) {
                            Text(
                                text = if (customer.isActive) "Aktif" else "Nonaktif",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // ── Tombol aksi ──────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Tombol edit nama & nomor HP
                    Button(
                        onClick = {
                            customerViewModel.onCustomerNameChange(customer.name)
                            customerViewModel.onCustomerPhoneChange(customer.phoneNumber ?: "")
                            showEditDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Data Pelanggan")
                    }

                    // Tombol aktifkan / nonaktifkan
                    OutlinedButton(
                        onClick = {
                            customerViewModel.toggleCustomerActive(customer.id, customer.isActive)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (customer.isActive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text(if (customer.isActive) "Nonaktifkan Pelanggan" else "Aktifkan Pelanggan")
                    }
                }
            }

            // ── Header riwayat log ───────────────────────────────────
            item {
                Text(
                    text = "Riwayat Perubahan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── List log ─────────────────────────────────────────────
            when (val logState = customerLogState.value) {
                is CustomerLogUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is CustomerLogUiState.Error -> {
                    item {
                        Text(
                            text = logState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is CustomerLogUiState.Success -> {
                    if (logState.logs.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada riwayat perubahan.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(logState.logs) { log ->
                            CustomerLogItem(log = log)
                        }
                    }
                }
            }
        }
    }

    // ── Dialog edit data pelanggan ───────────────────────────────────
    if (showEditDialog && customer != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                customerViewModel.resetCustomerForm()
                customerViewModel.resetActionState()
            },
            title = { Text("Edit Data Pelanggan") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customerName.value,
                        onValueChange = customerViewModel::onCustomerNameChange,
                        label = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerPhone.value,
                        onValueChange = customerViewModel::onCustomerPhoneChange,
                        label = { Text("Nomor HP (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (actionState.value is CustomerActionState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (actionState.value as CustomerActionState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { customerViewModel.updateCustomer(customer.id) },
                    enabled = actionState.value !is CustomerActionState.Loading
                ) {
                    if (actionState.value is CustomerActionState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Simpan")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    customerViewModel.resetCustomerForm()
                    customerViewModel.resetActionState()
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

/*
 * Card satu item log riwayat perubahan pelanggan.
 */
@Composable
fun CustomerLogItem(log: CustomerLog) {
    val typeLabel = when (log.type) {
        "created" -> "Pelanggan Dibuat"
        "updated" -> "Data Diperbarui"
        else -> log.type
    }
    val typeColor = when (log.type) {
        "created" -> Color(0xFF2E7D32)
        "updated" -> Color(0xFF1565C0)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = typeColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.createdAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}