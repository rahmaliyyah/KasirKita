package com.example.kasirkita.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kasirkita.ui.theme.*
import com.example.kasirkita.viewmodel.ProductActionState
import com.example.kasirkita.viewmodel.ProductViewModel

@Composable
fun ProductFormScreen(
    productViewModel: ProductViewModel,
    isEditMode: Boolean = false,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val name by productViewModel.productName.collectAsState()
    val price by productViewModel.productPrice.collectAsState()
    val stock by productViewModel.productStock.collectAsState()
    val actionState by productViewModel.actionState.collectAsState()

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(actionState) {
        when (actionState) {
            is ProductActionState.Success -> {
                onSuccess()
            }
            is ProductActionState.Error -> {
                errorMessage = (actionState as ProductActionState.Error).message
                showError = true
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        ProductFormHeader(
            title = if (isEditMode) "Edit Produk" else "Tambah Produk",
            onBackClick = onBackClick
        )

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nama Produk
            Text(
                text = "Nama Produk",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            OutlinedTextField(
                value = name,
                onValueChange = { productViewModel.setProductName(it) },
                placeholder = { Text("Contoh: Roti Sourdough") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = SurfaceVariant,
                    cursorColor = GoldPrimary
                )
            )

            // Harga
            Text(
                text = "Harga (Rp)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            OutlinedTextField(
                value = price,
                onValueChange = { productViewModel.setProductPrice(it) },
                placeholder = { Text("Contoh: 50000") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = SurfaceVariant,
                    cursorColor = GoldPrimary
                )
            )

            // Stok
            Text(
                text = "Stok Awal",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            OutlinedTextField(
                value = stock,
                onValueChange = { productViewModel.setProductStock(it) },
                placeholder = { Text("Contoh: 100") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = SurfaceVariant,
                    cursorColor = GoldPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (showError) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Error.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = Error),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (isEditMode) {
                        // TODO: implement update
                    } else {
                        productViewModel.createProduct(name, price, stock)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                enabled = actionState !is ProductActionState.Loading
            ) {
                if (actionState is ProductActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Perbarui" else "Tambah Produk",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProductFormHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(GoldPrimary, GoldDark)
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
