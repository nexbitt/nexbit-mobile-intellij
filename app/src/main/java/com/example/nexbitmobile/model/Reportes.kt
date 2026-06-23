package com.example.nexbitmobile.model

data class StatsResponse(
    val productos: Int,
    val pedidos: Int,
    val clientes: Int,
    val categorias: Int
)

data class VentaKpi(
    val total_tickets: Int,
    val ingresos_totales: Double,
    val ticket_promedio: Double,
    val top_productos: List<TopProducto>?
)

data class TopProducto(
    val nombre: String,
    val total_vendido: Int,
    val ingresos: Double
)

data class InventarioKpi(
    val total_productos: Int,
    val agotados: Int,
    val stock_bajo: Int,
    val valor_total_inventario: Double,
    val ganancia_potencial: Double,
    val top_margen: List<ProductoMargen>?
)

data class ProductoMargen(
    val nombre: String,
    val margen: Double
)

data class SeguridadKpi(
    val total_usuarios: Int,
    val activos: Int,
    val inactivos: Int,
    val por_rol: List<RolCount>?
)

data class RolCount(
    val rol: String,
    val cantidad: Int
)

data class CarritoKpi(
    val total_items: Int,
    val valor_potencial: Double,
    val problemas_stock: Int
)

data class RepartidorKpi(
    val total_repartidores: Int,
    val entregas_a_tiempo: Int,
    val entregas_tarde: Int,
    val por_repartidor: List<RepartidorStats>?
)

data class RepartidorStats(
    val nombre: String,
    val entregas: Int,
    val a_tiempo: Int
)
