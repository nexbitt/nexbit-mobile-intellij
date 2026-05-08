package com.example.nexbitmobile.model

/**
 * Request para crear un nuevo usuario.
 */
data class UsuarioCreateRequest(
    val rol_id: Int,
    val nombre: String,
    val email: String,
    val password: String,
    val tipo_documento: String? = null,
    val numero_documento: String? = null,
    val telefono: String? = null,
    val direccion: String? = null
)

/**
 * Request para actualizar un usuario existente.
 */
data class UsuarioUpdateRequest(
    val rol_id: Int? = null,
    val nombre: String? = null,
    val email: String? = null,
    val password: String? = null,
    val tipo_documento: String? = null,
    val numero_documento: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val activo: Boolean? = null
)

/**
 * Respuesta al crear un usuario.
 */
data class UsuarioCreateResponse(
    val message: String,
    val id_usuario: Int
)
