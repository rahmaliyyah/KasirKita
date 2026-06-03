package com.example.kasirkita.ui.sale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.Sale
import com.example.kasirkita.model.SaleItemWithProduct
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.formatDate
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.SaleDetailUiState
import com.example.kasirkita.viewmodel.SaleViewModel

@Composable
fun SaleDetailScreen(
    saleViewModel: SaleViewModel,
    saleId: String,
    onBackClick: () -> Unit
) {
    val detailState by saleViewModel.saleDetailState.collectAsStateWithLifecycle()

    LaunchedEffect(saleId) {
        saleViewModel.loadSaleDetail(saleId)
    }

    Scaffold(
        topBar = {
            ModernTopBar(
                title = "Detail Penjualan",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (detailState) {
                is SaleDetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is SaleDetailUiState.Success -> {
                    val successState = detailState as SaleDetailUiState.Success
                    SaleDetailContent(
                        sale = successState.sale,
                        items = successState.items
                    )
                }
                is SaleDetailUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((detailState as SaleDetailUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun SaleDetailContent(
    sale: Sale,
    items: List<SaleItemWithProduct>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Elegance Header Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GoldGradientStart, GoldGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Receipt, 
                            contentDescription = null, 
                            tint = Color.White, 
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = sale.totalAmount.toRupiah(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = sale.soldAt.formatDate(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        color = Success.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "TRANSAKSI BERHASIL",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Success,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 2. Transaction Info
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Informasi Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SaleInfoRow("ID Transaksi", "#SL-${sale.id.take(8).uppercase()}")
                        SaleInfoRow("Diterima", sale.amountPaid.toRupiah(), color = MaterialTheme.colorScheme.onSurface)
                        SaleInfoRow("Kembalian", sale.changeAmount.toRupiah(), color = Success)
                    }
                }
            }
        }

        // 3. Items List
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Produk Terjual (${items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        items.forEachIndexed { index, item ->
                            SaleItemRow(item)
                            if (index < items.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaleInfoRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun SaleItemRow(item: SaleItemWithProduct) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.product.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${item.saleItem.quantity.toInt()} x ${item.saleItem.priceAtSale.toRupiah()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            item.saleItem.subtotal.toRupiah(),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = GoldPrimary
        )
    }
}
