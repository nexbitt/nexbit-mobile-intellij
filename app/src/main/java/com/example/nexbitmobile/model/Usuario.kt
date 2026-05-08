package com.example.nexbitmobile.model

data class Usuario(
    val id_usuario: Int,
    val rol_id: Int,
    val nombre: String,
    val email: String,
    val tipo_documento: String? = null,
    val numero_documento: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val activo: Boolean = true,
    val rol_nombre: String? = null
)
