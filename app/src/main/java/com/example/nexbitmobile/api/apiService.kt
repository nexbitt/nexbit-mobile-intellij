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

    // ═══════════════════════════════════════════════════════════════════
    // ─── CATÁLOGO DE PRODUCTOS ────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** GET /api/productos/publico — Catálogo público (sin token) */
    @GET("productos/publico")
    fun getProductosPublico(): Call<List<Producto>>

    /** GET /api/productos — Todos los productos (admin, con token) */
    @GET("productos")
    fun getProductos(
        @Header("Authorization") token: String
    ): Call<List<Producto>>

    /** GET /api/productos/{id} — Detalle de producto (con token) */
    @GET("productos/{id}")
    fun getProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Producto>

    // ═══════════════════════════════════════════════════════════════════
    // ─── CATEGORÍAS ───────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** GET /api/categorias — Listar categorías (con token) */
    @GET("categorias")
    fun getCategorias(
        @Header("Authorization") token: String
    ): Call<List<Categoria>>

    // ═══════════════════════════════════════════════════════════════════
    // ─── CARRITO DE COMPRAS ───────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** GET /api/carrito?usuario_id={id} — Obtener carrito del usuario */
    @GET("carrito")
    fun getCarrito(
        @Header("Authorization") token: String,
        @Query("usuario_id") usuarioId: Int
    ): Call<List<CarritoItem>>

    /** POST /api/carrito/add — Agregar item al carrito */
    @POST("carrito/add")
    fun addToCarrito(
        @Header("Authorization") token: String,
        @Body request: CarritoAddRequest
    ): Call<List<CarritoItem>>

    /** PUT /api/carrito/update/{id_carrito} — Actualizar cantidad */
    @PUT("carrito/update/{id_carrito}")
    fun updateCarritoItem(
        @Header("Authorization") token: String,
        @Path("id_carrito") idCarrito: Int,
        @Body request: CarritoUpdateRequest
    ): Call<List<CarritoItem>>

    /** DELETE /api/carrito/remove/{producto_id}?usuario_id={id} — Eliminar item */
    @DELETE("carrito/remove/{producto_id}")
    fun removeFromCarrito(
        @Header("Authorization") token: String,
        @Path("producto_id") productoId: Int,
        @Query("usuario_id") usuarioId: Int
    ): Call<List<CarritoItem>>

    /** POST /api/carrito/clear — Vaciar carrito */
    @POST("carrito/clear")
    fun clearCarrito(
        @Header("Authorization") token: String,
        @Body request: CarritoClearRequest
    ): Call<List<CarritoItem>>
}
