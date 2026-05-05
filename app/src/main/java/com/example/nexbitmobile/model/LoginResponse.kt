package com.example.nexbitmobile.model

data class LoginResponse(
    val message: String,
    val token: String,
    val user: Usuario
)
