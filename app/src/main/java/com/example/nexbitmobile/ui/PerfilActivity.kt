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

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Leer datos del usuario logueado
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val nombre = prefs.getString("userName", "")
        val email = prefs.getString("userEmail", "")

        val etNombre = findViewById<EditText>(R.id.etProfileName)
        val etCorreo = findViewById<EditText>(R.id.etProfileEmail)

        etNombre?.setText(nombre)
        etCorreo?.setText(email)

        // Botón de guardar simulado
        val btnGuardar = findViewById<Button>(R.id.btnSaveProfile)
        btnGuardar?.setOnClickListener {
            Toast.makeText(this, "Datos guardados (Simulación)", Toast.LENGTH_SHORT).show()
        }
    }
}
