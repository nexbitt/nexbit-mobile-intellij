package com.example.nexbitmobile.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // 10.0.2.2 es el alias del localhost de tu PC desde el emulador Android
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    private var retrofit: Retrofit? = null

    /**
     * Retorna una instancia de Retrofit.
     * Si se pasa un Context, se inyecta automáticamente el token JWT
     * en el header Authorization de cada petición.
     */
    fun getInstance(context: Context? = null): Retrofit {
        if (retrofit == null || context != null) {
            val clientBuilder = OkHttpClient.Builder()

            // Interceptor para inyectar el token JWT en las peticiones protegidas
            if (context != null) {
                clientBuilder.addInterceptor(Interceptor { chain ->
                    val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)
                    val token = prefs.getString("token", null)

                    val request = if (token != null) {
                        chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    } else {
                        chain.request()
                    }
                    chain.proceed(request)
                })
            }

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
