package com.example.nexbitmobile.api

import retrofit2.Call
import retrofit2.http.*
import com.example.nexbitmobile.model.*

interface ApiService {

    @POST("usuarios/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("usuarios/me")
    fun getMe(): Call<Usuario>

    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @GET("usuarios/{id}")
    fun getUsuario(@Path("id") id: Int): Call<Usuario>

    @POST("usuarios")
    fun createUsuario(@Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

    @POST("usuarios/registro")
    fun registerUsuario(@Body request: UsuarioCreateRequest): Call<UsuarioCreateResponse>

    @PUT("usuarios/{id}")
    fun updateUsuario(@Path("id") id: Int, @Body request: UsuarioUpdateRequest): Call<Usuario>

    @PUT("admin/usuarios/{id}")
    fun updateUsuarioByAdmin(@Path("id") id: Int, @Body request: UsuarioUpdateRequest): Call<Void>

    @DELETE("usuarios/{id}")
    fun deleteUsuario(@Path("id") id: Int): Call<Void>

    @POST("proveedores")
    fun createProveedor(@Body proveedor: Proveedor): Call<ProveedorResponse>

    @GET("proveedores")
    fun getProveedores(): Call<List<Proveedor>>

    @GET("proveedores/{id}")
    fun getProveedor(@Path("id") id: Int): Call<Proveedor>

    @PUT("proveedores/{id}")
    fun updateProveedor(@Path("id") id: Int, @Body proveedor: Proveedor): Call<Void>

    @DELETE("proveedores/{id}")
    fun deleteProveedor(@Path("id") id: Int): Call<Void>

    @GET("productos/publico")
    fun getProductosPublico(): Call<List<Producto>>

    @GET("productos")
    fun getProductos(): Call<List<Producto>>

    @GET("productos/{id}")
    fun getProducto(@Path("id") id: Int): Call<Producto>
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

    @DELETE("productos/{id}")
    fun deleteProducto(@Path("id") id: Int): Call<Void>

    @GET("categorias")
    fun getCategorias(): Call<List<Categoria>>

    @POST("categorias")
    fun createCategoria(@Body categoria: CategoriaRequest): Call<CategoriaCreateResponse>

    @PUT("categorias/{id}")
    fun updateCategoria(@Path("id") id: Int, @Body categoria: CategoriaRequest): Call<Void>

    @DELETE("categorias/{id}")
    fun deleteCategoria(@Path("id") id: Int): Call<Void>

    @GET("pedidos")
    fun getPedidos(): Call<List<Pedido>>

    @GET("pedidos/usuario/{usuarioId}")
    fun getMisPedidos(@Path("usuarioId") usuarioId: Int): Call<List<Pedido>>

    @GET("pedidos/{id}")
    fun getPedido(@Path("id") id: Int): Call<Pedido>

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
    fun getCarrito(@Query("usuario_id") usuarioId: Int): Call<List<CarritoItem>>

    @POST("carrito/add")
    fun addToCarrito(@Body request: CarritoAddRequest): Call<List<CarritoItem>>

    @PUT("carrito/update/{id_carrito}")
    fun updateCarritoItem(@Path("id_carrito") idCarrito: Int, @Body request: CarritoUpdateRequest): Call<List<CarritoItem>>

    @DELETE("carrito/remove/{producto_id}")
    fun removeFromCarrito(@Path("producto_id") productoId: Int, @Query("usuario_id") usuarioId: Int): Call<List<CarritoItem>>

    @POST("carrito/clear")
    fun clearCarrito(@Body request: CarritoClearRequest): Call<List<CarritoItem>>

    @POST("auth/recover-password")
    fun recoverPassword(@Body request: RecoverPasswordRequest): Call<AuthResponse>

    @POST("auth/verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<AuthResponse>

    @POST("auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<AuthResponse>

    @GET("repartidores/{id}")
    fun getRepartidor(@Path("id") id: Int): Call<RepartidorResponse>

    @PUT("repartidores/pedidos/{pedidoId}/estado")
    fun cambiarEstadoPedido(@Path("pedidoId") pedidoId: Int, @Body request: EstadoPedidoRequest): Call<Void>

    @GET("repartidores/pedidos-sin-asignar")
    fun getPedidosSinAsignar(): Call<List<PedidoRepartidor>>

    @POST("repartidores/{id}/asignar-pedido")
    fun asignarPedido(@Path("id") id: Int, @Body request: AsignarPedidoRequest): Call<Void>

    @GET("roles")
    fun getRoles(): Call<List<Rol>>

    @POST("roles")
    fun createRol(@Body request: RolUpdateRequest): Call<JsonResponse>

    @PUT("roles/{id}")
    fun updateRol(@Path("id") id: Int, @Body request: RolUpdateRequest): Call<Void>

    @GET("reparto/stats")
    fun getRepartoStats(): Call<RepartoStats>

    @GET("reparto/activo")
    fun getRepartoActivo(): Call<RepartoActivoResponse>

    @GET("reparto/disponibles")
    fun getRepartoDisponibles(): Call<List<PedidoRepartidor>>

    @GET("reparto/historial")
    fun getRepartoHistorial(@Query("filtro") filtro: String? = null): Call<List<PedidoRepartidor>>

    @POST("reparto/{id}/tomar")
    fun tomarPedido(@Path("id") id: Int): Call<Void>

    @PUT("reparto/{id}/en-camino")
    fun marcarEnCamino(@Path("id") id: Int): Call<Void>

    @POST("reparto/{id}/entregar")
    fun confirmarEntrega(@Path("id") id: Int): Call<Void>

    @PUT("reparto/{id}/cancelar")
    fun cancelarEntrega(@Path("id") id: Int): Call<Void>

    @POST("reparto/{id}/problema")
    fun reportarProblema(@Path("id") id: Int, @Body request: ReporteProblemaRequest): Call<Void>

    @Multipart
    @POST("pedidos/{id}/subir-comprobante")
    fun subirComprobante(
        @Path("id") pedidoId: Int,
        @Part comprobante: okhttp3.MultipartBody.Part
    ): Call<Void>

    @PUT("pedidos/{id}/aprobar-pago")
    fun aprobarPago(@Path("id") pedidoId: Int): Call<Void>

    @PUT("pedidos/{id}/rechazar-pago")
    fun rechazarPago(@Path("id") pedidoId: Int, @Body request: RechazarPagoRequest): Call<Void>

    @PATCH("admin/pedidos/{id}/gestion")
    fun gestionarPedido(@Path("id") pedidoId: Int, @Body request: GestionPedidoRequest): Call<Void>

    @GET("pedidos/en-revision")
    fun getPedidosEnRevision(): Call<List<Pedido>>

    @GET("pedidos/usuario/trash")
    fun getPedidosEliminados(): Call<List<Pedido>>

    @PUT("pedidos/{id}/eliminar")
    fun eliminarPedido(@Path("id") id: Int): Call<Void>

    @PUT("pedidos/{id}/restaurar")
    fun restaurarPedido(@Path("id") id: Int): Call<Void>

    @GET("bancos")
    fun getBancos(): Call<List<Banco>>

    // ─── FASE 2: CHAT ────────────────────────────────────────────────

    @GET("chat/conversacion/pedido/{pedido_id}")
    fun getConversacion(@Path("pedido_id") pedidoId: Int): Call<Conversacion>

    @POST("chat/conversacion/{conversacion_id}/mensajes")
    fun enviarMensaje(@Path("conversacion_id") conversacionId: Int, @Body request: MensajeRequest): Call<Mensaje>

    @PUT("chat/conversacion/{conversacion_id}/leidos")
    fun marcarLeidos(@Path("conversacion_id") conversacionId: Int): Call<Void>

    @GET("chat/mensajes/no-leidos")
    fun getMensajesNoLeidos(): Call<MensajeNoLeidosResponse>

    @GET("chat/conversaciones/admin")
    fun getConversacionesAdmin(): Call<List<Conversacion>>

    // ─── FASE 2: STATS ───────────────────────────────────────────────

    @GET("stats")
    fun getStats(): Call<StatsResponse>

    // ─── Reportes / Analítica ──────────────────────────────────────────────────

    @GET("reportes/ventas/kpis")
    fun getReporteVentasKpis(): Call<VentaKpi>

    @GET("reportes/ventas")
    fun getReporteVentas(): Call<List<VentaRow>>

    @GET("reportes/inventario/kpis")
    fun getReporteInventarioKpis(): Call<InventarioKpi>

    @GET("reportes/inventario")
    fun getReporteInventario(): Call<List<InventarioRow>>

    @GET("reportes/seguridad/kpis")
    fun getReporteSeguridadKpis(): Call<SeguridadKpi>

    @GET("reportes/seguridad")
    fun getReporteSeguridad(): Call<List<SeguridadRow>>

    @GET("reportes/carritos/kpis")
    fun getReporteCarritosKpis(): Call<CarritosKpi>

    @GET("reportes/carritos")
    fun getReporteCarritos(): Call<List<CarritoRow>>

    @GET("reportes/repartidores/kpis")
    fun getReporteRepartidoresKpis(): Call<LogisticaKpi>

    @GET("reportes/repartidores")
    fun getReporteRepartidores(): Call<List<LogisticaRow>>
}

