package com.example.nexbitmobile.model

// ─── Ventas ───────────────────────────────────────────────────────────────────

data class VentaKpi(
    val total_tickets: Int,
    val total_ingresos: Double,
    val promedio_ticket: Double,
    val top_productos: List<TopProducto>
)

data class TopProducto(val producto: String, val total_uds: Int)

data class VentaRow(
    val Factura_No: String?,
    val Fecha_Venta: String?,
    val Cliente: String?,
    val Documento: String?,
    val Categoria: String?,
    val Producto: String?,
    val Cant: Int?,
    val Precio_Venta_COP: Double?,
    val Subtotal_Item: Double?,
    val Subtotal_Pedido: Double?,
    val IVA: Double?,
    val Total_Factura: Double?,
    val Estado_Pago: String?,
    val Repartidor: String?
)

// ─── Inventario ────────────────────────────────────────────────────────────────

data class InventarioKpi(
    val total_productos: Int,
    val agotados: Int,
    val stock_bajo: Int,
    val ok: Int,
    val valor_total_costo: Double,
    val ganancia_potencial: Double,
    val top_margen: List<TopMargen>
)

data class TopMargen(val producto: String, val margen_pct: Double)

data class InventarioRow(
    val ID: Int?,
    val Producto: String?,
    val Categoria: String?,
    val Proveedor: String?,
    val Stock_Disponible: Int?,
    val Stock_Min: Int?,
    val Alerta_Stock: String?,
    val Costo_Unit_COP: Double?,
    val PVP_COP: Double?,
    val Margen_Ganancia: Double?,
    val Margen_Pct: Double?,
    val Valor_Inventario_Costo: Double?,
    val Ganancia_Potencial: Double?
)

// ─── Seguridad ─────────────────────────────────────────────────────────────────

data class SeguridadKpi(
    val total_usuarios: Int,
    val activos: Int,
    val inactivos: Int,
    val por_rol: List<RolCount>
)

data class RolCount(val rol: String?, val cantidad: Int)

data class SeguridadRow(
    val ID_User: Int?,
    val Nombre_Usuario: String?,
    val Email_Login: String?,
    val Telefono: String?,
    val Rol: String?,
    val Permisos_Asignados: String?,
    val Estado_Cuenta: String?,
    val Fecha_Registro: String?,
    val Ultima_Modificacion: String?
)

// ─── Carritos Activos ──────────────────────────────────────────────────────────

data class CarritosKpi(
    val total_items: Int,
    val valor_potencial: Double,
    val con_problema_stock: Int
)

data class CarritoRow(
    val Usuario: String?,
    val Session_ID: String?,
    val Producto: String?,
    val Cant_En_Carrito: Int?,
    val Precio_Actual: Double?,
    val Total_Proyectado: Double?,
    val Disponibilidad: String?
)

// ─── Logística (Repartidores) ──────────────────────────────────────────────────

data class LogisticaKpi(
    val total_pedidos: Int,
    val a_tiempo: Int,
    val con_retraso: Int,
    val por_repartidor: List<RepartidorCount>
)

data class RepartidorCount(val repartidor: String?, val cantidad: Int)

data class LogisticaRow(
    val ID_Repartidor: Int?,
    val Repartidor: String?,
    val Telefono: String?,
    val ID_Pedido: Any?,
    val Cliente: String?,
    val Direccion_Entrega: String?,
    val Estado_Pedido: String?,
    val Fecha_Asignacion: String?,
    val Entrega_Estimada: String?,
    val Entrega_Real: String?,
    val Cumplimiento: String?,
    val Total_Pedido: Double?,
    val Productos: String?
)
