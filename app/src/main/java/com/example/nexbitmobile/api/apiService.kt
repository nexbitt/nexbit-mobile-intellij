package com.example.nexbitmobile.api

import retrofit2.Call
import retrofit2.http.*
import com.example.nexbitmobile.model.LoginRequest
import com.example.nexbitmobile.model.LoginResponse
import com.example.nexbitmobile.model.Producto
import com.example.nexbitmobile.model.Usuario

interface ApiService {

    // ─── Autenticación ───────────────────────────────────────────────
    @POST("usuarios/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("usuarios/logout")
    fun logout(): Call<Map<String, String>>

    // ─── Usuarios ────────────────────────────────────────────────────
    @GET("usuarios/me")
    fun getMe(): Call<Usuario>

    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    // ─── Productos ───────────────────────────────────────────────────
    @GET("productos/publico")
    fun getProductosPublicos(): Call<List<Producto>>

    @GET("productos")
    fun getProductos(): Call<List<Producto>>

    @GET("productos/{id}")
    fun getProducto(@Path("id") id: Int): Call<Producto>
}
