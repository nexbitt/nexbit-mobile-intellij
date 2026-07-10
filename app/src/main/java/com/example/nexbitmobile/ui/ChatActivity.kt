package com.example.nexbitmobile.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Conversacion
import com.example.nexbitmobile.model.Mensaje
import com.example.nexbitmobile.model.MensajeRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private var pedidoId: Int = -1
    private var conversacionId: Int = -1
    private var currentUserId: Int = 0
    private var currentUserRole: String = ""

    private lateinit var rvMensajes: RecyclerView
    private lateinit var etMensaje: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvChatTitle: TextView

    private val mensajes = mutableListOf<Mensaje>()
    private lateinit var adapter: MensajeAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val pollInterval = 3000L

    private val pollRunnable = object : Runnable {
        override fun run() {
            cargarConversacion()
            handler.postDelayed(this, pollInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pedidoId = intent.getIntExtra("pedido_id", -1)
        if (pedidoId == -1) {
            pedidoId = intent.getIntExtra("pedidoId", -1)
        }
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        currentUserId = prefs.getInt("userId", 0)
        currentUserRole = prefs.getString("userRole", "") ?: ""

        if (pedidoId == -1 || currentUserId == 0) {
            Toast.makeText(this, "Error al abrir chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupListeners()
        inicializarChat()
    }

    private fun bindViews() {
        rvMensajes = findViewById(R.id.rvMensajes)
        etMensaje = findViewById(R.id.etMensaje)
        btnSend = findViewById(R.id.btnSend)
        tvChatTitle = findViewById(R.id.tvChatTitle)

        adapter = MensajeAdapter(mensajes, currentUserId)
        rvMensajes.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMensajes.adapter = adapter
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnSend.setOnClickListener {
            val texto = etMensaje.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarMensaje(texto)
            }
        }
    }

    private fun inicializarChat() {
        tvChatTitle.text = "Chat - Pedido #$pedidoId"
        cargarConversacion()
        handler.postDelayed(pollRunnable, pollInterval)
    }

    private fun cargarConversacion() {
        ApiClient.instance.getConversacion(pedidoId).enqueue(object : Callback<Conversacion> {
            override fun onResponse(call: Call<Conversacion>, response: Response<Conversacion>) {
                if (response.isSuccessful) {
                    val conv = response.body() ?: return
                    conversacionId = conv.id_conversacion

                    if (conversacionId != -1 && mensajes.isEmpty()) {
                        marcarLeidos()
                    }

                    conv.mensajes?.let { nuevosMensajes ->
                        val currentSize = mensajes.size
                        if (nuevosMensajes.size > currentSize) {
                            mensajes.clear()
                            mensajes.addAll(nuevosMensajes)
                            adapter.notifyDataSetChanged()
                            rvMensajes.scrollToPosition(mensajes.size - 1)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Conversacion>, t: Throwable) {
                // Silently retry on next poll
            }
        })
    }

    private fun enviarMensaje(texto: String) {
        if (conversacionId == -1) {
            Toast.makeText(this, "Inicializando chat...", Toast.LENGTH_SHORT).show()
            return
        }

        etMensaje.isEnabled = false
        btnSend.isEnabled = false

        val request = MensajeRequest(mensaje = texto)
        ApiClient.instance.enviarMensaje(conversacionId, request)
            .enqueue(object : Callback<Mensaje> {
                override fun onResponse(call: Call<Mensaje>, response: Response<Mensaje>) {
                    etMensaje.isEnabled = true
                    btnSend.isEnabled = true
                    if (response.isSuccessful) {
                        etMensaje.text.clear()
                        response.body()?.let { msg ->
                            adapter.addMessage(msg)
                            rvMensajes.scrollToPosition(mensajes.size - 1)
                        }
                        marcarLeidos()
                    } else {
                        Toast.makeText(this@ChatActivity, "Error al enviar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Mensaje>, t: Throwable) {
                    etMensaje.isEnabled = true
                    btnSend.isEnabled = true
                    Toast.makeText(this@ChatActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun marcarLeidos() {
        if (conversacionId == -1) return
        ApiClient.instance.marcarLeidos(conversacionId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(pollRunnable)
    }
}
