package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PerfilRepartidorActivity : AppCompatActivity() {

    private lateinit var ivAvatarRepartidor: TextView
    private lateinit var tvNombreRepartidor: TextView
    private lateinit var tvEmailRepartidor: TextView
    private lateinit var tvTelefonoRepartidor: TextView
    private lateinit var statsHoyRepartidor: TextView
    private lateinit var statsCompletadasRepartidor: TextView
    private lateinit var starsRepartidor: TextView
    private lateinit var switchDisponible: Switch
    private lateinit var switchNotificaciones: Switch
    private lateinit var btnCerrarSesion: Button
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_repartidor)

        val toolbar = findViewById<Toolbar>(R.id.toolbarPerfilRepartidor)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(toolbar.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)

        bindViews()
        cargarDatos()
        setupListeners()
    }

    private fun bindViews() {
        ivAvatarRepartidor = findViewById(R.id.ivAvatarRepartidor)
        tvNombreRepartidor = findViewById(R.id.tvNombreRepartidor)
        tvEmailRepartidor = findViewById(R.id.tvEmailRepartidor)
        tvTelefonoRepartidor = findViewById(R.id.tvTelefonoRepartidor)
        statsHoyRepartidor = findViewById(R.id.statsHoyRepartidor)
        statsCompletadasRepartidor = findViewById(R.id.statsCompletadasRepartidor)
        starsRepartidor = findViewById(R.id.starsRepartidor)
        switchDisponible = findViewById(R.id.switchDisponible)
        switchNotificaciones = findViewById(R.id.switchNotificaciones)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    }

    private fun cargarDatos() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val nombre = prefs.getString("userName", "Repartidor") ?: "Repartidor"
        val email = prefs.getString("userEmail", "") ?: ""
        val telefono = prefs.getString("userPhone", "+57 300 000 0000") ?: "+57 300 000 0000"

        ivAvatarRepartidor.text = nombre.first().uppercase()
        tvNombreRepartidor.text = nombre
        tvEmailRepartidor.text = email
        tvTelefonoRepartidor.text = telefono

        if (userId != 0) {
            ApiClient.instance.getRepartidor(userId).enqueue(object : Callback<com.example.nexbitmobile.model.RepartidorResponse> {
                override fun onResponse(call: Call<com.example.nexbitmobile.model.RepartidorResponse>, response: Response<com.example.nexbitmobile.model.RepartidorResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        val pedidos = data?.pedidos_repartidor ?: emptyList()
                        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        statsHoyRepartidor.text = pedidos.count { it.fecha_asignacion?.take(10) == hoy }.toString()
                        statsCompletadasRepartidor.text = pedidos.count { it.estado == "ENTREGADO" }.toString()
                        // Rating estimado
                        val total = pedidos.size
                        val completadas = pedidos.count { it.estado == "ENTREGADO" }
                        val rating = if (total > 0) String.format("%.1f", (completadas.toDouble() / total) * 5) else "0.0"
                        starsRepartidor.text = "★ $rating"
                    }
                }
                override fun onFailure(call: Call<com.example.nexbitmobile.model.RepartidorResponse>, t: Throwable) {}
            })
        }
    }

    private fun setupListeners() {
        switchDisponible.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Disponible" else "No disponible", Toast.LENGTH_SHORT).show()
        }
        switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Notificaciones activadas" else "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
        }
        btnCerrarSesion.setOnClickListener {
            getSharedPreferences("app", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}
