package com.example.nexbitmobile.model

data class Rol(
    val id_rol: Int,
    val nombre: String,
    val descripcion: String?
)

data class RolUpdateRequest(
    val nombre: String,
    val descripcion: String?
)

data class JsonResponse(
    val message: String?,
    val id_rol: Int?
)
