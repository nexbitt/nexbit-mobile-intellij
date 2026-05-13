package com.example.nexbitmobile.model

data class Proveedor(
    val id_proveedor: Int? = null,
    val nit: String,
    val nombre: String,
    val telefono: String,
    val correo: String,
    val direccion: String
)
