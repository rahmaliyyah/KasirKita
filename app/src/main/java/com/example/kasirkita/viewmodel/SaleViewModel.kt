package com.example.kasirkita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasirkita.model.CartItem
import com.example.kasirkita.model.Product
import com.example.kasirkita.model.Sale
import com.example.kasirkita.model.SaleItemInsert
import com.example.kasirkita.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SaleViewModel : ViewModel() {
    private val saleRepository = SaleRepository()
    private val productRepository = com.example.kasirkita.repository.ProductRepository()

    // State untuk daftar penjualan
    private val _saleListState = MutableStateFlow<SaleListUiState>(SaleListUiState.Loading)
    val saleListState: StateFlow<SaleListUiState> = _saleListState.asStateFlow()

    // State untuk detail penjualan
    private val _saleDetailState = MutableStateFlow<SaleDetailUiState>(SaleDetailUiState.Loading)
    val saleDetailState: StateFlow<SaleDetailUiState> = _saleDetailState.asStateFlow()

    // State untuk proses transaksi
    private val _transactionState = MutableStateFlow<SaleTransactionUiState>(SaleTransactionUiState.Idle)
    val transactionState: StateFlow<SaleTransactionUiState> = _transactionState.asStateFlow()

    // Form input
    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()

    private val _selectedCashRegisterId = MutableStateFlow("")
    val selectedCashRegisterId: StateFlow<String> = _selectedCashRegisterId.asStateFlow()

    // Cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Total amount
    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    // Amount paid by customer
    private val _amountPaid = MutableStateFlow("")
    val amountPaid: StateFlow<String> = _amountPaid.asStateFlow()

    // Change amount
    private val _changeAmount = MutableStateFlow(0.0)
    val changeAmount: StateFlow<Double> = _changeAmount.asStateFlow()

    /**
     * Load semua penjualan.
     */
    fun loadAllSales() {
        viewModelScope.launch {
            try {
                if (_saleListState.value !is SaleListUiState.Success) {
                    _saleListState.value = SaleListUiState.Loading
                }
                val sales = saleRepository.getAllSales()
                _saleListState.value = SaleListUiState.Success(sales)
            } catch (e: Exception) {
                _saleListState.value = SaleListUiState.Error(e.message ?: "Error loading sales")
            }
        }
    }

    /**
     * Load penjualan untuk user tertentu.
     */
    fun loadSalesByUser(userId: String) {
        viewModelScope.launch {
            try {
                _saleListState.value = SaleListUiState.Loading
                val sales = saleRepository.getSalesByUser(userId)
                _saleListState.value = SaleListUiState.Success(sales)
            } catch (e: Exception) {
                _saleListState.value = SaleListUiState.Error(e.message ?: "Error loading sales")
            }
        }
    }

    /**
     * Load penjualan untuk satu kas.
     */
    fun loadSalesByCashRegister(cashRegisterId: String) {
        viewModelScope.launch {
            try {
                _saleListState.value = SaleListUiState.Loading
                val sales = saleRepository.getSalesByCashRegister(cashRegisterId)
                _saleListState.value = SaleListUiState.Success(sales)
            } catch (e: Exception) {
                _saleListState.value = SaleListUiState.Error(e.message ?: "Error loading sales")
            }
        }
    }

    /**
     * Load detail penjualan lengkap dengan items dan produk.
     */
    fun loadSaleDetail(saleId: String) {
        viewModelScope.launch {
            try {
                _saleDetailState.value = SaleDetailUiState.Loading
                val sale = saleRepository.getSaleById(saleId)
                val saleItems = saleRepository.getSaleItems(saleId)

                // Fetch product details for each item
                val itemsWithProduct = saleItems.mapNotNull { item ->
                    try {
                        val product = productRepository.getProductById(item.productId)
                        com.example.kasirkita.model.SaleItemWithProduct(item, product)
                    } catch (e: Exception) {
                        null
                    }
                }

                _saleDetailState.value = SaleDetailUiState.Success(sale, itemsWithProduct)
            } catch (e: Exception) {
                _saleDetailState.value = SaleDetailUiState.Error(e.message ?: "Error loading sale")
            }
        }
    }

    /**
     * Tambah item ke cart.
     */
    fun addToCart(product: Product, quantity: Double) {
        if (quantity <= 0) {
            _transactionState.value = SaleTransactionUiState.Error("Quantity harus lebih dari 0")
            return
        }

        if (product.stock < quantity) {
            _transactionState.value = SaleTransactionUiState.Error("Stok tidak cukup")
            return
        }

        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            val newQuantity = existingItem.quantity + quantity
            if (product.stock < newQuantity) {
                _transactionState.value = SaleTransactionUiState.Error("Stok tidak cukup")
                return
            }
            currentCart[currentCart.indexOf(existingItem)] = existingItem.copy(
                quantity = newQuantity,
                subtotal = newQuantity * product.price
            )
        } else {
            currentCart.add(
                CartItem(
                    product = product,
                    quantity = quantity,
                    subtotal = quantity * product.price
                )
            )
        }

        _cartItems.value = currentCart
        updateTotalAmount()
    }

    /**
     * Hapus item dari cart.
     */
    fun removeFromCart(productId: String) {
        val currentCart = _cartItems.value.toMutableList()
        currentCart.removeAll { it.product.id == productId }
        _cartItems.value = currentCart
        updateTotalAmount()
    }

    /**
     * Update quantity item di cart.
     */
    fun updateCartItemQuantity(productId: String, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }

        val currentCart = _cartItems.value.toMutableList()
        val itemIndex = currentCart.indexOfFirst { it.product.id == productId }

        if (itemIndex >= 0) {
            val product = currentCart[itemIndex].product
            if (product.stock < newQuantity) {
                _transactionState.value = SaleTransactionUiState.Error("Stok tidak cukup")
                return
            }

            currentCart[itemIndex] = currentCart[itemIndex].copy(
                quantity = newQuantity,
                subtotal = newQuantity * product.price
            )
            _cartItems.value = currentCart
            updateTotalAmount()
        }
    }

    /**
     * Clear semua item dari cart.
     */
    fun clearCart() {
        _cartItems.value = emptyList()
        _totalAmount.value = 0.0
        _changeAmount.value = 0.0
        _amountPaid.value = ""
    }

    /**
     * Update total amount (sum dari semua item).
     */
    private fun updateTotalAmount() {
        val total = _cartItems.value.sumOf { it.subtotal }
        _totalAmount.value = total
        calculateChange()
    }

    /**
     * Hitung kembalian.
     */
    private fun calculateChange() {
        val paid = _amountPaid.value.toDoubleOrNull() ?: 0.0
        val change = if (paid >= _totalAmount.value) paid - _totalAmount.value else 0.0
        _changeAmount.value = change
    }

    /**
     * Set customer ID untuk transaksi.
     */
    fun setSelectedCustomer(customerId: String?) {
        _selectedCustomerId.value = customerId
    }

    /**
     * Set cash register ID untuk transaksi.
     */
    fun setSelectedCashRegister(cashRegisterId: String) {
        _selectedCashRegisterId.value = cashRegisterId
    }

    /**
     * Set jumlah yang dibayar dan hitung kembalian.
     */
    fun setAmountPaid(amountStr: String) {
        _amountPaid.value = amountStr
        calculateChange()
    }

    /**
     * Proses checkout dan buat transaksi penjualan.
     */
    fun checkout() {
        // Validasi
        if (_cartItems.value.isEmpty()) {
            _transactionState.value = SaleTransactionUiState.Error("Keranjang belum ada item")
            return
        }

        if (_selectedCashRegisterId.value.isBlank()) {
            _transactionState.value = SaleTransactionUiState.Error("Pilih kas terlebih dahulu")
            return
        }

        val paid = _amountPaid.value.toDoubleOrNull()
        if (paid == null || paid <= 0) {
            _transactionState.value = SaleTransactionUiState.Error("Jumlah pembayaran tidak valid")
            return
        }

        if (paid < _totalAmount.value) {
            _transactionState.value = SaleTransactionUiState.Error("Pembayaran kurang dari total")
            return
        }

        viewModelScope.launch {
            try {
                _transactionState.value = SaleTransactionUiState.Loading

                // Convert cart items to SaleItemInsert
                val saleItems = _cartItems.value.map { cartItem ->
                    // Sale ID akan diisi oleh repository
                    SaleItemInsert(
                        saleId = "",  // Akan diisi setelah sale dibuat
                        productId = cartItem.product.id,
                        quantity = cartItem.quantity,
                        priceAtSale = cartItem.product.price,
                        subtotal = cartItem.subtotal
                    )
                }

                val saleId = saleRepository.createSale(
                    customerId = _selectedCustomerId.value,
                    cashRegisterId = _selectedCashRegisterId.value,
                    totalAmount = _totalAmount.value,
                    amountPaid = paid,
                    changeAmount = _changeAmount.value,
                    items = saleItems
                )

                _transactionState.value = SaleTransactionUiState.Success(saleId)
                clearCart()
                loadAllSales()
            } catch (e: Exception) {
                _transactionState.value = SaleTransactionUiState.Error(e.message ?: "Error creating sale")
            }
        }
    }

    fun resetTransactionState() {
        _transactionState.value = SaleTransactionUiState.Idle
    }
}
