package com.example.nexbitmobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.PedidoRepartidor
import com.example.nexbitmobile.model.ReporteProblemaRequest
import com.example.nexbitmobile.model.RepartoActivoResponse
import com.example.nexbitmobile.model.RepartoStats
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntregasActivity : AppCompatActivity() {

    private lateinit var rvEntregas: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: EntregaAdapter
    private lateinit var layoutStats: LinearLayout
    private lateinit var layoutFilterBar: LinearLayout
    private lateinit var tvDisponiblesCount: TextView
    private lateinit var tvActivoCount: TextView
    private lateinit var tvEntregadosCount: TextView
    private lateinit var tvCanceladosCount: TextView
    private var userId: Int = 0
    private var currentTab: Int = 0
    private var currentFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_entregas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)

        layoutStats = findViewById(R.id.layoutStats)
        layoutFilterBar = findViewById(R.id.layoutFilterBar)
        tvDisponiblesCount = findViewById(R.id.tvDisponiblesCount)
        tvActivoCount = findViewById(R.id.tvActivoCount)
        tvEntregadosCount = findViewById(R.id.tvEntregadosCount)
        tvCanceladosCount = findViewById(R.id.tvCanceladosCount)
        tabLayout = findViewById(R.id.tabLayout)
        rvEntregas = findViewById(R.id.rvEntregas)

        adapter = EntregaAdapter(
            entregas = emptyList(),
            onVerMapaClick = { entrega -> abrirMapa(entrega.direccion_entrega) },
            onAccionClick = { entrega -> manejarAccion(entrega) },
            onItemClick = { entrega -> mostrarDialogoDetalle(entrega) }
        )
        rvEntregas.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                actualizarVisibilidad()
                cargarDatos()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) { cargarDatos() }
        })

        if (userId != 0) {
            cargarDatos()
        } else {
            Toast.makeText(this, "Error: No se encontró sesión activa", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun actualizarVisibilidad() {
        layoutStats.visibility = if (currentTab == 0) View.VISIBLE else View.GONE
        layoutFilterBar.visibility = if (currentTab == 2) View.VISIBLE else View.GONE
    }

    private fun cargarDatos() {
        when (currentTab) {
            0 -> {
                cargarStats()
                cargarEntregasAsignadas()
            }
            1 -> cargarPedidosDisponibles()
            2 -> cargarHistorialEntregas()
        }
    }

    private fun cargarStats() {
        ApiClient.instance.getRepartoStats().enqueue(object : Callback<RepartoStats> {
            override fun onResponse(call: Call<RepartoStats>, response: Response<RepartoStats>) {
                if (response.isSuccessful) {
                    val stats = response.body()
                    tvDisponiblesCount.text = "${stats?.disponibles ?: 0}"
                    tvActivoCount.text = "${stats?.activo ?: 0}"
                    tvEntregadosCount.text = "${stats?.entregados ?: 0}"
                    tvCanceladosCount.text = "${stats?.cancelados ?: 0}"
                }
            }

            override fun onFailure(call: Call<RepartoStats>, t: Throwable) {}
        })
    }

    private fun cargarEntregasAsignadas() {
        ApiClient.instance.getRepartoActivo().enqueue(object : Callback<RepartoActivoResponse> {
            override fun onResponse(call: Call<RepartoActivoResponse>, response: Response<RepartoActivoResponse>) {
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp?.tiene_activo == true && resp.pedido != null) {
                        adapter.updateData(listOf(resp.pedido))
                    } else {
                        mostrarSinEntregas()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al cargar entregas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RepartoActivoResponse>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun cargarPedidosDisponibles() {
        ApiClient.instance.getRepartoDisponibles().enqueue(object : Callback<List<PedidoRepartidor>> {
            override fun onResponse(call: Call<List<PedidoRepartidor>>, response: Response<List<PedidoRepartidor>>) {
                if (response.isSuccessful) {
                    val disponibles = response.body() ?: emptyList()
                    adapter.updateData(disponibles)
                    if (disponibles.isEmpty()) {
                        Toast.makeText(this@EntregasActivity, "No hay pedidos disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al cargar pedidos disponibles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PedidoRepartidor>>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun cargarHistorialEntregas() {
        ApiClient.instance.getRepartoHistorial(currentFilter).enqueue(object : Callback<List<PedidoRepartidor>> {
            override fun onResponse(call: Call<List<PedidoRepartidor>>, response: Response<List<PedidoRepartidor>>) {
                if (response.isSuccessful) {
                    val historial = response.body() ?: emptyList()
                    adapter.updateData(historial)
                    if (historial.isEmpty()) {
                        Toast.makeText(this@EntregasActivity, "No hay historial", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al cargar historial", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PedidoRepartidor>>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun mostrarSinEntregas() {
        adapter.updateData(emptyList())
    }

    private fun manejarAccion(entrega: PedidoRepartidor) {
        when (entrega.estado) {
            "PENDIENTE", "CONFIRMADO", "APROBADO" -> mostrarDialogoTomar(entrega)
            "ASIGNADO" -> confirmarEnCamino(entrega)
            "EN_CAMINO" -> mostrarOpcionesEntrega(entrega)
        }
    }

    private fun mostrarDialogoTomar(entrega: PedidoRepartidor) {
        AlertDialog.Builder(this)
            .setTitle("Tomar Pedido")
            .setMessage("¿Deseas asignarte el pedido #${entrega.id_pedido}?\n\nCliente: ${entrega.cliente?.nombre ?: "N/A"}\nDirección: ${entrega.direccion_entrega ?: "N/A"}")
            .setPositiveButton("Sí, tomar") { _, _ -> tomarPedido(entrega.id_pedido) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun tomarPedido(pedidoId: Int) {
        ApiClient.instance.tomarPedido(pedidoId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EntregasActivity, "Pedido tomado con éxito", Toast.LENGTH_SHORT).show()
                    val tab0 = tabLayout.getTabAt(0)
                    if (currentTab == 0) cargarDatos() else tab0?.select()
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al tomar pedido", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmarEnCamino(entrega: PedidoRepartidor) {
        AlertDialog.Builder(this)
            .setTitle("Iniciar Ruta")
            .setMessage("¿Confirmas que ya saliste a entregar el pedido #${entrega.id_pedido}?")
            .setPositiveButton("Sí, iniciar ruta") { _, _ ->
                ApiClient.instance.marcarEnCamino(entrega.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EntregasActivity, "Ruta iniciada", Toast.LENGTH_SHORT).show()
                            cargarDatos()
                        } else {
                            Toast.makeText(this@EntregasActivity, "Error al iniciar ruta", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@EntregasActivity, "Error de conexión", Toast.LENGTH_LONG).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarOpcionesEntrega(entrega: PedidoRepartidor) {
        val opciones = arrayOf("Confirmar Entrega", "Reportar Problema")
        AlertDialog.Builder(this)
            .setTitle("Pedido #${entrega.id_pedido}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> confirmarEntrega(entrega)
                    1 -> mostrarDialogoReporteProblema(entrega)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEntrega(entrega: PedidoRepartidor) {
        AlertDialog.Builder(this)
            .setTitle("Finalizar Entrega")
            .setMessage("¿Confirmas que el pedido #${entrega.id_pedido} fue entregado exitosamente?")
            .setPositiveButton("Sí, entregado") { _, _ ->
                ApiClient.instance.confirmarEntrega(entrega.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EntregasActivity, "Entrega confirmada", Toast.LENGTH_SHORT).show()
                            cargarDatos()
                        } else {
                            Toast.makeText(this@EntregasActivity, "Error al confirmar entrega", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@EntregasActivity, "Error de conexión", Toast.LENGTH_LONG).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoReporteProblema(entrega: PedidoRepartidor) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_reporte_problema, null)
        builder.setView(view)
        val dialog = builder.create()

        var tipoSeleccionado = "Dirección incorrecta"

        view.findViewById<TextView>(R.id.optionDireccion).setOnClickListener {
            tipoSeleccionado = "Dirección incorrecta"
            Toast.makeText(this, "Seleccionado: $tipoSeleccionado", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<TextView>(R.id.optionCliente).setOnClickListener {
            tipoSeleccionado = "Cliente no responde"
            Toast.makeText(this, "Seleccionado: $tipoSeleccionado", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<TextView>(R.id.optionAccidente).setOnClickListener {
            tipoSeleccionado = "Accidente/Avería"
            Toast.makeText(this, "Seleccionado: $tipoSeleccionado", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<TextView>(R.id.optionOtro).setOnClickListener {
            tipoSeleccionado = "Otro"
            Toast.makeText(this, "Seleccionado: $tipoSeleccionado", Toast.LENGTH_SHORT).show()
        }

        val etNotas = view.findViewById<TextView>(R.id.etNotasProblema)

        view.findViewById<TextView>(R.id.btnCancelarProblema).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.btnEnviarProblema).setOnClickListener {
            val notas = etNotas.text.toString()
            enviarReporteProblema(entrega.id_pedido, tipoSeleccionado, notas)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun enviarReporteProblema(pedidoId: Int, tipo: String, notas: String) {
        val request = ReporteProblemaRequest(tipo, notas.ifEmpty { null })
        ApiClient.instance.reportarProblema(pedidoId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EntregasActivity, "Problema reportado exitosamente", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al reportar problema", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun abrirMapa(direccion: String?) {
        if (direccion.isNullOrEmpty()) {
            Toast.makeText(this, "Dirección no especificada", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(direccion)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val fallback = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(direccion)}")
            startActivity(Intent(Intent.ACTION_VIEW, fallback))
        }
    }

    private fun mostrarDialogoDetalle(entrega: PedidoRepartidor) {
        val sb = StringBuilder()
        sb.append("Pedido #${entrega.id_pedido}\n")
        sb.append("────────────────\n")
        sb.append("Cliente:   ${entrega.cliente?.nombre ?: "N/A"}\n")
        sb.append("Dirección: ${entrega.direccion_entrega ?: "N/A"}\n")
        sb.append("Notas:     ${entrega.notas_entrega ?: "Ninguna"}\n")
        sb.append("Total:     $${String.format("%,.2f", entrega.total)}\n")

        val detalles = entrega.detalle_pedido
        if (!detalles.isNullOrEmpty()) {
            sb.append("\nProductos:\n")
            for (det in detalles) {
                val nombre = det.producto?.nombre ?: "Producto"
                sb.append("  - ${det.cantidad}x $nombre -> $${String.format("%,.2f", det.subtotal)}\n")
            }
        }

        val historial = entrega.seguimiento
        if (!historial.isNullOrEmpty()) {
            sb.append("\nHistorial:\n")
            for (seg in historial) {
                val desde = seg.estado_anterior ?: "INICIO"
                val hasta = seg.estado_nuevo
                val quien = seg.usuario?.nombre ?: "Sistema"
                val fecha = seg.fecha?.take(16)?.replace("T", " ") ?: "?"
                sb.append("  $fecha  $desde -> $hasta  ($quien)\n")
                if (!seg.notas.isNullOrEmpty()) sb.append("    \"${seg.notas}\"\n")
            }
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Detalle del Pedido")
            .setMessage(sb.toString())
            .setPositiveButton("Cerrar", null)

        if (entrega.estado == "ASIGNADO" || entrega.estado == "EN_CAMINO") {
            builder.setNeutralButton("Reportar Problema") { _, _ ->
                mostrarDialogoReporteProblema(entrega)
            }
        }
        builder.show()
    }
}
