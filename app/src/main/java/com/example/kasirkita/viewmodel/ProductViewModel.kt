package com.example.kasirkita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasirkita.model.Product
import com.example.kasirkita.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val productRepository = ProductRepository()

    // State untuk daftar produk
    private val _productListState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val productListState: StateFlow<ProductListUiState> = _productListState.asStateFlow()

    // State untuk detail produk
    private val _productDetailState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val productDetailState: StateFlow<ProductDetailUiState> = _productDetailState.asStateFlow()

    // State untuk aksi (tambah/edit/deactivate)
    private val _actionState = MutableStateFlow<ProductActionState>(ProductActionState.Idle)
    val actionState: StateFlow<ProductActionState> = _actionState.asStateFlow()

    // Form input: untuk tambah/edit produk
    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName.asStateFlow()

    private val _productPrice = MutableStateFlow("")
    val productPrice: StateFlow<String> = _productPrice.asStateFlow()

    private val _productStock = MutableStateFlow("")
    val productStock: StateFlow<String> = _productStock.asStateFlow()

    // Detail produk yang sedang dilihat
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    /**
     * Load semua produk aktif (untuk cashier).
     */
    fun loadActiveProducts() {
        viewModelScope.launch {
            try {
                if (_productListState.value !is ProductListUiState.Success) {
                    _productListState.value = ProductListUiState.Loading
                }
                val products = productRepository.getActiveProducts()
                _productListState.value = ProductListUiState.Success(products)
            } catch (e: Exception) {
                _productListState.value = ProductListUiState.Error(e.message ?: "Error loading products")
            }
        }
    }

    /**
     * Load semua produk (untuk owner).
     */
    fun loadAllProducts() {
        viewModelScope.launch {
            try {
                if (_productListState.value !is ProductListUiState.Success) {
                    _productListState.value = ProductListUiState.Loading
                }
                val products = productRepository.getAllProducts()
                _productListState.value = ProductListUiState.Success(products)
            } catch (e: Exception) {
                _productListState.value = ProductListUiState.Error(e.message ?: "Error loading products")
            }
        }
    }

    /**
     * Load detail produk dengan inventory history.
     */
    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            try {
                _productDetailState.value = ProductDetailUiState.Loading
                val product = productRepository.getProductById(productId)
                val logs = productRepository.getInventoryLogsByProduct(productId)
                _selectedProduct.value = product
                _productDetailState.value = ProductDetailUiState.Success(product, logs)
                // Populate form
                _productName.value = product.name
                _productPrice.value = product.price.toString()
                _productStock.value = product.stock.toString()
            } catch (e: Exception) {
                _productDetailState.value = ProductDetailUiState.Error(e.message ?: "Error loading product")
            }
        }
    }

    /**
     * Tambah produk baru.
     */
    fun createProduct(
        name: String,
        priceStr: String,
        stockStr: String
    ) {
        // Validasi
        if (name.isBlank()) {
            _actionState.value = ProductActionState.Error("Nama produk tidak boleh kosong")
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            _actionState.value = ProductActionState.Error("Harga harus angka positif")
            return
        }

        val stock = stockStr.toDoubleOrNull()
        if (stock == null || stock < 0) {
            _actionState.value = ProductActionState.Error("Stok harus angka positif atau 0")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = ProductActionState.Loading
                productRepository.createProduct(
                    name = name,
                    price = price,
                    stock = stock
                )
                _actionState.value = ProductActionState.Success
                resetFormFields()
                loadAllProducts()
            } catch (e: Exception) {
                _actionState.value = ProductActionState.Error(e.message ?: "Error creating product")
            }
        }
    }

    /**
     * Update produk.
     */
    fun updateProduct(
        productId: String,
        name: String? = null,
        priceStr: String? = null,
        stockStr: String? = null
    ) {
        if (name != null && name.isBlank()) {
            _actionState.value = ProductActionState.Error("Nama produk tidak boleh kosong")
            return
        }

        val price = priceStr?.toDoubleOrNull()
        if (priceStr != null && (price == null || price <= 0)) {
            _actionState.value = ProductActionState.Error("Harga harus angka positif")
            return
        }

        val stock = stockStr?.toDoubleOrNull()
        if (stockStr != null && (stock == null || stock < 0)) {
            _actionState.value = ProductActionState.Error("Stok harus angka positif atau 0")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = ProductActionState.Loading
                productRepository.updateProduct(
                    id = productId,
                    name = name,
                    price = price,
                    stock = stock
                )
                _actionState.value = ProductActionState.Success
                loadAllProducts()
            } catch (e: Exception) {
                _actionState.value = ProductActionState.Error(e.message ?: "Error updating product")
            }
        }
    }

    /**
     * Update status aktif/nonaktif produk.
     */
    fun toggleProductActive(productId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                _actionState.value = ProductActionState.Loading
                productRepository.toggleProductActive(productId, isActive)
                _actionState.value = ProductActionState.Success
                loadProductDetail(productId) // Refresh detail
                loadAllProducts() // Refresh list
            } catch (e: Exception) {
                _actionState.value = ProductActionState.Error(e.message ?: "Gagal mengubah status produk")
            }
        }
    }

    /**
     * Tambah/Kurangi stok secara cepat.
     */
    fun adjustStock(productId: String, amount: Double) {
        val currentProduct = (_productDetailState.value as? ProductDetailUiState.Success)?.product ?: return
        val newStock = currentProduct.stock + amount
        if (newStock < 0) {
            _actionState.value = ProductActionState.Error("Stok tidak boleh negatif")
            return
        }

        viewModelScope.launch {
            try {
                _actionState.value = ProductActionState.Loading
                productRepository.updateProduct(productId, null, null, newStock)
                _actionState.value = ProductActionState.Success
                loadProductDetail(productId)
                loadAllProducts()
            } catch (e: Exception) {
                _actionState.value = ProductActionState.Error(e.message ?: "Gagal update stok")
            }
        }
    }

    // Form setters
    fun setProductName(name: String) {
        _productName.value = name
    }

    fun setProductPrice(price: String) {
        _productPrice.value = price
    }

    fun setProductStock(stock: String) {
        _productStock.value = stock
    }

    fun resetFormFields() {
        _productName.value = ""
        _productPrice.value = ""
        _productStock.value = ""
    }

    fun resetActionState() {
        _actionState.value = ProductActionState.Idle
    }
}
