package com.example.nexbitmobile.api

import android.content.Context
import android.content.Intent
import com.example.nexbitmobile.NexbitApplication
import com.example.nexbitmobile.ui.LoginActivity
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = NexbitApplication.appContext.getSharedPreferences("app", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        if (response.code == 401) {
            // Token is invalid or expired
            prefs.edit().clear().apply()
            
            // Redirect to LoginActivity
            val intent = Intent(NexbitApplication.appContext, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            NexbitApplication.appContext.startActivity(intent)
        }

        return response
    }
}
