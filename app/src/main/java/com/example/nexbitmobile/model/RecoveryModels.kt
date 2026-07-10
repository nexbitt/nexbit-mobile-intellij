package com.example.nexbitmobile.model

data class RecoverPasswordRequest(val email: String)
data class VerifyOtpRequest(val email: String, val otp: String)
data class ResetPasswordRequest(val email: String, val token: String, val password: String)

data class AuthResponse(
    val success: Boolean,
    val data: AuthData?,
    val error: String?,
    val message: String?
)

data class AuthData(val token: String?)
