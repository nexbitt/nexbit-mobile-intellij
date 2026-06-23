package com.example.nexbitmobile.model

data class ConversacionResponse(
    val id_conversacion: Int,
    val pedido_id: Int,
    val mensajes: List<Mensaje>?
)

data class Mensaje(
    val id_mensaje: Int,
    val conversacion_id: Int,
    val usuario_id: Int,
    val usuario_nombre: String?,
    val mensaje: String,
    val fecha: String?,
    val leido: Boolean
)

data class EnviarMensajeRequest(
    val mensaje: String
)

data class MensajesNoLeidosResponse(
    val total: Int
)
