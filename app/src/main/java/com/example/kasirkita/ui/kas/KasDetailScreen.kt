package com.example.kasirkita.ui.kas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.CashLog
import com.example.kasirkita.viewmodel.KasActionState
import com.example.kasirkita.viewmodel.KasLogUiState
import com.example.kasirkita.viewmodel.KasViewModel

/*
 * KasDetailScreen menampilkan:
 * 1. Info saldo kas yang dipilih
 * 2. Tombol transaksi manual (owner only)
 * 3. Tombol edit nama & nonaktifkan (owner only)
 * 4. Log semua transaksi kas ini
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasDetailScreen(
    kasViewModel: KasViewModel,
    isOwner: Boolean,
    onBackClick: () -> Unit
) {
    val selectedKas = kasViewModel.selectedKas.collectAsStateWithLifecycle()
    val kasLogState = kasViewModel.kasLogState.collectAsStateWithLifecycle()
    val actionState = kasViewModel.actionState.collectAsStateWithLifecycle()
    val transactionAmount = kasViewModel.transactionAmount.collectAsStateWithLifecycle()
    val transactionDescription = kasViewModel.transactionDescription.collectAsStateWithLifecycle()
    val kasName = kasViewModel.kasName.collectAsStateWithLifecycle()

    // State lokal untuk dialog
    var showTransaksiDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("manual_in") }
    var showEditDialog by remember { mutableStateOf(false) }

    val kas = selectedKas.value

    // Load log saat screen tampil
    LaunchedEffect(kas?.id) {
        kas?.id?.let { kasViewModel.loadKasLogs(it) }
    }

    // Reset state setelah aksi berhasil
    LaunchedEffect(actionState.value) {
        if (actionState.value is KasActionState.Success) {
            showTransaksiDialog = false
            showEditDialog = false
            kasViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(kas?.name ?: "Detail Kas") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (kas == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Kas tidak ditemukan")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Card info saldo ──────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Saldo Saat Ini",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = kas.currentBalance.toRupiah(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (kas.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                        ) {
                            Text(
                                text = if (kas.isActive) "Aktif" else "Nonaktif",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // ── Tombol aksi (hanya owner) ────────────────────────────
            if (isOwner) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Tombol tambah saldo manual
                        Button(
                            onClick = {
                                transactionType = "manual_in"
                                showTransaksiDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Tambah Saldo Manual")
                        }
                        // Tombol kurangi saldo manual
                        OutlinedButton(
                            onClick = {
                                transactionType = "manual_out"
                                showTransaksiDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("− Kurangi Saldo Manual")
                        }
                        // Tombol edit nama
                        OutlinedButton(
                            onClick = {
                                kasViewModel.onKasNameChange(kas.name)
                                showEditDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Nama Kas")
                        }
                        // Tombol aktifkan / nonaktifkan
                        OutlinedButton(
                            onClick = { kasViewModel.toggleKasActive(kas.id, kas.isActive) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (kas.isActive) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        ) {
                            Text(if (kas.isActive) "Nonaktifkan Kas" else "Aktifkan Kas")
                        }
                    }
                }
            }

            // ── Header log transaksi ─────────────────────────────────
            item {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── List log ─────────────────────────────────────────────
            when (val logState = kasLogState.value) {
                is KasLogUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is KasLogUiState.Error -> {
                    item {
                        Text(
                            text = logState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is KasLogUiState.Success -> {
                    if (logState.logs.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada transaksi.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(logState.logs) { log ->
                            CashLogItem(log = log)
                        }
                    }
                }
            }
        }
    }

    // ── Dialog transaksi manual ──────────────────────────────────────
    if (showTransaksiDialog) {
        val title = if (transactionType == "manual_in") "Tambah Saldo" else "Kurangi Saldo"
        AlertDialog(
            onDismissRequest = {
                showTransaksiDialog = false
                kasViewModel.resetTransactionForm()
                kasViewModel.resetActionState()
            },
            title = { Text(title) },
            text = {
                Column {
                    OutlinedTextField(
                        value = transactionAmount.value,
                        onValueChange = kasViewModel::onTransactionAmountChange,
                        label = { Text("Jumlah (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = transactionDescription.value,
                        onValueChange = kasViewModel::onTransactionDescriptionChange,
                        label = { Text("Keterangan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (actionState.value is KasActionState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (actionState.value as KasActionState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { kasViewModel.manualTransaction(transactionType) },
                    enabled = actionState.value !is KasActionState.Loading
                ) {
                    if (actionState.value is KasActionState.Loading) {
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
                    showTransaksiDialog = false
                    kasViewModel.resetTransactionForm()
                    kasViewModel.resetActionState()
                }) {
                    Text("Batal")
                }
            }
        )
    }

    // ── Dialog edit nama kas ─────────────────────────────────────────
    if (showEditDialog && kas != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                kasViewModel.resetKasForm()
                kasViewModel.resetActionState()
            },
            title = { Text("Edit Nama Kas") },
            text = {
                Column {
                    OutlinedTextField(
                        value = kasName.value,
                        onValueChange = kasViewModel::onKasNameChange,
                        label = { Text("Nama Kas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (actionState.value is KasActionState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (actionState.value as KasActionState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { kasViewModel.updateKasName(kas.id) },
                    enabled = actionState.value !is KasActionState.Loading
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    kasViewModel.resetKasForm()
                    kasViewModel.resetActionState()
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

/*
 * Card satu baris log transaksi.
 * Masuk (manual_in, sale) = warna hijau
 * Keluar (manual_out, expense) = warna merah
 */
@Composable
fun CashLogItem(log: CashLog) {
    val isIncoming = log.type in listOf("manual_in", "sale")
    val typeLabel = when (log.type) {
        "manual_in"  -> "Masuk Manual"
        "manual_out" -> "Keluar Manual"
        "sale"       -> "Penjualan"
        "expense"    -> "Pengeluaran"
        else         -> log.type
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
                    fontWeight = FontWeight.Medium
                )
                if (!log.description.isNullOrBlank()) {
                    Text(
                        text = log.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Saldo: ${log.balanceAfter.toRupiah()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = log.createdAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${if (isIncoming) "+" else "−"} ${log.amount.toRupiah()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isIncoming) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}