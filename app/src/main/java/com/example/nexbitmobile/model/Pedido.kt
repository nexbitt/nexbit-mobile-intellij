package com.example.nexbitmobile.model

data class Pedido(
    val id_pedido: Int,
    val usuario_id: Int,
    val usuario_nombre: String?,
    val numero_documento: String?,
    val total: Double,
    val estado: String,
    val fecha: String? = null,
    val fecha_pedido: String?,
    val direccion_entrega: String? = null,
    val notas_entrega: String? = null,
    val comprobante_pago_url: String? = null,
    val detalles: List<PedidoDetalle>? = null,
    val repartidor_id: Int? = null
)

data class PedidoDetalle(
    val id_detalle_pedido: Int,
    val producto_id: Int,
    val producto_nombre: String?,
    val imagen_url: String?,
    val cantidad: Int,
    val precio_unitario: Double,
    val subtotal: Double
)

data class PedidoRequest(
    val usuario_id: Int,
    val total: Double,
    val estado: String,
    val direccion_entrega: String? = null,
    val notas_entrega: String? = null,
    val repartidor_id: Int? = null
)

data class CheckoutRequest(
    val usuario_id: Int,
    val direccion_entrega: String,
    val notas_entrega: String? = null
)

data class CheckoutResponse(
    val message: String,
    val id_pedido: Int
)

data class PedidoEditRequest(
    val estado: String? = null,
    val direccion_entrega: String? = null,
    val notas_entrega: String? = null,
    val repartidor_id: Int? = null,
    val total: Double? = null
)


