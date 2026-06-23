package com.example.nexbitmobile.model

data class RepartidorResponse(
    val id_usuario: Int,
    val nombre: String,
    val telefono: String?,
    val email: String,
    val activo: Boolean,
    val pedidos_repartidor: List<PedidoRepartidor>?
)

data class PedidoRepartidor(
    val id_pedido: Int,
    val usuario_id: Int,
    val total: Double,
    val estado: String,
    val direccion_entrega: String?,
    val notas_entrega: String?,
    val fecha_pedido: String?,
    val fecha_asignacion: String?,
    val cliente: ClienteSimple?,
    val detalle_pedido: List<DetallePedidoRepartidor>?,
    val seguimiento: List<SeguimientoPedido>?
)

data class ClienteSimple(
    val nombre: String
)

data class DetallePedidoRepartidor(
    val id_detalle_pedido: Int,
    val cantidad: Int,
    val subtotal: Double,
    val producto: ProductoSimple?
)

data class ProductoSimple(
    val nombre: String
)

data class SeguimientoPedido(
    val id_seguimiento: Int,
    val estado_anterior: String?,
    val estado_nuevo: String,
    val notas: String?,
    val fecha: String?,
    val usuario: ClienteSimple?
)

data class EstadoPedidoRequest(
    val estado: String,
    val notas: String?
)

data class AsignarPedidoRequest(
    val pedido_id: Int
)

data class RepartoStats(
    val disponibles: Int,
    val activo: Int,
    val entregados: Int,
    val cancelados: Int
)

data class RepartoActivoResponse(
    val tiene_activo: Boolean,
    val pedido: PedidoRepartidor?
)

data class ReporteProblemaRequest(
    val tipo: String,
    val notas: String?
)

data class HistorialRepartoResponse(
    val historial: List<PedidoRepartidor>?
)

