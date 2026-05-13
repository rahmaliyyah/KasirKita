package com.example.kasirkita.ui.kas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.CashRegister
import com.example.kasirkita.viewmodel.KasActionState
import com.example.kasirkita.viewmodel.KasListUiState
import com.example.kasirkita.viewmodel.KasViewModel
import java.text.NumberFormat
import java.util.Locale

/*
 * Helper untuk format angka ke Rupiah.
 * Contoh: 500000.0 → "Rp500.000"
 */
fun Double.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this)
}

/*
 * KasListScreen menampilkan daftar semua kas.
 * Owner bisa tambah kas baru (FAB muncul).
 * Kasir hanya bisa lihat.
 *
 * State hoisting: screen tidak menyimpan state sendiri,
 * semua berasal dari KasViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasListScreen(
    kasViewModel: KasViewModel,
    isOwner: Boolean,       // true = owner, false = kasir
    onKasClick: (CashRegister) -> Unit,
    onBackClick: () -> Unit
) {
    val kasListState = kasViewModel.kasListState.collectAsStateWithLifecycle()
    val actionState = kasViewModel.actionState.collectAsStateWithLifecycle()
    val kasName = kasViewModel.kasName.collectAsStateWithLifecycle()
    val kasBalance = kasViewModel.kasBalance.collectAsStateWithLifecycle()

    // State lokal untuk kontrol dialog (hanya untuk tampilkan/sembunyikan)
    var showTambahDialog by remember { mutableStateOf(false) }

    // Load data saat screen pertama kali tampil
    LaunchedEffect(Unit) {
        kasViewModel.loadKasRegisters()
    }

    // Tutup dialog dan reset form kalau aksi berhasil
    LaunchedEffect(actionState.value) {
        if (actionState.value is KasActionState.Success) {
            showTambahDialog = false
            kasViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Kas") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB tambah kas hanya muncul untuk owner
            if (isOwner) {
                FloatingActionButton(onClick = { showTambahDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Kas")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = kasListState.value) {
                is KasListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is KasListUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is KasListUiState.Success -> {
                    if (state.registers.isEmpty()) {
                        Text(
                            text = "Belum ada kas. Tap + untuk menambah.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.registers) { kas ->
                                KasItem(
                                    kas = kas,
                                    onClick = { onKasClick(kas) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog tambah kas baru
    if (showTambahDialog) {
        TambahKasDialog(
            kasName = kasName.value,
            kasBalance = kasBalance.value,
            actionState = actionState.value,
            onNameChange = kasViewModel::onKasNameChange,
            onBalanceChange = kasViewModel::onKasBalanceChange,
            onConfirm = { kasViewModel.createKas() },
            onDismiss = {
                showTambahDialog = false
                kasViewModel.resetKasForm()
                kasViewModel.resetActionState()
            }
        )
    }
}

/*
 * Card satu item kas.
 */
@Composable
fun KasItem(
    kas: CashRegister,
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
                    text = kas.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = kas.currentBalance.toRupiah(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // Badge status aktif/nonaktif
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (kas.isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Text(
                    text = if (kas.isActive) "Aktif" else "Nonaktif",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (kas.isActive) {
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
 * Dialog untuk tambah kas baru.
 */
@Composable
fun TambahKasDialog(
    kasName: String,
    kasBalance: String,
    actionState: KasActionState,
    onNameChange: (String) -> Unit,
    onBalanceChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kas Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = kasName,
                    onValueChange = onNameChange,
                    label = { Text("Nama Kas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = kasBalance,
                    onValueChange = onBalanceChange,
                    label = { Text("Saldo Awal (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                // Tampilkan error kalau ada
                if (actionState is KasActionState.Error) {
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
                enabled = actionState !is KasActionState.Loading
            ) {
                if (actionState is KasActionState.Loading) {
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