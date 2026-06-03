package com.example.kasirkita.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasirkita.model.Product
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.ProductListUiState
import com.example.kasirkita.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun ProductListScreen(
    productViewModel: ProductViewModel,
    onProductClick: (Product) -> Unit,
    onAddProductClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val listState by productViewModel.productListState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    LaunchedEffect(Unit) {
        productViewModel.loadAllProducts()
    }

    val filteredProducts = remember(listState, searchQuery, selectedFilter) {
        if (listState is ProductListUiState.Success) {
            val products = (listState as ProductListUiState.Success).products
            products.filter { product ->
                val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (selectedFilter) {
                    "Aktif" -> product.isActive
                    "Nonaktif" -> !product.isActive
                    else -> true
                }
                matchesSearch && matchesFilter
            }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            Column {
                ModernTopBar(
                    title = "Manajemen Stok"
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        if (listState is ProductListUiState.Loading && filteredProducts.isNotEmpty()) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(2.dp),
                                color = GoldPrimary,
                                trackColor = GoldPrimary.copy(alpha = 0.1f)
                            )
                        }
                        
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari produk...", fontSize = 14.sp) },
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

                        // Filters
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Semua", "Aktif", "Nonaktif").forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = Color.White,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedFilter == filter,
                                        borderColor = MaterialTheme.colorScheme.surfaceVariant,
                                        selectedBorderColor = GoldPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProductClick,
                containerColor = GoldPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = bottomPadding) // Adjust FAB pos
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) // Only top padding for topBar
                .padding(bottom = bottomPadding) // Manually handled bottom padding
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (listState is ProductListUiState.Loading && filteredProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                if (filteredProducts.isEmpty() && listState !is ProductListUiState.Loading) {
                    EmptyProductState()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProducts) { product ->
                            ProductCardModern(product) { onProductClick(product) }
                        }
                    }
                }
            }

            if (listState is ProductListUiState.Error) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((listState as ProductListUiState.Error).message, color = Error)
                }
            }
        }
    }
}

@Composable
fun ProductCardModern(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (product.isActive) 1.dp else 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .graphicsLayer(alpha = if (product.isActive) 1f else 0.4f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon/Image Placeholder
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldPrimary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Harga: ${formatCurrency(product.price)}",
                        style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Stok",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = product.stock.toInt().toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (product.stock > 10) Success else if (product.stock > 0) Warning else Error
                        )
                    )
                }
            }

            if (!product.isActive) {
                Text(
                    text = "Nonaktif",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 12.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = Error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyProductState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Inventory, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Produk Tidak Ditemukan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("Coba cari produk lain atau tambahkan produk baru", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
}
