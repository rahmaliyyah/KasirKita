package com.example.kasirkita.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.formatDate
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.ExpenseActionState
import com.example.kasirkita.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseViewModel: ExpenseViewModel,
    expenseId: String,
    onBackClick: () -> Unit,
    onEditSuccess: () -> Unit
) {
    val selectedExpense by expenseViewModel.selectedExpense.collectAsStateWithLifecycle()
    val actionState by expenseViewModel.actionState.collectAsStateWithLifecycle()
    val editDescription by expenseViewModel.expenseDescription.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        expenseViewModel.loadExpenseDetail(expenseId)
    }

    LaunchedEffect(actionState) {
        if (actionState is ExpenseActionState.Success) {
            if (showEditDialog) {
                showEditDialog = false
            } else {
                onEditSuccess()
            }
            expenseViewModel.resetActionState()
        }
    }

    val expense = selectedExpense
    val isCancelled = expense?.status == "cancelled"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ModernTopBar(
                title = "Detail Pengeluaran",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (expense == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Elegance Header Section (Gradient & Big Amount)
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .fillMaxWidth()
                                        .graphicsLayer(alpha = if (isCancelled) 0.4f else 1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .shadow(12.dp, CircleShape)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Error, Error.copy(alpha = 0.7f))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Payments, 
                                            contentDescription = null, 
                                            tint = Color.White, 
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = expense.amount.toRupiah(),
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = Error
                                        )
                                    )

                                    Text(
                                        text = expense.date.formatDate(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (isCancelled) {
                                    Surface(
                                        color = Error,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "DIBATALKAN",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Info Detail Card
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                "Informasi Pengeluaran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    DetailRow(
                                        label = "Deskripsi", 
                                        value = expense.description, 
                                        icon = Icons.Default.Description
                                    )
                                    
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    
                                    DetailRow(
                                        label = "ID Transaksi", 
                                        value = "#EXP-${expense.id.take(8).uppercase()}", 
                                        icon = Icons.Default.Event
                                    )
                                }
                            }
                        }
                    }

                    // 3. Action Buttons
                    if (!isCancelled) {
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showEditDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ubah Deskripsi", fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { showCancelConfirm = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Error.copy(alpha = 0.08f),
                                        contentColor = Error
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Batalkan Pengeluaran", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (actionState is ExpenseActionState.Loading) {
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }

        if (showEditDialog && expense != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Ubah Deskripsi", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Masukkan deskripsi baru untuk pengeluaran ini.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = expenseViewModel::setExpenseDescription,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            placeholder = { Text("Deskripsi...") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            expenseViewModel.updateExpenseDescription(expenseId, editDescription)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = editDescription.isNotBlank()
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showCancelConfirm && expense != null) {
            AlertDialog(
                onDismissRequest = { showCancelConfirm = false },
                title = { Text("Batalkan Pengeluaran?", fontWeight = FontWeight.Bold) },
                text = { Text("Tindakan ini akan mengembalikan saldo kas sebesar ${expense.amount.toRupiah()} dan tidak dapat dibatalkan kembali.") },
                confirmButton = {
                    Button(
                        onClick = {
                            expenseViewModel.cancelExpense(expenseId)
                            showCancelConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ya, Batalkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelConfirm = false }) {
                        Text("Kembali")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}
