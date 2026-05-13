package com.example.nexbitmobile.model

data class CarritoItem(
    val id_carrito: Int,
    val usuario_id: Int?,
    val session_id: String?,
    val producto_id: Int,
    val cantidad: Int,
    val nombre: String,
    val precio: Double,
    val subtotal: Double,
    val stock_actual: Int,
    val imagen_url: String?
)

data class CarritoAddRequest(
    val usuario_id: Int?,
    val session_id: String?,
    val producto_id: Int,
    val cantidad: Int
)

data class CarritoUpdateRequest(
    val cantidad: Int,
    val usuario_id: Int?,
    val session_id: String?
)

data class CarritoClearRequest(
    val usuario_id: Int?,
    val session_id: String?
)
