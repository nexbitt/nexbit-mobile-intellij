package com.example.nexbitmobile.model

data class Banco(
    val id_configuracion: Int,
    val banco: String,
    val tipo_cuenta: String,
    val numero_cuenta: String,
    val titular: String,
    val documento: String?,
    val descripcion: String?,
    val activo: Boolean? = null
)
