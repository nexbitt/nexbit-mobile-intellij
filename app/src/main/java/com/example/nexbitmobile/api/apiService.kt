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

    @PUT("roles/{id}")
    fun updateRol(@Path("id") id: Int, @Body request: RolUpdateRequest): Call<Void>
}

