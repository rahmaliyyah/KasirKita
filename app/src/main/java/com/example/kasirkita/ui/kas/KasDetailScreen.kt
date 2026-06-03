package com.example.kasirkita.ui.kas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.CashLog
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.formatDate
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.KasActionState
import com.example.kasirkita.viewmodel.KasLogUiState
import com.example.kasirkita.viewmodel.KasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasDetailScreen(
    kasViewModel: KasViewModel,
    isOwner: Boolean,
    onBackClick: () -> Unit,
    onSaleClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit
) {
    val selectedKas by kasViewModel.selectedKas.collectAsStateWithLifecycle()
    val kasLogState by kasViewModel.kasLogState.collectAsStateWithLifecycle()
    val actionState by kasViewModel.actionState.collectAsStateWithLifecycle()
    val transactionAmount by kasViewModel.transactionAmount.collectAsStateWithLifecycle()
    val transactionDescription by kasViewModel.transactionDescription.collectAsStateWithLifecycle()
    val kasName by kasViewModel.kasName.collectAsStateWithLifecycle()

    var showTransaksiDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("manual_in") }
    var showEditDialog by remember { mutableStateOf(false) }

    val kas = selectedKas

    LaunchedEffect(kas?.id) {
        kas?.id?.let { kasViewModel.loadKasLogs(it) }
    }

    LaunchedEffect(actionState) {
        if (actionState is KasActionState.Success) {
            showTransaksiDialog = false
            showEditDialog = false
            kasViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                title = kas?.name ?: "Detail Kas",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (kas == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(text = "Saldo Saat Ini", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = kas.currentBalance.toRupiah(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = if (kas.currentBalance >= 0) Success else Error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (kas.isActive) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (kas.isActive) "Kas Aktif" else "Kas Nonaktif",
                                    color = if (kas.isActive) Success else Error,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (isOwner) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SmallActionCard(
                                title = "Masuk",
                                icon = Icons.Default.Add,
                                color = Success,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    transactionType = "manual_in"
                                    showTransaksiDialog = true
                                }
                            )
                            SmallActionCard(
                                title = "Keluar",
                                icon = Icons.Default.Remove,
                                color = Error,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    transactionType = "manual_out"
                                    showTransaksiDialog = true
                                }
                            )
                        }
                    }
                    
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { 
                                    kasViewModel.onKasNameChange(kas.name)
                                    showEditDialog = true 
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Edit Nama")
                            }
                            
                            Button(
                                onClick = { kasViewModel.toggleKasActive(kas.id, kas.isActive) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (kas.isActive) Error.copy(alpha = 0.1f) else Success.copy(alpha = 0.1f),
                                    contentColor = if (kas.isActive) Error else Success
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(if (kas.isActive) "Nonaktifkan" else "Aktifkan")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Riwayat Transaksi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                when (val logState = kasLogState) {
                    is KasLogUiState.Loading -> {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GoldPrimary)
                            }
                        }
                    }
                    is KasLogUiState.Error -> {
                        item {
                            Text(text = logState.message, color = Error)
                        }
                    }
                    is KasLogUiState.Success -> {
                        if (logState.logs.isEmpty()) {
                            item {
                                Text(text = "Belum ada riwayat transaksi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            items(logState.logs) { log ->
                                CashLogItem(
                                    log = log,
                                    onClick = {
                                        if (log.type == "sale" && log.referenceId != null) {
                                            onSaleClick(log.referenceId)
                                        } else if (log.type == "expense" && log.referenceId != null) {
                                            onExpenseClick(log.referenceId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTransaksiDialog) {
        val title = if (transactionType == "manual_in") "Tambah Saldo" else "Kurangi Saldo"
        AlertDialog(
            onDismissRequest = { showTransaksiDialog = false },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = transactionAmount,
                        onValueChange = kasViewModel::onTransactionAmountChange,
                        label = { Text("Jumlah (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    OutlinedTextField(
                        value = transactionDescription,
                        onValueChange = kasViewModel::onTransactionDescriptionChange,
                        label = { Text("Keterangan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    if (actionState is KasActionState.Error) {
                        Text(text = (actionState as KasActionState.Error).message, color = Error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { kasViewModel.manualTransaction(transactionType) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    enabled = actionState !is KasActionState.Loading && transactionAmount.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransaksiDialog = false }) { Text("Batal") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showEditDialog && kas != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Nama Kas", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = kasName,
                        onValueChange = kasViewModel::onKasNameChange,
                        label = { Text("Nama Kas") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { kasViewModel.updateKasName(kas.id) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                    Text("Simpan")
                }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Batal") } },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SmallActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(80.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.height(4.dp))
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun CashLogItem(log: CashLog, onClick: () -> Unit) {
    val isIncoming = log.type in listOf("manual_in", "sale")
    val typeLabel = when (log.type) {
        "manual_in"  -> "Masuk Manual"
        "manual_out" -> "Keluar Manual"
        "sale"       -> "Penjualan"
        "expense"    -> "Pengeluaran"
        else         -> log.type
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = typeLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (!log.description.isNullOrBlank()) {
                    Text(text = log.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = log.createdAt.take(10).formatDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "${if (isIncoming) "+" else "−"} ${log.amount.toRupiah()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isIncoming) Success else Error
            )
        }
    }
}
