package com.example.kasirkita.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import com.example.kasirkita.model.Expense
import com.example.kasirkita.model.Sale
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.utils.formatDate
import com.example.kasirkita.utils.toRupiah

@Composable
fun ModernTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.weight(1f),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

@Composable
fun BalanceCard(
    totalBalance: Double,
    title: String = "Total Saldo Toko",
    subtitle: String = "Kelola finansialmu dengan bijak",
) {
    var isVisible by remember { mutableStateOf(value = true) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(12.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GoldGradientStart, GoldGradientEnd),
                    )
                )
                .padding(24.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { isVisible = !isVisible },
                        )
                    }
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "IDR",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Rp ",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    Text(
                        text = if (isVisible) totalBalance.toRupiah().replace("Rp", "").trim() else "••••••",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(
    transactions: List<DashboardTransaction>, 
    onSeeAllClick: () -> Unit,
    onSaleClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Transaksi Terakhir",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            )
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.clickable { onSeeAllClick() },
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (transactions.isEmpty()) {
            Text("Belum ada transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 20.dp))
        } else {
            transactions.forEach { tx ->
                TransactionItem(
                    title = tx.title,
                    subtitle = tx.subtitle,
                    amount = tx.amountStr,
                    icon = tx.icon,
                    isIncome = tx is DashboardTransaction.SaleTx,
                    isCancelled = tx.isCancelled,
                    onClick = {
                        when (tx) {
                            is DashboardTransaction.SaleTx -> onSaleClick(tx.sale.id)
                            is DashboardTransaction.ExpenseTx -> onExpenseClick(tx.expense.id)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    subtitle: String,
    amount: String,
    icon: ImageVector,
    isIncome: Boolean,
    isCancelled: Boolean = false,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCancelled) 0.dp else 1.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .graphicsLayer(alpha = if (isCancelled) 0.4f else 1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) Success.copy(alpha = 0.1f) 
                            else Error.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isIncome) Success else Error,
                        modifier = Modifier.size(24.dp),
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        maxLines = 1,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                }
                
                Text(
                    text = (if (isIncome) "+" else "-") + amount,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isIncome) Success else Error,
                    )
                )
            }

            if (isCancelled) {
                Text(
                    text = "Dibatalkan",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 12.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = GoldPrimary,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
        ),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

sealed class DashboardTransaction {
    abstract val title: String
    abstract val subtitle: String
    abstract val amountStr: String
    abstract val icon: ImageVector
    abstract val timestamp: String
    abstract val isCancelled: Boolean

    data class SaleTx(val sale: Sale) : DashboardTransaction() {
        override val title = "Penjualan #${sale.id.take(6).uppercase()}"
        override val subtitle = sale.soldAt.formatDate()
        override val amountStr = sale.totalAmount.toRupiah()
        override val icon = Icons.Default.ShoppingBag
        override val timestamp = sale.soldAt
        override val isCancelled = false
    }

    data class ExpenseTx(val expense: Expense) : DashboardTransaction() {
        override val title = expense.description
        override val subtitle = expense.date.formatDate()
        override val amountStr = expense.amount.toRupiah()
        override val icon = Icons.AutoMirrored.Filled.ReceiptLong
        override val timestamp = expense.date
        override val isCancelled = expense.status == "cancelled"
    }
}
