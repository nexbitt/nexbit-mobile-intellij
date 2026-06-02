package com.example.nexbitmobile.api

import retrofit2.Call
import retrofit2.http.*
import com.example.nexbitmobile.model.*

interface ApiService {

    @POST("usuarios/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

<<<<<<< HEAD
=======
    /** GET /api/usuarios/me — Datos del usuario autenticado */
>>>>>>> b7f452d219807d848280414615abd82330c326b6
    @GET("usuarios/me")
    fun getMe(): Call<Usuario>

    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @GET("usuarios/{id}")
    fun getUsuario(@Path("id") id: Int): Call<Usuario>

    @POST("usuarios")
    fun createUsuario(@Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

<<<<<<< HEAD
=======
    /** POST /api/usuarios/registro — Registro público (auto-registro) */
>>>>>>> b7f452d219807d848280414615abd82330c326b6
    @POST("usuarios/registro")
    fun registerUsuario(@Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

    @PUT("usuarios/{id}")
    fun updateUsuario(@Path("id") id: Int, @Body request: UsuarioUpdateRequest): Call<Usuario>

    @DELETE("usuarios/{id}")
    fun deleteUsuario(@Path("id") id: Int): Call<Void>

    @POST("proveedores")
<<<<<<< HEAD
    fun createProveedor(@Body proveedor: Proveedor): Call<ProveedorResponse>
=======
    fun createProveedor(
        @Body proveedor: Proveedor
    ): Call<ProveedorResponse>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @GET("proveedores")
    fun getProveedores(): Call<List<Proveedor>>

    @GET("proveedores/{id}")
<<<<<<< HEAD
    fun getProveedor(@Path("id") id: Int): Call<Proveedor>
=======
    fun getProveedor(
        @Path("id") id: Int
    ): Call<Proveedor>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @PUT("proveedores/{id}")
<<<<<<< HEAD
    fun updateProveedor(@Path("id") id: Int, @Body proveedor: Proveedor): Call<Void>
=======
    fun updateProveedor(
        @Path("id") id: Int,
        @Body proveedor: Proveedor
    ): Call<Void>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @DELETE("proveedores/{id}")
<<<<<<< HEAD
    fun deleteProveedor(@Path("id") id: Int): Call<Void>
=======
    fun deleteProveedor(
        @Path("id") id: Int
    ): Call<Void>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @GET("productos/publico")
    fun getProductosPublico(): Call<List<Producto>>

<<<<<<< HEAD
    @GET("productos")
    fun getProductos(): Call<List<Producto>>

    @GET("productos/{id}")
    fun getProducto(@Path("id") id: Int): Call<Producto>

=======
    /** GET /api/productos — Todos los productos (admin) */
    @GET("productos")
    fun getProductos(): Call<List<Producto>>

    /** GET /api/productos/{id} — Detalle de producto */
    @GET("productos/{id}")
    fun getProducto(
        @Path("id") id: Int
    ): Call<Producto>

    /** POST /api/productos — Crear producto con imagen (admin) */
>>>>>>> b7f452d219807d848280414615abd82330c326b6
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

<<<<<<< HEAD
=======
    /** PUT /api/productos/{id} — Actualizar producto con imagen (admin) */
>>>>>>> b7f452d219807d848280414615abd82330c326b6
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

<<<<<<< HEAD
    @DELETE("productos/{id}")
    fun deleteProducto(@Path("id") id: Int): Call<Void>

    @GET("categorias")
    fun getCategorias(): Call<List<Categoria>>
=======
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
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @POST("categorias")
    fun createCategoria(@Body categoria: Categoria): Call<Void>

    @PUT("categorias/{id}")
    fun updateCategoria(@Path("id") id: Int, @Body categoria: Categoria): Call<Void>

    @DELETE("categorias/{id}")
    fun deleteCategoria(@Path("id") id: Int): Call<Void>

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

    @GET("carrito")
<<<<<<< HEAD
    fun getCarrito(@Query("usuario_id") usuarioId: Int): Call<List<CarritoItem>>
=======
    fun getCarrito(
        @Query("usuario_id") usuarioId: Int
    ): Call<List<CarritoItem>>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @POST("carrito/add")
<<<<<<< HEAD
    fun addToCarrito(@Body request: CarritoAddRequest): Call<List<CarritoItem>>
=======
    fun addToCarrito(
        @Body request: CarritoAddRequest
    ): Call<List<CarritoItem>>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @PUT("carrito/update/{id_carrito}")
<<<<<<< HEAD
    fun updateCarritoItem(@Path("id_carrito") idCarrito: Int, @Body request: CarritoUpdateRequest): Call<List<CarritoItem>>
=======
    fun updateCarritoItem(
        @Path("id_carrito") idCarrito: Int,
        @Body request: CarritoUpdateRequest
    ): Call<List<CarritoItem>>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @DELETE("carrito/remove/{producto_id}")
<<<<<<< HEAD
    fun removeFromCarrito(@Path("producto_id") productoId: Int, @Query("usuario_id") usuarioId: Int): Call<List<CarritoItem>>
=======
    fun removeFromCarrito(
        @Path("producto_id") productoId: Int,
        @Query("usuario_id") usuarioId: Int
    ): Call<List<CarritoItem>>
>>>>>>> b7f452d219807d848280414615abd82330c326b6

    @POST("carrito/clear")
<<<<<<< HEAD
    fun clearCarrito(@Body request: CarritoClearRequest): Call<List<CarritoItem>>

    @GET("repartidores/{id}")
    fun getRepartidor(@Path("id") id: Int): Call<RepartidorResponse>

    @PUT("repartidores/pedidos/{pedidoId}/estado")
    fun cambiarEstadoPedido(@Path("pedidoId") pedidoId: Int, @Body request: EstadoPedidoRequest): Call<Void>

    @GET("repartidores/pedidos-sin-asignar")
    fun getPedidosSinAsignar(): Call<List<PedidoRepartidor>>

    @POST("repartidores/{id}/asignar-pedido")
    fun asignarPedido(@Path("id") id: Int, @Body request: AsignarPedidoRequest): Call<Void>
=======
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
>>>>>>> b7f452d219807d848280414615abd82330c326b6
}

