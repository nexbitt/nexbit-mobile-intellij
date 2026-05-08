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
import com.example.nexbitmobile.model.UsuarioCreateRequest
import com.example.nexbitmobile.model.UsuarioCreateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etTipoDocumento = findViewById<EditText>(R.id.etTipoDocumento)
        val etNumeroDocumento = findViewById<EditText>(R.id.etNumeroDocumento)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)

        tvToLogin.setOnClickListener {
            // Retroceder al login
            finish()
        }

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            val tipoDoc = etTipoDocumento.text.toString().trim().takeIf { it.isNotEmpty() }
            val numDoc = etNumeroDocumento.text.toString().trim().takeIf { it.isNotEmpty() }
            val telefono = etTelefono.text.toString().trim().takeIf { it.isNotEmpty() }
            val direccion = etDireccion.text.toString().trim().takeIf { it.isNotEmpty() }

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = UsuarioCreateRequest(
                rol_id = 2, // Cliente
                nombre = nombre,
                email = email,
                password = password,
                tipo_documento = tipoDoc,
                numero_documento = numDoc,
                telefono = telefono,
                direccion = direccion
            )

            ApiClient.instance.createUsuario(request).enqueue(object : Callback<UsuarioCreateResponse> {
                override fun onResponse(call: Call<UsuarioCreateResponse>, response: Response<UsuarioCreateResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegistroActivity, "Registro exitoso. Ahora inicia sesión.", Toast.LENGTH_LONG).show()
                        finish() // Vuelve a la pantalla de Login
                    } else {
                        Toast.makeText(this@RegistroActivity, "Error al registrar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UsuarioCreateResponse>, t: Throwable) {
                    Toast.makeText(this@RegistroActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
