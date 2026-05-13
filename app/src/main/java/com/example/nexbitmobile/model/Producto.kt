package com.example.nexbitmobile.model

data class Producto(
    val id_producto: Int,
    val categoria_id: Int?,
    val proveedor_id: Int?,
    val nombre: String,
    val descripcion: String?,
    val imagen_url: String?,
    val precio_compra: Double,
    val precio_venta: Double,
    val stock_actual: Int,
    val stock_minimo: Int,
    val activo: Int,
    val categoria_nombre: String?,
    val proveedor_nombre: String?
)
