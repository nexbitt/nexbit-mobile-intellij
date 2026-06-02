package com.example.nexbitmobile.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        val toolbar = findViewById<Toolbar>(R.id.toolbarRegistro)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val spTipoDocumento = findViewById<Spinner>(R.id.spTipoDocumento)
        val etNumeroDocumento = findViewById<EditText>(R.id.etNumeroDocumento)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etDireccion = findViewById<EditText>(R.id.etDireccion)

        val options = arrayOf("CC", "TI", "CE", "NIT", "Pasaporte")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipoDocumento.adapter = adapter

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)

        tvToLogin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val tipoDoc = spTipoDocumento.selectedItem.toString()
            val numDoc = etNumeroDocumento.text.toString().trim().takeIf { it.isNotEmpty() }
            val telefono = etTelefono.text.toString().trim().takeIf { it.isNotEmpty() }
            val direccion = etDireccion.text.toString().trim().takeIf { it.isNotEmpty() }

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = UsuarioCreateRequest(
                rol_id = 2,
                nombre = nombre,
                email = email,
                password = password,
                tipo_documento = tipoDoc,
                numero_documento = numDoc,
                telefono = telefono,
                direccion = direccion
            )

            ApiClient.instance.registerUsuario(request).enqueue(object : Callback<UsuarioCreateResponse> {
                override fun onResponse(call: Call<UsuarioCreateResponse>, response: Response<UsuarioCreateResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegistroActivity, "Registro exitoso. Ahora inicia sesión.", Toast.LENGTH_LONG).show()
                        finish()
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
