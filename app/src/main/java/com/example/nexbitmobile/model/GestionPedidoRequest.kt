package com.example.nexbitmobile.model

data class GestionPedidoRequest(
    val accion: String,
    val motivo: String? = null
)
