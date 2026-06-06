package com.example.kasirkita.ui.customer

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
import com.example.kasirkita.model.Customer
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.CustomerActionState
import com.example.kasirkita.viewmodel.CustomerListUiState
import com.example.kasirkita.viewmodel.CustomerViewModel

@Composable
fun CustomerListScreen(
    customerViewModel: CustomerViewModel,
    onCustomerClick: (Customer) -> Unit,
    onBackClick: () -> Unit
) {
    val customerListState by customerViewModel.customerListState.collectAsStateWithLifecycle()
    val actionState by customerViewModel.actionState.collectAsStateWithLifecycle()
    val customerName by customerViewModel.customerName.collectAsStateWithLifecycle()
    val customerPhone by customerViewModel.customerPhone.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showTambahDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
    }

    LaunchedEffect(actionState) {
        if (actionState is CustomerActionState.Success) {
            showTambahDialog = false
            customerViewModel.resetActionState()
            customerViewModel.resetCustomerForm()
        }
    }

    val filteredCustomers = remember(customerListState, searchQuery) {
        if (customerListState is CustomerListUiState.Success) {
            val customers = (customerListState as CustomerListUiState.Success).customers
            customers.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                (it.phoneNumber?.contains(searchQuery) ?: false)
            }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            Column {
                ModernTopBar(
                    title = "Pelanggan",
                    onBackClick = onBackClick
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama atau nomor HP...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTambahDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (customerListState) {
                is CustomerListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is CustomerListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (customerListState as CustomerListUiState.Error).message, color = Error)
                    }
                }
                is CustomerListUiState.Success -> {
                    if (filteredCustomers.isEmpty()) {
                        EmptyCustomerState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredCustomers) { customer ->
                                CustomerCardModern(customer) { onCustomerClick(customer) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTambahDialog) {
        TambahCustomerDialog(
            customerName = customerName,
            customerPhone = customerPhone,
            actionState = actionState,
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

@Composable
fun CustomerCardModern(customer: Customer, onClick: () -> Unit) {
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = customer.phoneNumber ?: "Tidak ada nomor HP",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            
            if (customer.isActive) {
                Surface(
                    color = Success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Member",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = Success,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCustomerState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum Ada Pelanggan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("Mulai tambahkan pelanggan setia Anda", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

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
        title = { Text("Tambah Pelanggan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onNameChange,
                    label = { Text("Nama Pelanggan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = onPhoneChange,
                    label = { Text("Nomor HP (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
                if (actionState is CustomerActionState.Error) {
                    Text(actionState.message, color = Error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                enabled = actionState !is CustomerActionState.Loading
            ) {
                if (actionState is CustomerActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
