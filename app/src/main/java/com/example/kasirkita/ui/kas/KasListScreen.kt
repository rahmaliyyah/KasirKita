package com.example.kasirkita.ui.kas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.CashRegister
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.KasActionState
import com.example.kasirkita.viewmodel.KasListUiState
import com.example.kasirkita.viewmodel.KasViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun KasListScreen(
    kasViewModel: KasViewModel,
    isOwner: Boolean,
    onKasClick: (CashRegister) -> Unit,
    onBackClick: () -> Unit
) {
    val kasListState = kasViewModel.kasListState.collectAsStateWithLifecycle()
    val actionState = kasViewModel.actionState.collectAsStateWithLifecycle()
    val kasName = kasViewModel.kasName.collectAsStateWithLifecycle()
    val kasBalance = kasViewModel.kasBalance.collectAsStateWithLifecycle()

    var showTambahDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kasViewModel.loadKasRegisters()
    }

    LaunchedEffect(actionState.value) {
        if (actionState.value is KasActionState.Success) {
            showTambahDialog = false
            kasViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                title = "Kelola Kas & Saldo",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (isOwner) {
                FloatingActionButton(
                    onClick = { showTambahDialog = true },
                    containerColor = GoldPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Kas")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = kasListState.value) {
                is KasListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is KasListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Error)
                    }
                }
                is KasListUiState.Success -> {
                    if (state.registers.isEmpty()) {
                        EmptyKasState { showTambahDialog = true }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.registers) { kas ->
                                KasItemModern(kas) { onKasClick(kas) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTambahDialog) {
        TambahKasDialogModern(
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

@Composable
fun KasItemModern(kas: CashRegister, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldPrimary)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kas.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatCurrency(kas.currentBalance),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (kas.currentBalance >= 0) Success else Error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            if (!kas.isActive) {
                Surface(
                    color = Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Nonaktif",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = Error)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyKasState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.AccountBalance, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum Ada Kas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
        ) {
            Text("Tambah Kas Sekarang")
        }
    }
}

@Composable
fun TambahKasDialogModern(
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
        title = { Text("Tambah Kas Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = kasName,
                    onValueChange = onNameChange,
                    label = { Text("Nama Kas (misal: Kas Utama)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
                OutlinedTextField(
                    value = kasBalance,
                    onValueChange = onBalanceChange,
                    label = { Text("Saldo Awal") },
                    prefix = { Text("Rp ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
                if (actionState is KasActionState.Error) {
                    Text(actionState.message, color = Error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                if (actionState is KasActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Simpan Kas")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
}
