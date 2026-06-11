package com.example.nexbitmobile.model

data class Categoria(
    val id_categoria: Int,
    val nombre: String,
    val descripcion: String?
)

data class CategoriaCreateResponse(
    val message: String?,
    val id_categoria: Int?
)
