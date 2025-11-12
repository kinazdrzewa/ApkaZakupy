package com.example.apkazupy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkazupy.data.Product
import com.example.apkazupy.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val api = NetworkModule.productApi

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // local shopping list (in-memory for demo)
    private val _localList = MutableStateFlow<List<Product>>(emptyList())
    val localList: StateFlow<List<Product>> = _localList

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _products.value = api.getAll()
            } catch (e: Exception) {
                // log or handle error in a real app
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToLocalList(product: Product) {
        _localList.value = _localList.value + product
    }

    fun addProductRemote(product: Product, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                api.add(product)
                onComplete(true)
                loadProducts()
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
