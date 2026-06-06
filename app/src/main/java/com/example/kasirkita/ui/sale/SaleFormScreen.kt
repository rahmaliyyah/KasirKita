package com.example.kasirkita.ui.sale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.kasirkita.model.*
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleFormScreen(
    saleViewModel: SaleViewModel,
    productViewModel: ProductViewModel,
    customerViewModel: CustomerViewModel,
    kasViewModel: KasViewModel,
    onSuccess: (String) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val cartItems by saleViewModel.cartItems.collectAsState()
    val totalAmount by saleViewModel.totalAmount.collectAsState()
    val amountPaid by saleViewModel.amountPaid.collectAsState()
    val changeAmount by saleViewModel.changeAmount.collectAsState()
    val selectedCashRegisterId by saleViewModel.selectedCashRegisterId.collectAsState()
    val selectedCustomerId by saleViewModel.selectedCustomerId.collectAsState()
    val transactionState by saleViewModel.transactionState.collectAsState()

    val productListState by productViewModel.productListState.collectAsState()
    val customerListState by customerViewModel.customerListState.collectAsState()
    val kasListState by kasViewModel.kasListState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showKasDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    
    var customerSearchQuery by remember { mutableStateOf("") }
    var kasSearchQuery by remember { mutableStateOf("") }

    val customerName by customerViewModel.customerName.collectAsState()
    val customerPhone by customerViewModel.customerPhone.collectAsState()
    val customerActionState by customerViewModel.actionState.collectAsState()

    LaunchedEffect(Unit) {
        productViewModel.loadActiveProducts()
        customerViewModel.loadCustomers()
        kasViewModel.loadKasRegisters()
    }

    LaunchedEffect(transactionState) {
        if (transactionState is SaleTransactionUiState.Success) {
            val saleId = (transactionState as SaleTransactionUiState.Success).saleId
            onSuccess(saleId)
            saleViewModel.resetTransactionState()
        }
    }

    LaunchedEffect(customerActionState) {
        if (customerActionState is CustomerActionState.Success) {
            showAddCustomerDialog = false
            customerViewModel.resetActionState()
            customerViewModel.loadCustomers()
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar(title = "Kasir")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            isSearchFocused = it.isNotEmpty()
                        },
                        placeholder = { Text("Cari & Tambah Produk...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    isSearchFocused = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )

                    if (isSearchFocused && searchQuery.isNotEmpty()) {
                        Popup(
                            alignment = Alignment.TopCenter,
                            offset = androidx.compose.ui.unit.IntOffset(0, 160),
                            onDismissRequest = { isSearchFocused = false },
                            properties = PopupProperties(focusable = false)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .heightIn(max = 300.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                when (productListState) {
                                    is ProductListUiState.Success -> {
                                        val products = (productListState as ProductListUiState.Success).products
                                            .filter { it.name.contains(searchQuery, ignoreCase = true) }
                                        
                                        if (products.isEmpty()) {
                                            Box(Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                Text("Produk tidak ditemukan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        } else {
                                            LazyColumn {
                                                items(products) { product ->
                                                    val isOutOfStock = product.stock <= 0
                                                    ListItem(
                                                        headlineContent = { 
                                                            Text(
                                                                product.name, 
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isOutOfStock) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                                            ) 
                                                        },
                                                        supportingContent = { 
                                                            Text(
                                                                formatCurrency(product.price), 
                                                                color = if (isOutOfStock) GoldPrimary.copy(alpha = 0.5f) else GoldPrimary, 
                                                                fontWeight = FontWeight.SemiBold
                                                            ) 
                                                        },
                                                        trailingContent = { 
                                                            if (isOutOfStock) {
                                                                Text("Habis", color = Error, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                            } else {
                                                                Text("Stok: ${product.stock.toInt()}", fontSize = 11.sp)
                                                            }
                                                        },
                                                        modifier = Modifier.clickable(enabled = !isOutOfStock) {
                                                            saleViewModel.addToCart(product, 1.0)
                                                            searchQuery = ""
                                                            isSearchFocused = false
                                                        }
                                                    )
                                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                }
                                            }
                                        }
                                    }
                                    is ProductListUiState.Loading -> {
                                        Box(Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GoldPrimary)
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ringkasan Belanja", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (cartItems.isNotEmpty()) {
                            TextButton(onClick = { saleViewModel.clearCart() }) {
                                Text("Kosongkan", color = Error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (cartItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("Cari produk untuk mulai transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(cartItems) { item ->
                                PremiumCheckoutItem(
                                    item = item, 
                                    onUpdate = { id, qty -> saleViewModel.updateCartItemQuantity(id, qty) },
                                    onRemove = { id -> saleViewModel.removeFromCart(id) }
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 16.dp, bottom = bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectionBox(
                                label = "Pelanggan",
                                value = if (selectedCustomerId.isNullOrEmpty()) "Umum" else {
                                    val customers = (customerListState as? CustomerListUiState.Success)?.customers ?: emptyList()
                                    customers.find { it.id == selectedCustomerId }?.name ?: "Member"
                                },
                                icon = Icons.Default.Person,
                                modifier = Modifier.weight(1f),
                                onClick = { showCustomerDialog = true }
                            )
                            SelectionBox(
                                label = "Pilih Kas",
                                value = if (selectedCashRegisterId.isEmpty()) "Pilih Kas" else {
                                    val registers = (kasListState as? KasListUiState.Success)?.registers ?: emptyList()
                                    registers.find { it.id == selectedCashRegisterId }?.name ?: "Terpilih"
                                },
                                icon = Icons.Default.AccountBalanceWallet,
                                modifier = Modifier.weight(1f),
                                onClick = { showKasDialog = true }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.spacedBy(12.dp), 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Tagihan", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    formatCurrency(totalAmount), 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    style = MaterialTheme.typography.titleLarge, 
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                            
                            OutlinedTextField(
                                value = amountPaid,
                                onValueChange = { saleViewModel.setAmountPaid(it) },
                                placeholder = { Text("Bayar Tunai", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f).height(52.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                        
                        val isPaidEnough = amountPaid.isNotBlank() && (amountPaid.toDoubleOrNull() ?: 0.0) >= totalAmount
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPaidEnough) Success.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (isPaidEnough) Success.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Payments, 
                                        contentDescription = null, 
                                        tint = if (isPaidEnough) Success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), 
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Kembalian", 
                                        fontSize = 12.sp, 
                                        color = if (isPaidEnough) Success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    formatCurrency(if (isPaidEnough) changeAmount else 0.0), 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = if (isPaidEnough) Success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Button(
                            onClick = { saleViewModel.checkout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            enabled = transactionState !is SaleTransactionUiState.Loading && 
                                      selectedCashRegisterId.isNotEmpty() && 
                                      cartItems.isNotEmpty() &&
                                      (amountPaid.toDoubleOrNull() ?: 0.0) >= totalAmount
                        ) {
                            if (transactionState is SaleTransactionUiState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Bayar Sekarang", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        
                        if (transactionState is SaleTransactionUiState.Error) {
                            Text(
                                (transactionState as SaleTransactionUiState.Error).message, 
                                color = MaterialTheme.colorScheme.error, 
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomerDialog) {
        PremiumSelectionDialog(
            title = "Pilih Pelanggan",
            onDismiss = { 
                showCustomerDialog = false 
                customerSearchQuery = ""
            },
            searchQuery = customerSearchQuery,
            onSearchQueryChange = { customerSearchQuery = it },
            searchPlaceholder = "Cari nama atau nomor HP..."
        ) {
            item {
                SelectionListItem(
                    title = "Tambah Pelanggan Baru",
                    subtitle = "Daftarkan pelanggan di sini",
                    icon = Icons.Default.PersonAdd,
                    onClick = {
                        showAddCustomerDialog = true
                    }
                )
            }
            val customers = (customerListState as? CustomerListUiState.Success)?.customers
                ?.filter { 
                    it.name.contains(customerSearchQuery, ignoreCase = true) || 
                    (it.phoneNumber?.contains(customerSearchQuery) ?: false)
                } ?: emptyList()
                
            item {
                SelectionListItem(
                    title = "Pelanggan Umum",
                    subtitle = "Transaksi tanpa member",
                    icon = Icons.Default.Public,
                    onClick = {
                        saleViewModel.setSelectedCustomer(null)
                        showCustomerDialog = false
                        customerSearchQuery = ""
                    }
                )
            }
            items(customers) { customer ->
                SelectionListItem(
                    title = customer.name,
                    subtitle = customer.phoneNumber ?: "No phone",
                    icon = Icons.Default.Person,
                    onClick = {
                        saleViewModel.setSelectedCustomer(customer.id)
                        showCustomerDialog = false
                        customerSearchQuery = ""
                    }
                )
            }
        }
    }

    if (showKasDialog) {
        PremiumSelectionDialog(
            title = "Pilih Kas Pembayaran",
            onDismiss = { 
                showKasDialog = false 
                kasSearchQuery = ""
            },
            searchQuery = kasSearchQuery,
            onSearchQueryChange = { kasSearchQuery = it },
            searchPlaceholder = "Cari nama kas..."
        ) {
            val registers = (kasListState as? KasListUiState.Success)?.registers
                ?.filter { it.isActive && it.name.contains(kasSearchQuery, ignoreCase = true) } ?: emptyList()
                
            items(registers) { register ->
                SelectionListItem(
                    title = register.name,
                    subtitle = "Saldo: ${formatCurrency(register.currentBalance)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    onClick = {
                        saleViewModel.setSelectedCashRegister(register.id)
                        showKasDialog = false
                        kasSearchQuery = ""
                    }
                )
            }
        }
    }

    if (showAddCustomerDialog) {
        com.example.kasirkita.ui.customer.TambahCustomerDialog(
            customerName = customerName,
            customerPhone = customerPhone,
            actionState = customerActionState,
            onNameChange = customerViewModel::onCustomerNameChange,
            onPhoneChange = customerViewModel::onCustomerPhoneChange,
            onConfirm = { customerViewModel.createCustomer() },
            onDismiss = {
                showAddCustomerDialog = false
                customerViewModel.resetCustomerForm()
                customerViewModel.resetActionState()
            }
        )
    }
}

@Composable
fun PremiumCheckoutItem(item: CartItem, onUpdate: (String, Double) -> Unit, onRemove: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(formatCurrency(item.product.price), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = { if (item.quantity > 1) onUpdate(item.product.id, item.quantity - 1) else onRemove(item.product.id) },
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = GoldPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(10.dp))
                }
            }
            
            Text(
                item.quantity.toInt().toString(), 
                fontWeight = FontWeight.Black, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(min = 20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Surface(
                onClick = { onUpdate(item.product.id, item.quantity + 1) },
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = GoldPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
fun SelectionBox(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            }
        }
    }
}

@Composable
fun PremiumSelectionDialog(
    title: String,
    onDismiss: () -> Unit,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    searchPlaceholder: String = "Cari...",
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (onSearchQueryChange != null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text(searchPlaceholder, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SelectionListItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
}
