package com.example.nexbitmobile.loginActivity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.ui.ClientesActivity
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.api.ApiService
import com.example.nexbitmobile.model.LoginRequest
import com.example.nexbitmobile.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val message = findViewById<TextView>(R.id.message)

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                // Usa instance para login
                val api = ApiClient.instance
                val request = LoginRequest(email, password)

                api.login(request).enqueue(object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {

                            val body = response.body()
                            val token = body?.token
                            val userName = body?.user?.nombre ?: ""

                            // ✅ Guardar token y datos del usuario
                            val prefs = getSharedPreferences("app", MODE_PRIVATE)
                            prefs.edit()
                                .putString("token", token)
                                .putString("userName", userName)
                                .putInt("userId", body?.user?.id_usuario ?: 0)
                                .putInt("rolId", body?.user?.rol_id ?: 0)
                                .apply()

                            message.setTextColor(Color.GREEN)
                            message.text = "¡Bienvenido, $userName!"

                            val intent = Intent(this@LoginActivity2, ClientesActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            message.setTextColor(Color.RED)
                            message.text = when (response.code()) {
                                401 -> "Correo o contraseña incorrectos"
                                403 -> "La cuenta está inactiva"
                                else -> "Error al iniciar sesión"
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        message.setTextColor(Color.RED)
                        message.text = "Error de conexión: ${t.message}"
                        t.printStackTrace()
                    }
                })

            } else {
                message.setTextColor(Color.RED)
                message.text = "Campos vacíos"
            }
        }
    }
}
