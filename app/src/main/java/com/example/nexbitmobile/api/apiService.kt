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

    /** GET /api/usuarios/me — Datos del usuario autenticado */
    @GET("usuarios/me")
    fun getMe(): Call<Usuario>

    /** GET /api/usuarios — Listar todos los usuarios (protegida) */
    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    /** GET /api/usuarios/{id} — Obtener un usuario por ID (protegida) */
    @GET("usuarios/{id}")
    fun getUsuario(@Path("id") id: Int): Call<Usuario>

    /** POST /api/usuarios — Crear un nuevo usuario (protegida) */
    @POST("usuarios")
    fun createUsuario(@Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

    /** POST /api/usuarios/registro — Registro público (auto-registro) */
    @POST("usuarios/registro")
    fun registerUsuario(@Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

    /** PUT /api/usuarios/{id} — Actualizar un usuario (protegida) */
    @PUT("usuarios/{id}")
    fun updateUsuario(@Path("id") id: Int, @Body request: UsuarioUpdateRequest): Call<Usuario>

    /** DELETE /api/usuarios/{id} — Eliminar un usuario (protegida) */
    @DELETE("usuarios/{id}")
    fun deleteUsuario(@Path("id") id: Int): Call<Void>

    // ═══════════════════════════════════════════════════════════════════
    // ─── PROVEEDORES (Equivalente a Clientes del Profesor) ────────────
    // ═══════════════════════════════════════════════════════════════════

    // CREAR PROVEEDOR
    @POST("proveedores")
    fun createProveedor(
        @Body proveedor: Proveedor
    ): Call<ProveedorResponse>

    // LISTAR PROVEEDORES
    @GET("proveedores")
    fun getProveedores(): Call<List<Proveedor>>

    // OBTENER UNO
    @GET("proveedores/{id}")
    fun getProveedor(
        @Path("id") id: Int
    ): Call<Proveedor>

    // ACTUALIZAR
    @PUT("proveedores/{id}")
    fun updateProveedor(
        @Path("id") id: Int,
        @Body proveedor: Proveedor
    ): Call<Void>

    // ELIMINAR
    @DELETE("proveedores/{id}")
    fun deleteProveedor(
        @Path("id") id: Int
    ): Call<Void>

    // ═══════════════════════════════════════════════════════════════════
    // ─── CATÁLOGO DE PRODUCTOS ────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** GET /api/productos/publico — Catálogo público (sin token) */
    @GET("productos/publico")
    fun getProductosPublico(): Call<List<Producto>>

    /** GET /api/productos — Todos los productos (admin) */
    @GET("productos")
    fun getProductos(): Call<List<Producto>>

    /** GET /api/productos/{id} — Detalle de producto */
    @GET("productos/{id}")
    fun getProducto(
        @Path("id") id: Int
    ): Call<Producto>

    /** POST /api/productos — Crear producto con imagen (admin) */
    @Multipart
    @POST("productos")
    fun createProducto(
        @Part("categoria_id") categoriaId: okhttp3.RequestBody,
        @Part("proveedor_id") proveedorId: okhttp3.RequestBody,
        @Part("nombre") nombre: okhttp3.RequestBody,
        @Part("descripcion") descripcion: okhttp3.RequestBody,
        @Part("precio_compra") precioCompra: okhttp3.RequestBody,
        @Part("precio_venta") precioVenta: okhttp3.RequestBody,
        @Part("stock_actual") stockActual: okhttp3.RequestBody,
        @Part("stock_minimo") stockMinimo: okhttp3.RequestBody,
        @Part("activo") activo: okhttp3.RequestBody,
        @Part imagen: okhttp3.MultipartBody.Part?
    ): Call<Void>

    /** PUT /api/productos/{id} — Actualizar producto con imagen (admin) */
    @Multipart
    @PUT("productos/{id}")
    fun updateProducto(
        @Path("id") id: Int,
        @Part("categoria_id") categoriaId: okhttp3.RequestBody,
        @Part("proveedor_id") proveedorId: okhttp3.RequestBody,
        @Part("nombre") nombre: okhttp3.RequestBody,
        @Part("descripcion") descripcion: okhttp3.RequestBody,
        @Part("precio_compra") precioCompra: okhttp3.RequestBody,
        @Part("precio_venta") precioVenta: okhttp3.RequestBody,
        @Part("stock_actual") stockActual: okhttp3.RequestBody,
        @Part("stock_minimo") stockMinimo: okhttp3.RequestBody,
        @Part("activo") activo: okhttp3.RequestBody,
        @Part imagen: okhttp3.MultipartBody.Part?,
        @Part("imagen_url") imagenUrlActual: okhttp3.RequestBody?
    ): Call<Void>

    /** DELETE /api/productos/{id} — Eliminar producto (admin) */
    @DELETE("productos/{id}")
    fun deleteProducto(@Path("id") id: Int): Call<Void>

    // ═══════════════════════════════════════════════════════════════════
    // ─── CATEGORÍAS ───────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** GET /api/categorias — Listar categorías */
    @GET("categorias")
    fun getCategorias(): Call<List<Categoria>>

    @POST("categorias")
    fun createCategoria(@Body categoria: Categoria): Call<Void>

    @PUT("categorias/{id}")
    fun updateCategoria(@Path("id") id: Int, @Body categoria: Categoria): Call<Void>

    @DELETE("categorias/{id}")
    fun deleteCategoria(@Path("id") id: Int): Call<Void>

    // ═══════════════════════════════════════════════════════════════════
    // ─── PEDIDOS ──────────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    @GET("pedidos")
    fun getPedidos(): Call<List<Pedido>>

    @GET("pedidos/usuario/{usuarioId}")
    fun getMisPedidos(@Path("usuarioId") usuarioId: Int): Call<List<Pedido>>

    @GET("pedidos/{id}/ticket")
    fun getPedidoTicket(@Path("id") id: Int): Call<Pedido>

    @POST("pedidos/checkout")
    fun checkout(@Body request: CheckoutRequest): Call<CheckoutResponse>

    @POST("pedidos")
    fun createPedido(@Body request: PedidoRequest): Call<Void>

    @PUT("pedidos/{id}")
    fun updatePedido(@Path("id") id: Int, @Body request: PedidoRequest): Call<Void>

    @PUT("pedidos/{id}/cancelar")
    fun cancelarPedido(@Path("id") id: Int): Call<Void>

    @DELETE("pedidos/{id}")
    fun deletePedido(@Path("id") id: Int): Call<Void>

    // ═══════════════════════════════════════════════════════════════════
    // ─── CARRITO DE COMPRAS ───────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** GET /api/carrito?usuario_id={id} — Obtener carrito del usuario */
    @GET("carrito")
    fun getCarrito(
        @Query("usuario_id") usuarioId: Int
    ): Call<List<CarritoItem>>

    /** POST /api/carrito/add — Agregar item al carrito */
    @POST("carrito/add")
    fun addToCarrito(
        @Body request: CarritoAddRequest
    ): Call<List<CarritoItem>>

    /** PUT /api/carrito/update/{id_carrito} — Actualizar cantidad */
    @PUT("carrito/update/{id_carrito}")
    fun updateCarritoItem(
        @Path("id_carrito") idCarrito: Int,
        @Body request: CarritoUpdateRequest
    ): Call<List<CarritoItem>>

    /** DELETE /api/carrito/remove/{producto_id}?usuario_id={id} — Eliminar item */
    @DELETE("carrito/remove/{producto_id}")
    fun removeFromCarrito(
        @Path("producto_id") productoId: Int,
        @Query("usuario_id") usuarioId: Int
    ): Call<List<CarritoItem>>

    /** POST /api/carrito/clear — Vaciar carrito */
    @POST("carrito/clear")
    fun clearCarrito(
        @Body request: CarritoClearRequest
    ): Call<List<CarritoItem>>

    // ═══════════════════════════════════════════════════════════════════
    // ─── REPARTIDORES ─────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    /** GET /api/repartidores/{id} — Obtener datos del repartidor y sus entregas */
    @GET("repartidores/{id}")
    fun getRepartidor(
        @Path("id") id: Int
    ): Call<RepartidorResponse>

    /** PUT /api/repartidores/pedidos/{pedidoId}/estado — Cambiar estado de un pedido */
    @PUT("repartidores/pedidos/{pedidoId}/estado")
    fun cambiarEstadoPedido(
        @Path("pedidoId") pedidoId: Int,
        @Body request: EstadoPedidoRequest
    ): Call<Void>

    /** GET /api/repartidores/pedidos-sin-asignar — Obtener pedidos listos para asignación */
    @GET("repartidores/pedidos-sin-asignar")
    fun getPedidosSinAsignar(): Call<List<PedidoRepartidor>>

    /** POST /api/repartidores/{id}/asignar-pedido — Auto-asignar pedido a repartidor */
    @POST("repartidores/{id}/asignar-pedido")
    fun asignarPedido(
        @Path("id") id: Int,
        @Body request: AsignarPedidoRequest
    ): Call<Void>
}

