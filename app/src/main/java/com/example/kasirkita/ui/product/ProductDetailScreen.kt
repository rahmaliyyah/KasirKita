package com.example.kasirkita.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kasirkita.model.Product
import com.example.kasirkita.ui.components.ModernTopBar
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.toRupiah
import com.example.kasirkita.viewmodel.ProductActionState
import com.example.kasirkita.viewmodel.ProductDetailUiState
import com.example.kasirkita.viewmodel.ProductViewModel

@Composable
fun ProductDetailScreen(
    productViewModel: ProductViewModel,
    productId: String,
    onBackClick: () -> Unit,
    isOwner: Boolean = false
) {
    val detailState by productViewModel.productDetailState.collectAsStateWithLifecycle()
    val actionState by productViewModel.actionState.collectAsStateWithLifecycle()

    LaunchedEffect(productId) {
        productViewModel.loadProductDetail(productId)
    }

    LaunchedEffect(actionState) {
        if (actionState is ProductActionState.Success) {
            productViewModel.resetActionState()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            ModernTopBar(
                title = "Detail Produk",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (detailState) {
                is ProductDetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is ProductDetailUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (detailState as ProductDetailUiState.Error).message, color = Error)
                    }
                }
                is ProductDetailUiState.Success -> {
                    val product = (detailState as ProductDetailUiState.Success).product
                    ProductDetailContent(
                        product = product,
                        isOwner = isOwner,
                        onAdjustStock = { amount -> productViewModel.adjustStock(product.id, amount) },
                        onToggleActive = { isActive -> productViewModel.toggleProductActive(product.id, isActive) }
                    )
                }
            }

            if (actionState is ProductActionState.Loading) {
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
    }
}

@Composable
fun ProductDetailContent(
    product: Product,
    isOwner: Boolean,
    onAdjustStock: (Double) -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GoldGradientStart, GoldGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2, 
                            contentDescription = null, 
                            tint = Color.White, 
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = product.price.toRupiah(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        color = GoldPrimary
                    )

                    if (!product.isActive) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Error.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.2f))
                        ) {
                            Text(
                                "PRODUK INI DINONAKTIFKAN",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                fontSize = 11.sp,
                                color = Error,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Manajemen Inventaris",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Stok Tersedia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = product.stock.toInt().toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (product.stock > 0) TextPrimary else Error
                                )
                                Text(
                                    text = " pcs",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                                )
                            }
                        }
                        
                        if (isOwner) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AdjustButton(Icons.Default.Remove, Error) { onAdjustStock(-1.0) }
                                AdjustButton(Icons.Default.Add, Success) { onAdjustStock(1.0) }
                            }
                        }
                    }
                }
            }
        }

        if (isOwner) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        "Visibilitas Jualan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (product.isActive) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (product.isActive) Icons.Default.CheckCircle else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (product.isActive) Success else Error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(20.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (product.isActive) "Status Aktif" else "Status Nonaktif",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (product.isActive) "Produk muncul di menu kasir" else "Produk disembunyikan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Switch(
                                checked = product.isActive,
                                onCheckedChange = { onToggleActive(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Success,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Error.copy(alpha = 0.5f),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.1f),
        contentColor = color
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}
