package com.example.nexbitmobile.model

data class Conversacion(
    val id_conversacion: Int,
    val pedido_id: Int,
    val usuario_id: Int,
    val admin_id: Int?,
    val created_at: String?,
    val updated_at: String?,
    val usuario_nombre: String?,
    val ultimo_mensaje: String?,
    val no_leidos: Int?,
    val mensajes: List<Mensaje>? = null
)

data class Mensaje(
    val id_mensaje: Int,
    val conversacion_id: Int,
    val remitente_id: Int,
    val mensaje: String,
    val leido: Boolean,
    val created_at: String,
    val remitente_nombre: String?
)

data class MensajeRequest(
    val mensaje: String
)

data class MensajeNoLeidosResponse(
    val total: Int
)
