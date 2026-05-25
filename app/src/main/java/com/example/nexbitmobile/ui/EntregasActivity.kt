package com.example.nexbitmobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.AsignarPedidoRequest
import com.example.nexbitmobile.model.EstadoPedidoRequest
import com.example.nexbitmobile.model.PedidoRepartidor
import com.example.nexbitmobile.model.RepartidorResponse
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntregasActivity : AppCompatActivity() {

    private lateinit var rvEntregas: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: EntregaAdapter
    private var userId: Int = 0
    private var currentTab: Int = 0 // 0: Mis Entregas, 1: Disponibles

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

        tabLayout = findViewById(R.id.tabLayout)
        rvEntregas = findViewById(R.id.rvEntregas)

        adapter = EntregaAdapter(
            entregas = emptyList(),
            onVerMapaClick = { entrega -> abrirMapa(entrega.direccion_entrega) },
            onConfirmarClick = { entrega ->
                if (entrega.estado == "CONFIRMADO" || entrega.estado == "PENDIENTE") {
                    mostrarDialogoReclamar(entrega)
                } else {
                    mostrarDialogoEstado(entrega)
                }
            },
            onItemClick = { entrega -> mostrarDialogoDetalle(entrega) }
        )
        rvEntregas.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
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

    private fun cargarDatos() {
        when (currentTab) {
            0 -> cargarEntregasAsignadas()
            1 -> cargarPedidosDisponibles()
            2 -> cargarHistorialEntregas()
        }
    }

    private fun cargarEntregasAsignadas() {
        ApiClient.instance.getRepartidor(userId).enqueue(object : Callback<RepartidorResponse> {
            override fun onResponse(call: Call<RepartidorResponse>, response: Response<RepartidorResponse>) {
                if (response.isSuccessful) {
                    // Filtrar solo estados activos: ASIGNADO y EN_CAMINO
                    val todas = response.body()?.pedidos_repartidor ?: emptyList()
                    val activas = todas.filter { it.estado == "ASIGNADO" || it.estado == "EN_CAMINO" }
                    adapter.updateData(activas)
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
                    // Historial: ENTREGADO y CANCELADO
                    val todas = response.body()?.pedidos_repartidor ?: emptyList()
                    val historial = todas.filter { it.estado == "ENTREGADO" || it.estado == "CANCELADO" }
                    adapter.updateData(historial)
                    if (historial.isEmpty()) {
                        Toast.makeText(this@EntregasActivity, "No tienes historial de entregas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al cargar historial", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RepartidorResponse>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun cargarPedidosDisponibles() {
        ApiClient.instance.getPedidosSinAsignar().enqueue(object : Callback<List<PedidoRepartidor>> {
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
                    // Bug fix: forzar recarga independientemente del tab actual
                    val tab0 = tabLayout.getTabAt(0)
                    if (currentTab == 0) {
                        // Ya estamos en tab 0, onTabSelected no se dispara → recargar manualmente
                        cargarDatos()
                    } else {
                        tab0?.select() // Cambia a tab 0 y dispara onTabSelected
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

    /**
     * Bug fix principal: el diálogo anterior mezclaba setView() y setItems() que se
     * anulan mutuamente en AlertDialog. Ahora usa setItems() solo para los estados,
     * sin campo de notas roto. El backend acepta notas = null (campo nullable en BD).
     */
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
            }
        }

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
    }

    private fun actualizarEstado(pedidoId: Int, nuevoEstado: String) {
        // notas = null: el campo existe en BD (VARCHAR 500 NULL) pero no es requerido
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