package com.example.nexbitmobile.model

data class Producto(
    val id_producto: Int,
    val categoria_id: Int?,
    val nombre: String,
    val descripcion: String?,
    val precio_compra: Double,
    val precio_venta: Double,
    val stock: Int,
    val imagen_url: String?,
    val activo: Boolean
)
