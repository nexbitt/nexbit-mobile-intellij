package com.example.nexbitmobile.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.UsuarioUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisDireccionesActivity : AppCompatActivity() {

    private lateinit var etDireccion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var progressBar: ProgressBar
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mis_direcciones)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)

        etDireccion = findViewById(R.id.etDireccion)
        btnGuardar = findViewById(R.id.btnGuardar)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Pre-fill from SharedPreferences
        etDireccion.setText(prefs.getString("userAddress", ""))

        // Load fresh data from API
        cargarDireccion()

        btnGuardar.setOnClickListener { guardarDireccion() }
    }

    private fun cargarDireccion() {
        if (userId == 0) return

        progressBar.visibility = View.VISIBLE

        ApiClient.instance.getUsuario(userId).enqueue(object : Callback<com.example.nexbitmobile.model.Usuario> {
            override fun onResponse(
                call: Call<com.example.nexbitmobile.model.Usuario>,
                response: Response<com.example.nexbitmobile.model.Usuario>
            ) {
                if (!isFinishing) progressBar.visibility = View.GONE
                if (!isFinishing && response.isSuccessful) {
                    response.body()?.let { user ->
                        val direccion = user.direccion ?: ""
                        etDireccion.setText(direccion)
                        getSharedPreferences("app", MODE_PRIVATE)
                            .edit().putString("userAddress", direccion).apply()
                    }
                } else if (response.code() == 401) {
                    Log.e("MisDirecciones", "Sesión expirada, redirigiendo a login")
                } else {
                    Log.e("MisDirecciones", "Error HTTP ${response.code()} cargando dirección")
                }
            }

            override fun onFailure(
                call: Call<com.example.nexbitmobile.model.Usuario>,
                t: Throwable
            ) {
                if (!isFinishing) progressBar.visibility = View.GONE
                Log.e("MisDirecciones", "Error cargando dirección", t)
            }
        })
    }

    private fun guardarDireccion() {
        val direccion = etDireccion.text.toString().trim()
        if (direccion.isEmpty()) {
            Toast.makeText(this, "La dirección no puede estar vacía", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == 0) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnGuardar.isEnabled = false

        val request = UsuarioUpdateRequest(direccion = direccion)

        ApiClient.instance.updateUsuario(userId, request).enqueue(object : Callback<com.example.nexbitmobile.model.Usuario> {
            override fun onResponse(
                call: Call<com.example.nexbitmobile.model.Usuario>,
                response: Response<com.example.nexbitmobile.model.Usuario>
            ) {
                if (!isFinishing) {
                    progressBar.visibility = View.GONE
                    btnGuardar.isEnabled = true
                }

                if (response.isSuccessful) {
                    getSharedPreferences("app", MODE_PRIVATE)
                        .edit().putString("userAddress", direccion).apply()
                    if (!isFinishing) {
                        Toast.makeText(this@MisDireccionesActivity, "Dirección guardada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else if (response.code() == 401) {
                    Log.e("MisDirecciones", "Sesión expirada al guardar")
                } else {
                    val msg = response.errorBody()?.string() ?: "Error al guardar"
                    if (!isFinishing) {
                        Toast.makeText(this@MisDireccionesActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(
                call: Call<com.example.nexbitmobile.model.Usuario>,
                t: Throwable
            ) {
                if (!isFinishing) {
                    progressBar.visibility = View.GONE
                    btnGuardar.isEnabled = true
                    Toast.makeText(this@MisDireccionesActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
