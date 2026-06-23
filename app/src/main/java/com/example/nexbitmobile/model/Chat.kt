package com.example.nexbitmobile.model

data class Conversacion(
    val id_conversacion: Int,
    val pedido_id: Int,
    val usuario_id: Int,
    val admin_id: Int?,
    val created_at: String?,
    val updated_at: String?,
    val usuario: UsuarioSimple?,
    val admin: UsuarioSimple?,
    val mensajes: List<Mensaje>? = null,
    val mensajes_no_leidos: Int? = null,
    val ultimo_mensaje: String? = null,
    val no_leidos: Int? = null
)

data class UsuarioSimple(
    val id_usuario: Int,
    val nombre: String
)

data class RemitenteSimple(
    val id_usuario: Int,
    val nombre: String,
    val rol_id: Int?
)

data class Mensaje(
    val id_mensaje: Int,
    val conversacion_id: Int,
    val remitente_id: Int,
    val mensaje: String,
    val leido: Boolean,
    val created_at: String?,
    val remitente: RemitenteSimple?
)

data class MensajeRequest(
    val mensaje: String
)

data class MensajeNoLeidosResponse(
    val noLeidos: Int
)
