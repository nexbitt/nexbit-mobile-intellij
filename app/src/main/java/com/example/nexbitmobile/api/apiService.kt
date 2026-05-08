package com.example.nexbitmobile.api

import retrofit2.Call
import retrofit2.http.*
import com.example.nexbitmobile.model.*

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
    fun getMe(): Call<Usuario>

    /** GET /api/usuarios — Listar todos los usuarios (protegida) */
    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    /** GET /api/usuarios/{id} — Obtener un usuario por ID (protegida) */
    @GET("usuarios/{id}")
    fun getUsuario(@Path("id") id: Int): Call<Usuario>

    /** POST /api/usuarios — Crear un nuevo usuario (público / protegida) */
    @POST("usuarios")
    fun createUsuario(@Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

    // Nota: El resto de endpoints (productos, carrito, pedidos, etc.) fueron 
    // comentados o eliminados temporalmente para centrarse en el inicio de sesión
    // y resolver los errores de los modelos eliminados.
}
