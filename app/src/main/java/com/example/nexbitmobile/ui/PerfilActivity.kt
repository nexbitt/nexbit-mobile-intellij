package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilActivity : AppCompatActivity() {

    private var currentUserId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val toolbar = findViewById<Toolbar>(R.id.toolbarPerfil)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        currentUserId = prefs.getInt("userId", 0)

        // Referencias a la UI
        val tvHeaderName = findViewById<TextView>(R.id.tvHeaderName)
        val tvHeaderEmail = findViewById<TextView>(R.id.tvHeaderEmail)
        val tvAvatarInitial = findViewById<TextView>(R.id.tvAvatarInitial)
        val tvLoadingStatus = findViewById<TextView>(R.id.tvLoadingStatus)

        val etNombre = findViewById<EditText>(R.id.etProfileName)
        val etCorreo = findViewById<EditText>(R.id.etProfileEmail)
        val etDocType = findViewById<EditText>(R.id.etProfileDocType)
        val etDocNum = findViewById<EditText>(R.id.etProfileDocNum)
        val etTelefono = findViewById<EditText>(R.id.etProfilePhone)
        val etDireccion = findViewById<EditText>(R.id.etProfileAddress)

        // Cargar datos iniciales de SharedPreferences (mientras se cargan los de la DB)
        etNombre.setText(prefs.getString("userName", ""))
        etCorreo.setText(prefs.getString("userEmail", ""))
        etDocType.setText(prefs.getString("userDocType", ""))
        etDocNum.setText(prefs.getString("userDocNum", ""))
        etTelefono.setText(prefs.getString("userPhone", ""))
        etDireccion.setText(prefs.getString("userAddress", ""))

        // Actualizar header con datos temporales
        val tempName = prefs.getString("userName", "Usuario") ?: "Usuario"
        tvHeaderName.text = tempName
        tvHeaderEmail.text = prefs.getString("userEmail", "")
        if (tempName.isNotEmpty()) {
            tvAvatarInitial.text = tempName.first().uppercase()
        }

        // ─── CARGAR DATOS REALES DESDE EL BACKEND ───
        if (currentUserId != 0) {
            tvLoadingStatus.visibility = View.VISIBLE
            tvLoadingStatus.text = "Sincronizando con el servidor..."

            ApiClient.instance.getUsuario(currentUserId).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            // Llenar formulario con datos reales de la DB
                            etNombre.setText(user.nombre)
                            etCorreo.setText(user.email)
                            etDocType.setText(user.tipo_documento ?: "")
                            etDocNum.setText(user.numero_documento ?: "")
                            etTelefono.setText(user.telefono ?: "")
                            etDireccion.setText(user.direccion ?: "")

                            // Actualizar header
                            tvHeaderName.text = user.nombre
                            tvHeaderEmail.text = user.email
                            if (user.nombre.isNotEmpty()) {
                                tvAvatarInitial.text = user.nombre.first().uppercase()
                            }

                            // Sincronizar SharedPreferences con los datos de la DB
                            prefs.edit()
                                .putString("userName", user.nombre)
                                .putString("userEmail", user.email)
                                .putString("userDocType", user.tipo_documento)
                                .putString("userDocNum", user.numero_documento)
                                .putString("userPhone", user.telefono)
                                .putString("userAddress", user.direccion)
                                .apply()

                            tvLoadingStatus.text = "✓ Datos sincronizados con el servidor"
                            tvLoadingStatus.setTextColor(resources.getColor(R.color.primary, theme))

                            // Ocultar después de 2 segundos
                            tvLoadingStatus.postDelayed({
                                tvLoadingStatus.visibility = View.GONE
                            }, 2000)
                        }
                    } else {
                        tvLoadingStatus.text = "⚠ No se pudieron cargar datos del servidor (${response.code()})"
                        tvLoadingStatus.setTextColor(resources.getColor(R.color.error_text, theme))
                    }
                }

                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    tvLoadingStatus.text = "⚠ Sin conexión al servidor. Mostrando datos locales."
                    tvLoadingStatus.setTextColor(resources.getColor(R.color.error_text, theme))
                }
            })
        } else {
            tvLoadingStatus.text = "⚠ No se encontró ID de usuario"
            tvLoadingStatus.setTextColor(resources.getColor(R.color.error_text, theme))
        }

        // ─── BOTÓN GUARDAR ───
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etCorreo.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val req = UsuarioUpdateRequest(
                nombre = nombre,
                email = email,
                tipo_documento = etDocType.text.toString().trim().takeIf { it.isNotEmpty() },
                numero_documento = etDocNum.text.toString().trim().takeIf { it.isNotEmpty() },
                telefono = etTelefono.text.toString().trim().takeIf { it.isNotEmpty() },
                direccion = etDireccion.text.toString().trim().takeIf { it.isNotEmpty() }
            )

            if (currentUserId != 0) {
                ApiClient.instance.updateUsuario(currentUserId, req).enqueue(object : Callback<Usuario> {
                    override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                        if (response.isSuccessful) {
                            val u = response.body()
                            if (u != null) {
                                // Actualizar SharedPreferences con la respuesta del servidor
                                prefs.edit()
                                    .putString("userName", u.nombre)
                                    .putString("userEmail", u.email)
                                    .putString("userDocType", u.tipo_documento)
                                    .putString("userDocNum", u.numero_documento)
                                    .putString("userPhone", u.telefono)
                                    .putString("userAddress", u.direccion)
                                    .apply()

                                // Actualizar header
                                tvHeaderName.text = u.nombre
                                tvHeaderEmail.text = u.email
                                if (u.nombre.isNotEmpty()) {
                                    tvAvatarInitial.text = u.nombre.first().uppercase()
                                }
                            }
                            Toast.makeText(this@PerfilActivity, "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMsg = when (response.code()) {
                                400 -> "Datos inválidos"
                                409 -> "El correo ya está en uso"
                                else -> "Error al actualizar (${response.code()})"
                            }
                            Toast.makeText(this@PerfilActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Usuario>, t: Throwable) {
                        Toast.makeText(this@PerfilActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "No se encontró ID de usuario. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
            }
        }

        // ─── LOGOUT ───
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
