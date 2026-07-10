package com.example.nexbitmobile.api

import android.content.Context
import android.content.Intent
import com.example.nexbitmobile.NexbitApplication
import com.example.nexbitmobile.ui.LoginActivity
import com.example.nexbitmobile.util.SecurePrefs
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val context = NexbitApplication.appContext
        val token = SecurePrefs.getToken(context)

        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        if (response.code == 401) {
            val bodyString = response.peekBody(2048).string()
            // Solo se destruye la sesi√≥n si el backend ratifica token vencido/corrupto
            // Check if the response body contains backend auth error messages
            if (bodyString.contains("Token expirado") || bodyString.contains("Token inv·lido") || bodyString.contains("Acceso denegado")) {
                SecurePrefs.clearAll(context)

                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }
        }

        return response
    }
}
