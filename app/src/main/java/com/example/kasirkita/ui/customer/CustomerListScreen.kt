package com.example.kasirkita.ui.customer

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.Customer
import com.example.kasirkita.viewmodel.CustomerActionState
import com.example.kasirkita.viewmodel.CustomerListUiState
import com.example.kasirkita.viewmodel.CustomerViewModel

/*
 * CustomerListScreen menampilkan daftar semua pelanggan.
 * Owner dan cashier sama-sama bisa lihat dan tambah pelanggan.
 * RLS Supabase menjaga batasan created_by.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    customerViewModel: CustomerViewModel,
    onCustomerClick: (Customer) -> Unit,
    onBackClick: () -> Unit
) {
    val customerListState = customerViewModel.customerListState.collectAsStateWithLifecycle()
    val actionState = customerViewModel.actionState.collectAsStateWithLifecycle()
    val customerName = customerViewModel.customerName.collectAsStateWithLifecycle()
    val customerPhone = customerViewModel.customerPhone.collectAsStateWithLifecycle()

    var showTambahDialog by remember { mutableStateOf(false) }

    // Load data saat screen pertama kali tampil
    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
    }

    // Tutup dialog kalau aksi berhasil
    LaunchedEffect(actionState.value) {
        if (actionState.value is CustomerActionState.Success) {
            showTambahDialog = false
            customerViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Pelanggan") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showTambahDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pelanggan")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = customerListState.value) {
                is CustomerListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CustomerListUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is CustomerListUiState.Success -> {
                    if (state.customers.isEmpty()) {
                        Text(
                            text = "Belum ada pelanggan. Tap + untuk menambah.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.customers) { customer ->
                                CustomerItem(
                                    customer = customer,
                                    onClick = { onCustomerClick(customer) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog tambah pelanggan baru
    if (showTambahDialog) {
        TambahCustomerDialog(
            customerName = customerName.value,
            customerPhone = customerPhone.value,
            actionState = actionState.value,
            onNameChange = customerViewModel::onCustomerNameChange,
            onPhoneChange = customerViewModel::onCustomerPhoneChange,
            onConfirm = { customerViewModel.createCustomer() },
            onDismiss = {
                showTambahDialog = false
                customerViewModel.resetCustomerForm()
                customerViewModel.resetActionState()
            }
        )
    }
}

/*
 * Card satu item pelanggan.
 */
@Composable
fun CustomerItem(
    customer: Customer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = customer.phoneNumber ?: "Tidak ada nomor HP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Badge status aktif/nonaktif
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (customer.isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Text(
                    text = if (customer.isActive) "Aktif" else "Nonaktif",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (customer.isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
    }
}

/*
 * Dialog untuk tambah pelanggan baru.
 */
@Composable
fun TambahCustomerDialog(
    customerName: String,
    customerPhone: String,
    actionState: CustomerActionState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Pelanggan Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onNameChange,
                    label = { Text("Nama Pelanggan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = onPhoneChange,
                    label = { Text("Nomor HP (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (actionState is CustomerActionState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = actionState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = actionState !is CustomerActionState.Loading
            ) {
                if (actionState is CustomerActionState.Loading) {
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
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}