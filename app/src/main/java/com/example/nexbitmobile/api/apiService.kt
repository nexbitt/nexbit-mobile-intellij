package com.example.nexbitmobile.api

import retrofit2.Call
import retrofit2.http.*
import com.example.nexbitmobile.model.*
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.nexbitmobile.model.LoginResponse
import com.example.nexbitmobile.model.LoginRequest
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.DELETE


/**
 * Interfaz Retrofit que mapea los endpoints del backend Node.js.
 *
 * Base URL:  http://10.0.2.2:3000/api/
 * Autenticación: JWT via header "Authorization: Bearer <token>"
 */
interface ApiService {

    // ═══════════════════════════════════════════════════════════════════
    // ─── AUTENTICACIÓN Y USUARIOS ─────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** POST /api/usuarios/login — Login público */
    @POST("usuarios/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    /** GET /api/usuarios/me — Datos del usuario autenticado (requiere token) */
    @GET("usuarios/me")
    fun getMe(@Header("Authorization") token: String): Call<Usuario>

    /** GET /api/usuarios — Listar todos los usuarios (protegida) */
    @GET("usuarios")
    fun getUsuarios(@Header("Authorization") token: String): Call<List<Usuario>>

    /** GET /api/usuarios/{id} — Obtener un usuario por ID (protegida) */
    @GET("usuarios/{id}")
    fun getUsuario(@Header("Authorization") token: String, @Path("id") id: Int): Call<Usuario>

    /** POST /api/usuarios — Crear un nuevo usuario (protegida) */
    @POST("usuarios")
    fun createUsuario(@Header("Authorization") token: String, @Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

    /** PUT /api/usuarios/{id} — Actualizar un usuario (protegida) */
    @PUT("usuarios/{id}")
    fun updateUsuario(@Header("Authorization") token: String, @Path("id") id: Int, @Body request: UsuarioUpdateRequest): Call<Usuario>

    /** DELETE /api/usuarios/{id} — Eliminar un usuario (protegida) */
    @DELETE("usuarios/{id}")
    fun deleteUsuario(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    // ═══════════════════════════════════════════════════════════════════
    // ─── PROVEEDORES (Equivalente a Clientes del Profesor) ────────────
    // ═══════════════════════════════════════════════════════════════════

    // CREAR PROVEEDOR
    @POST("proveedores")
    fun createProveedor(
        @Header("Authorization") token: String,
        @Body proveedor: Proveedor
    ): Call<ProveedorResponse>

    // LISTAR PROVEEDORES
    @GET("proveedores")
    fun getProveedores(
        @Header("Authorization") token: String
    ): Call<List<Proveedor>>

    // OBTENER UNO
    @GET("proveedores/{id}")
    fun getProveedor(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Proveedor>

    // ACTUALIZAR
    @PUT("proveedores/{id}")
    fun updateProveedor(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body proveedor: Proveedor
    ): Call<Void>

    // ELIMINAR
    @DELETE("proveedores/{id}")
    fun deleteProveedor(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>
}
