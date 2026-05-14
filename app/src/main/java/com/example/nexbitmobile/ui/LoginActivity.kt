package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.LoginRequest
import com.example.nexbitmobile.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (!token.isNullOrEmpty()) {
            ApiClient.instance.getMe("").enqueue(object : Callback<com.example.nexbitmobile.model.Usuario> {
                override fun onResponse(call: Call<com.example.nexbitmobile.model.Usuario>, response: Response<com.example.nexbitmobile.model.Usuario>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        prefs.edit()
                            .putInt("userId", user?.id_usuario ?: 0)
                            .putInt("rolId", user?.rol_id ?: 0)
                            .putString("userName", user?.nombre)
                            .putString("userEmail", user?.email)
                            .putString("userDocType", user?.tipo_documento)
                            .putString("userDocNum", user?.numero_documento)
                            .putString("userPhone", user?.telefono)
                            .putString("userAddress", user?.direccion)
                            .apply()
                        
                        val intent = Intent(this@LoginActivity, com.example.nexbitmobile.MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onFailure(call: Call<com.example.nexbitmobile.model.Usuario>, t: Throwable) {
                    // Ignorar error de red y permitir login normal
                }
            })
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvMessage.text = "Por favor, completa todos los campos"
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)
            
            ApiClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Toast.makeText(this@LoginActivity, "¡Bienvenido ${loginResponse?.user?.nombre}!", Toast.LENGTH_SHORT).show()
                        
                        // Guardar datos en SharedPreferences
                        val prefs = getSharedPreferences("app", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("userId", loginResponse?.user?.id_usuario ?: 0)
                            .putInt("rolId", loginResponse?.user?.rol_id ?: 0)
                            .putString("userName", loginResponse?.user?.nombre)
                            .putString("userEmail", loginResponse?.user?.email)
                            .putString("userDocType", loginResponse?.user?.tipo_documento)
                            .putString("userDocNum", loginResponse?.user?.numero_documento)
                            .putString("userPhone", loginResponse?.user?.telefono)
                            .putString("userAddress", loginResponse?.user?.direccion)
                            .putString("token", loginResponse?.token)
                            .apply()

                        // Redirigir al Inicio (Bienvenida) al iniciar sesión
                        val intent = Intent(this@LoginActivity, com.example.nexbitmobile.MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        tvMessage.text = "Error: Credenciales incorrectas"
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    tvMessage.text = "Error de conexión: ${t.message}"
                }
            })
        }

        tvToRegister?.setOnClickListener {
            // Navegar a RegisterActivity
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }
}
