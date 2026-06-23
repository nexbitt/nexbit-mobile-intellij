package com.example.nexbitmobile.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class EntregasActivity : AppCompatActivity() {

    private lateinit var rvEntregas: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: EntregaAdapter

    private lateinit var activeBannerCard: com.google.android.material.card.MaterialCardView
    private lateinit var activeBannerTitle: TextView
    private lateinit var activeBannerCliente: TextView
    private lateinit var activeBannerDireccion: TextView
    private lateinit var stepAsignado: TextView
    private lateinit var stepEnCamino: TextView
    private lateinit var stepEntregado: TextView
    private lateinit var activeBannerMapa: Button
    private lateinit var activeBannerEstado: Button

    private lateinit var statsHoy: TextView
    private lateinit var statsCompletadas: TextView
    private lateinit var statsPendientes: TextView

    private var userId: Int = 0
    private var currentTab: Int = 0
    private var todasEntregas: List<PedidoRepartidor> = emptyList()
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

        bindViews()
        setupTabs()
        setupAdapter()
        setupActiveBannerButtons()

        if (userId != 0) {
            cargarDatos()
        } else {
            Toast.makeText(this, "Error: No se encontró sesión activa", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun bindViews() {        tabLayout = findViewById(R.id.tabLayout)
        rvEntregas = findViewById(R.id.rvEntregas)
        activeBannerCard = findViewById(R.id.activeBannerCard)
        activeBannerTitle = findViewById(R.id.activeBannerTitle)
        activeBannerCliente = findViewById(R.id.activeBannerCliente)
        activeBannerDireccion = findViewById(R.id.activeBannerDireccion)
        stepAsignado = findViewById(R.id.stepAsignado)
        stepEnCamino = findViewById(R.id.stepEnCamino)
        stepEntregado = findViewById(R.id.stepEntregado)
        activeBannerMapa = findViewById(R.id.activeBannerMapa)
        activeBannerEstado = findViewById(R.id.activeBannerEstado)
        statsHoy = findViewById(R.id.statsHoy)
        statsCompletadas = findViewById(R.id.statsCompletadas)
        statsPendientes = findViewById(R.id.statsPendientes)
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                cargarDatos()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) { cargarDatos() }
        })
    }

    private fun setupAdapter() {
        adapter = EntregaAdapter(
            entregas = emptyList(),
            onVerMapaClick = { entrega -> abrirMapa(entrega.direccion_entrega) },
            onAccionClick = { entrega -> mostrarDialogoEstado(entrega) },
            onItemClick = { entrega -> mostrarDialogoDetalle(entrega) }
        )
        rvEntregas.adapter = adapter
    }

    private fun setupActiveBannerButtons() {
        activeBannerMapa.setOnClickListener {
            val activa = todasEntregas.firstOrNull { it.estado == "ASIGNADO" || it.estado == "EN_CAMINO" }
            if (activa != null) abrirMapa(activa.direccion_entrega)
        }
        activeBannerEstado.setOnClickListener {
            val activa = todasEntregas.firstOrNull { it.estado == "ASIGNADO" || it.estado == "EN_CAMINO" }
            if (activa != null) mostrarDialogoEstado(activa)        }
    }

    private fun cargarDatos() {
        when (currentTab) {
            0 -> cargarStats()
            1 -> cargarPedidosDisponibles()
            2 -> cargarHistorialEntregas()
        }
    }

    private fun cargarStats() {
        ApiClient.instance.getRepartidor(userId).enqueue(object : Callback<RepartidorResponse> {
            override fun onResponse(call: Call<RepartidorResponse>, response: Response<RepartidorResponse>) {
                if (response.isSuccessful) {
                    todasEntregas = response.body()?.pedidos_repartidor ?: emptyList()
                    val activas = todasEntregas.filter { it.estado == "ASIGNADO" || it.estado == "EN_CAMINO" }
                    adapter.updateData(activas)
                    actualizarActiveBanner(todasEntregas)
                    actualizarStats(todasEntregas)
                    if (activas.isEmpty()) {
                        Toast.makeText(this@EntregasActivity, "No tienes entregas activas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al cargar entregas", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RepartidorResponse>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun cargarHistorialEntregas() {
        ApiClient.instance.getRepartidor(userId).enqueue(object : Callback<RepartidorResponse> {
            override fun onResponse(call: Call<RepartidorResponse>, response: Response<RepartidorResponse>) {
                if (response.isSuccessful) {
                    todasEntregas = response.body()?.pedidos_repartidor ?: emptyList()
                    val historial = todasEntregas.filter { it.estado == "ENTREGADO" || it.estado == "CANCELADO" }
                    adapter.updateData(historial)
                    actualizarStats(todasEntregas)
                    if (historial.isEmpty()) {
                        Toast.makeText(this@EntregasActivity, "No tienes historial de entregas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al cargar historial", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RepartidorResponse>, t: Throwable) {                Toast.makeText(this@EntregasActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
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

    private fun actualizarStats(entregas: List<PedidoRepartidor>) {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val statsHoyVal = entregas.count { it.fecha_asignacion?.take(10) == hoy }
        val completadas = entregas.count { it.estado == "ENTREGADO" }
        val pendientes = entregas.count { it.estado == "ASIGNADO" || it.estado == "EN_CAMINO" }
        statsHoy.text = statsHoyVal.toString()
        statsCompletadas.text = completadas.toString()
        statsPendientes.text = pendientes.toString()
    }

    private fun actualizarActiveBanner(entregas: List<PedidoRepartidor>) {
        val activa = entregas.firstOrNull { it.estado == "ASIGNADO" || it.estado == "EN_CAMINO" }
        if (activa != null) {
            activeBannerCard.visibility = View.VISIBLE
            activeBannerTitle.text = "Pedido #${activa.id_pedido}"
            activeBannerCliente.text = "Cliente: ${activa.cliente?.nombre ?: "N/A"}"
            activeBannerDireccion.text = activa.direccion_entrega ?: "Sin dirección"
            actualizarTimeline(activa.estado)
        } else {
            activeBannerCard.visibility = View.GONE
        }
    }

    private fun actualizarTimeline(estado: String) {
        val arrays = arrayOf(
            intArrayOf(R.drawable.bg_chip_selected, R.drawable.bg_chip, R.drawable.bg_chip) to listOf("#ffffff", "#ffffff", "#ffffff"),
            intArrayOf(R.drawable.bg_chip_selected, R.drawable.bg_chip_selected, R.drawable.bg_chip) to listOf("#ffffff", "#ffffff", "#ffffff"),
            intArrayOf(R.drawable.bg_chip_selected, R.drawable.bg_chip_selected, R.drawable.bg_chip_selected) to listOf("#ffffff", "#ffffff", "#ffffff")
        )
        val index = when (estado) {
            "ASIGNADO" -> 0
            "EN_CAMINO" -> 1
            "ENTREGADO" -> 2
            else -> 0
        }
        stepAsignado.setBackgroundResource(if (index >= 0) R.drawable.bg_chip_selected else R.drawable.bg_chip)
        stepEnCamino.setBackgroundResource(if (index >= 1) R.drawable.bg_chip_selected else R.drawable.bg_chip)
        stepEntregado.setBackgroundResource(if (index >= 2) R.drawable.bg_chip_selected else R.drawable.bg_chip)    }

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

    private fun mostrarDialogoReclamar(entrega: PedidoRepartidor) {
        AlertDialog.Builder(this)
            .setTitle("Reclamar Pedido")
            .setMessage("¿Deseas asignarte el pedido #${entrega.id_pedido}?\n\nCliente: ${entrega.cliente?.nombre ?: "N/A"}\nDirección: ${entrega.direccion_entrega ?: "N/A"}")
            .setPositiveButton("Sí, asignarme") { _, _ -> reclamarPedido(entrega.id_pedido) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun reclamarPedido(pedidoId: Int) {
        val request = AsignarPedidoRequest(pedidoId)
        ApiClient.instance.asignarPedido(userId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EntregasActivity, "Pedido asignado con éxito", Toast.LENGTH_SHORT).show()
                    val tab0 = tabLayout.getTabAt(0)
                    if (currentTab == 0) {
                        cargarDatos()
                    } else {
                        tab0?.select()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al asignar pedido (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun mostrarDialogoEstado(entrega: PedidoRepartidor) {
        val estados: Array<String> = when (entrega.estado) {
            "ASIGNADO" -> arrayOf("EN_CAMINO")
            "EN_CAMINO" -> arrayOf("ENTREGADO", "CANCELADO")
            else -> return
        }
        val etiquetas: Array<String> = estados.map { estado ->
            when (estado) {
                "EN_CAMINO" -> "🚚 Salí a entregar"
                "ENTREGADO" -> "✅ Pedido Entregado"
                "CANCELADO" -> "❌ Cancelar entrega"
                else -> estado
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Cambiar estado — Pedido #${entrega.id_pedido}")
            .setItems(etiquetas) { _, which ->
                val nuevoEstado = estados[which]
                confirmarCambioEstado(entrega, nuevoEstado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarCambioEstado(entrega: PedidoRepartidor, nuevoEstado: String) {
        val mensaje = when (nuevoEstado) {
            "EN_CAMINO" -> "¿Confirmas que ya saliste a entregar el pedido #${entrega.id_pedido}?"
            "ENTREGADO" -> "¿Confirmas que el pedido #${entrega.id_pedido} fue entregado exitosamente?"
            "CANCELADO" -> "¿Seguro que deseas cancelar el pedido #${entrega.id_pedido}?"
            else -> "¿Cambiar estado a $nuevoEstado?"
        }
        AlertDialog.Builder(this)
            .setTitle("Confirmar cambio")
            .setMessage(mensaje)
            .setPositiveButton("Confirmar") { _, _ ->
                actualizarEstado(entrega.id_pedido, nuevoEstado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoDetalle(entrega: PedidoRepartidor) {
        val view = layoutInflater.inflate(R.layout.dialog_detalle_entrega, null)
        if (view == null) {
            // Fallback al texto plano si no existe el layout
            val sb = StringBuilder()
            sb.append("📦 Pedido #${entrega.id_pedido}\n")
            sb.append("─────────────────────────\n")
            sb.append("Cliente:   ${entrega.cliente?.nombre ?: "No especificado"}\n")
            sb.append("Dirección: ${entrega.direccion_entrega ?: "No especificada"}\n")
            sb.append("Notas:     ${entrega.notas_entrega ?: "Ninguna"}\n")
            sb.append("Asignado:  ${entrega.fecha_asignacion?.take(16)?.replace("T", " ") ?: "N/A"}\n")
            sb.append("Total:     $${String.format("%,.2f", entrega.total)}\n")

            val detalles = entrega.detalle_pedido
            if (!detalles.isNullOrEmpty()) {
                sb.append("\n🛍 Productos:\n")
                for (det in detalles) {
                    val nombre = det.producto?.nombre ?: "Producto"
                    sb.append("  • ${det.cantidad}x $nombre → $${String.format("%,.2f", det.subtotal)}\n")
                }            }

            val historial = entrega.seguimiento
            if (!historial.isNullOrEmpty()) {
                sb.append("\n📋 Historial:\n")
                for (seg in historial) {
                    val desde = seg.estado_anterior ?: "INICIO"
                    val hasta = seg.estado_nuevo
                    val quien = seg.usuario?.nombre ?: "Sistema"
                    val fecha = seg.fecha?.take(16)?.replace("T", " ") ?: "?"
                    sb.append("  $fecha  $desde → $hasta  ($quien)\n")
                    if (!seg.notas.isNullOrEmpty()) sb.append("    \"${seg.notas}\"\n")
                }
            } else {
                sb.append("\n📋 Sin historial de seguimiento.\n")
            }

            val builder = AlertDialog.Builder(this)
                .setTitle("Detalle del Pedido")
                .setMessage(sb.toString())
                .setPositiveButton("Cerrar", null)

            if (entrega.estado == "ASIGNADO" || entrega.estado == "EN_CAMINO") {
                builder.setNeutralButton("Cambiar Estado") { _, _ -> mostrarDialogoEstado(entrega) }
            }
            builder.show()
            return
        }

        // Usar el layout dialog_detalle_entrega si existe
        actualizarDialogTimeline(view, entrega.estado)
        view.findViewById<TextView>(R.id.dialogPedidoId).text = "Pedido #${entrega.id_pedido}"
        view.findViewById<TextView>(R.id.dialogCliente).text = "Cliente: ${entrega.cliente?.nombre ?: "N/A"}"
        view.findViewById<TextView>(R.id.dialogDireccion).text = "Dirección: ${entrega.direccion_entrega ?: "N/A"}"
        view.findViewById<TextView>(R.id.dialogNotas).text = "Notas: ${entrega.notas_entrega ?: "Ninguna"}"
        view.findViewById<TextView>(R.id.dialogFecha).text = "Asignado: ${entrega.fecha_asignacion?.take(16)?.replace("T", " ") ?: "N/A"}"
        view.findViewById<TextView>(R.id.dialogTotal).text = "Total: $${String.format("%,.2f", entrega.total)}"

        val productosLayout = view.findViewById<LinearLayout>(R.id.dialogProductosLayout)
        val detalles = entrega.detalle_pedido
        if (!detalles.isNullOrEmpty()) {
            for (det in detalles) {
                val nombre = det.producto?.nombre ?: "Producto"
                val tv = TextView(this).apply {
                    text = "  • ${det.cantidad}x $nombre → $${String.format("%,.2f", det.subtotal)}"
                    textSize = 13f
                    setPadding(0, 2, 0, 2)
                }
                productosLayout.addView(tv)
            }
        } else {
            productosLayout.addView(TextView(this).apply {
                text = "  Sin productos"
                textSize = 13f; setTextColor(Color.GRAY)
            })
        }

        val historialLayout = view.findViewById<LinearLayout>(R.id.dialogHistorialLayout)
        val seguimiento = entrega.seguimiento
        if (!seguimiento.isNullOrEmpty()) {
            for (seg in seguimiento) {                val desde = seg.estado_anterior ?: "INICIO"
                val hasta = seg.estado_nuevo
                val quien = seg.usuario?.nombre ?: "Sistema"
                val fecha = seg.fecha?.take(16)?.replace("T", " ") ?: "?"
                val tv = TextView(this).apply {
                    text = "  $fecha  $desde → $hasta  ($quien)"
                    textSize = 11f; setPadding(0, 2, 0, 2)
                }
                historialLayout.addView(tv)
                if (!seg.notas.isNullOrEmpty()) {
                    historialLayout.addView(TextView(this).apply {
                        text = "    \"${seg.notas}\""
                        textSize = 11f; setTextColor(Color.GRAY); setPadding(0, 0, 0, 2)
                    })
                }
            }
        } else {
            historialLayout.addView(TextView(this).apply {
                text = "  Sin historial de seguimiento"
                textSize = 11f; setTextColor(Color.GRAY)
            })        }

        val builder = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Cerrar", null)

        if (entrega.estado == "ASIGNADO" || entrega.estado == "EN_CAMINO") {
            builder.setNeutralButton("Cambiar Estado") { _, _ ->
                mostrarDialogoEstado(entrega)
            }
        }
        builder.show()
    }

    private fun actualizarDialogTimeline(view: View, estado: String) {
        val step1 = view.findViewById<TextView>(R.id.dialogStep1)
        val step2 = view.findViewById<TextView>(R.id.dialogStep2)
        val step3 = view.findViewById<TextView>(R.id.dialogStep3)
        if (step1 != null) step1.setBackgroundResource(if (estado == "ASIGNADO" || estado == "EN_CAMINO" || estado == "ENTREGADO") R.drawable.bg_chip_selected else R.drawable.bg_chip)
        if (step2 != null) step2.setBackgroundResource(if (estado == "EN_CAMINO" || estado == "ENTREGADO") R.drawable.bg_chip_selected else R.drawable.bg_chip)
        if (step3 != null) step3.setBackgroundResource(if (estado == "ENTREGADO") R.drawable.bg_chip_selected else R.drawable.bg_chip)
    }

    private fun actualizarEstado(pedidoId: Int, nuevoEstado: String) {
        val request = EstadoPedidoRequest(estado = nuevoEstado, notas = null)
        ApiClient.instance.cambiarEstadoPedido(pedidoId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EntregasActivity, "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al actualizar estado (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
