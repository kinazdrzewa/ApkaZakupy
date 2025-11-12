package com.example.apkazupy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkazupy.data.User
import com.example.apkazupy.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel : ViewModel() {
    private val api = NetworkModule.userApi

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun register(login: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp: Response<User> = api.register(User(login = login, password = password))
                if (resp.isSuccessful) {
                    _currentUser.value = resp.body()
                    onResult(true, null)
                } else {
                    onResult(false, "Registration failed: ${resp.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun login(login: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp: Response<User> = api.login(User(login = login, password = password))
                if (resp.isSuccessful) {
                    _currentUser.value = resp.body()
                    onResult(true, null)
                } else {
                    onResult(false, "Login failed: ${resp.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }
}
