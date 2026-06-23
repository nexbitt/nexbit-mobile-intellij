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
import com.example.nexbitmobile.model.ConversacionResponse
import com.example.nexbitmobile.model.EnviarMensajeRequest
import com.example.nexbitmobile.model.Mensaje
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private var pedidoId: Int = 0
    private var conversacionId: Int = 0
    private var userId: Int = 0

    private lateinit var rvChat: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var etMensaje: EditText
    private lateinit var btnEnviar: ImageButton
    private lateinit var tvPedidoId: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null
    private val POLLING_INTERVAL = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pedidoId = intent.getIntExtra("pedidoId", 0)
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)

        tvPedidoId = findViewById(R.id.tvPedidoId)
        rvChat = findViewById(R.id.rvChat)
        etMensaje = findViewById(R.id.etMensaje)
        btnEnviar = findViewById(R.id.btnEnviar)

        tvPedidoId.text = "#$pedidoId"

        adapter = ChatAdapter(emptyList(), userId)
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnEnviar.setOnClickListener { enviarMensaje() }

        cargarConversacion()
    }

    private fun cargarConversacion() {
        ApiClient.instance.getConversacion(pedidoId).enqueue(object : Callback<ConversacionResponse> {
            override fun onResponse(call: Call<ConversacionResponse>, response: Response<ConversacionResponse>) {
                if (response.isSuccessful) {
                    val conv = response.body()
                    if (conv != null) {
                        conversacionId = conv.id_conversacion
                        val mensajes = conv.mensajes ?: emptyList()
                        adapter.updateData(mensajes)
                        rvChat.scrollToPosition(adapter.itemCount - 1)
                        iniciarPolling()
                    }
                } else {
                    Toast.makeText(this@ChatActivity, "Error al cargar conversación", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ConversacionResponse>, t: Throwable) {
                Toast.makeText(this@ChatActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun iniciarPolling() {
        pollingRunnable?.let { handler.removeCallbacks(it) }
        pollingRunnable = object : Runnable {
            override fun run() {
                recargarMensajes()
                handler.postDelayed(this, POLLING_INTERVAL)
            }
        }
        handler.postDelayed(pollingRunnable!!, POLLING_INTERVAL)
    }

    private fun recargarMensajes() {
        ApiClient.instance.getConversacion(pedidoId).enqueue(object : Callback<ConversacionResponse> {
            override fun onResponse(call: Call<ConversacionResponse>, response: Response<ConversacionResponse>) {
                if (response.isSuccessful) {
                    val mensajes = response.body()?.mensajes ?: emptyList()
                    adapter.updateData(mensajes)
                }
            }
            override fun onFailure(call: Call<ConversacionResponse>, t: Throwable) {}
        })
    }

    private fun enviarMensaje() {
        val texto = etMensaje.text.toString().trim()
        if (texto.isEmpty() || conversacionId == 0) return

        etMensaje.setText("")
        btnEnviar.isEnabled = false

        val request = EnviarMensajeRequest(texto)
        ApiClient.instance.enviarMensaje(conversacionId, request)
            .enqueue(object : Callback<Mensaje> {
                override fun onResponse(call: Call<Mensaje>, response: Response<Mensaje>) {
                    btnEnviar.isEnabled = true
                    recargarMensajes()
                }

                override fun onFailure(call: Call<Mensaje>, t: Throwable) {
                    btnEnviar.isEnabled = true
                    Toast.makeText(this@ChatActivity, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingRunnable?.let { handler.removeCallbacks(it) }
    }
}
