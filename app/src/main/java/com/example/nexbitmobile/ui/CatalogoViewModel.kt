package com.example.nexbitmobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogoViewModel : ViewModel() {

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProductos() {
        if (_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                ApiClient.instance.getProductosPublico().enqueue(object : retrofit2.Callback<List<Producto>> {
                    override fun onResponse(call: retrofit2.Call<List<Producto>>, response: retrofit2.Response<List<Producto>>) {
                        if (response.isSuccessful) {
                            _productos.value = response.body() ?: emptyList()
                        }
                        _isLoading.value = false
                    }
                    override fun onFailure(call: retrofit2.Call<List<Producto>>, t: Throwable) {
                        _isLoading.value = false
                    }
                })
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}
