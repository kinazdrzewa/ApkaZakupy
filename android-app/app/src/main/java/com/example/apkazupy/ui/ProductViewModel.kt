package com.example.apkazupy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkazupy.data.Product
import com.example.apkazupy.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.apkazupy.network.ShoppingListDto
import com.example.apkazupy.network.AddItemRequest
import com.example.apkazupy.network.ApiService
import com.example.apkazupy.network.ListItemDto

class ProductViewModel : ViewModel() {
    private val api = NetworkModule.productApi

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // local shopping list (in-memory for demo)
    private val _localList = MutableStateFlow<List<Product>>(emptyList())
    val localList: StateFlow<List<Product>> = _localList

    // named shopping lists (only names are stored for now)
    private val _namedLists = MutableStateFlow<List<String>>(emptyList())
    val namedLists: StateFlow<List<String>> = _namedLists

    // detailed lists (id + name) for selection when adding items
    private val _listDetails = MutableStateFlow<List<ShoppingListDto>>(emptyList())
    val listDetails: StateFlow<List<ShoppingListDto>> = _listDetails

    // items for the currently opened list
    private val _listItems = MutableStateFlow<List<ListItemDto>>(emptyList())
    val listItems: StateFlow<List<ListItemDto>> = _listItems

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

    fun addNamedList(name: String) {
        if (name.isBlank()) return
        _namedLists.value = _namedLists.value + name.trim()
    }

    fun addNamedListRemote(userId: Long?, name: String, onComplete: (Boolean, String?) -> Unit) {
        if (name.isBlank()) {
            onComplete(false, "Empty name")
            return
        }
        viewModelScope.launch {
            try {
                val resp = NetworkModule.apiService.createList(com.example.apkazupy.network.CreateListRequest(name = name.trim(), userId = userId))
                if (resp.isSuccessful) {
                    // reload lists from server to keep server as source of truth
                    loadNamedListsRemote(userId)
                    loadListDetailsRemote(userId)
                    onComplete(true, null)
                } else {
                    onComplete(false, "Server: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun loadNamedListsRemote(userId: Long?) {
        viewModelScope.launch {
            try {
                val lists = NetworkModule.apiService.getLists(userId)
                _namedLists.value = lists
            } catch (e: Exception) {
                // ignore or log; keep local lists as fallback
            }
        }
    }

    fun clearNamedLists() {
        _namedLists.value = emptyList()
    }

    fun loadListDetailsRemote(userId: Long?) {
        viewModelScope.launch {
            try {
                val details = NetworkModule.apiService.getListDetails(userId)
                _listDetails.value = details
            } catch (e: Exception) {
                // ignore or log
            }
        }
    }

    fun addProductToListRemote(listId: Long, productId: Long, quantity: Int = 1, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = NetworkModule.apiService.addItemToList(listId, AddItemRequest(productId = productId, quantity = quantity))
                if (resp.isSuccessful) {
                    // reload items for that list
                    loadListItemsRemote(listId)
                    onComplete(true, null)
                } else {
                    onComplete(false, "Server: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun deleteListRemote(listId: Long, userId: Long?, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = NetworkModule.apiService.deleteList(listId)
                if (resp.isSuccessful) {
                    // reload lists
                    loadNamedListsRemote(userId)
                    loadListDetailsRemote(userId)
                    onComplete(true, null)
                } else {
                    onComplete(false, "Server: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun loadListItemsRemote(listId: Long) {
        viewModelScope.launch {
            try {
                val items = NetworkModule.apiService.getListItems(listId)
                _listItems.value = items
            } catch (e: Exception) {
                // ignore or log
            }
        }
    }

    fun updateItemQuantityRemote(listId: Long, itemId: Long, newQuantity: Int, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = NetworkModule.apiService.updateItemQuantity(listId, itemId, com.example.apkazupy.network.ApiService.UpdateQuantityRequest(newQuantity))
                if (resp.isSuccessful) {
                    loadListItemsRemote(listId)
                    onComplete(true, null)
                } else {
                    onComplete(false, "Server: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun deleteItemRemote(listId: Long, itemId: Long, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = NetworkModule.apiService.deleteItem(listId, itemId)
                if (resp.isSuccessful) {
                    loadListItemsRemote(listId)
                    onComplete(true, null)
                } else {
                    onComplete(false, "Server: ${resp.code()} ${resp.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
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
