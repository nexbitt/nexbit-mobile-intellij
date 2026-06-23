package com.example.nexbitmobile.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
<<<<<<< HEAD
import android.widget.*
=======
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
>>>>>>> origin/main
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import com.example.nexbitmobile.model.PedidoRequest
import com.example.nexbitmobile.model.Usuario
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PedidosAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PedidoAdminAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var chipToday: TextView
    private lateinit var chipWeek: TextView
    private lateinit var chipMonth: TextView

    private var usuariosList = listOf<Usuario>()
<<<<<<< HEAD
    private val estados = arrayOf("PENDIENTE", "CONFIRMADO", "ASIGNADO", "EN_CAMINO", "PAGADO", "ENTREGADO", "CANCELADO")
    private var allPedidos = listOf<Pedido>()
    private var currentDateFilter = "ALL"
=======
    private val estados = arrayOf("PENDIENTE", "CONFIRMADO", "EN_REVISION", "APROBADO", "ASIGNADO", "EN_CAMINO", "ENTREGADO", "CANCELADO")
    private var filterEstado: String? = null
>>>>>>> origin/main

    // Referencia al WebView para impresión (debe mantenerse para que no sea recolectada por el GC)
    private var printWebView: WebView? = null

    private var isClienteView: Boolean = false
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos_admin)

        isClienteView = intent.getBooleanExtra("isClienteView", false)
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (isClienteView) {
            supportActionBar?.title = "Mis Pedidos"
        } else {
            supportActionBar?.title = "Pedidos (Admin)"
        }

        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        chipToday = findViewById(R.id.chipToday)
        chipWeek = findViewById(R.id.chipWeek)
        chipMonth = findViewById(R.id.chipMonth)

        if (isClienteView) {
            fabAdd.hide()
<<<<<<< HEAD
            chipToday.visibility = View.GONE
            chipWeek.visibility = View.GONE
            chipMonth.visibility = View.GONE
        } else {
            setupChips()
=======
        } else {
            fabAdd.show()
            // Add filter button to toolbar for admin view
            val filterButton = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_sort_by_size)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setPadding(8, 8, 8, 8)
                setOnClickListener { showFilterDialog() }
            }
            toolbar.addView(filterButton, 600, 600)
>>>>>>> origin/main
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PedidoAdminAdapter(emptyList(), isClienteView, this::showEditDialog, this::deletePedido, this::descargarTicket)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showCreateDialog() }

        if (!isClienteView) {
            loadUsuarios()
        }
        loadPedidos()
    }

    private fun setupChips() {
        val active = { chip: TextView ->
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(ContextCompat.getColor(this, R.color.chip_selected_text))
        }
        val inactive = { chip: TextView ->
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(ContextCompat.getColor(this, R.color.chip_text))
        }
        val reset = { inactive(chipToday); inactive(chipWeek); inactive(chipMonth) }
        reset(); active(chipToday)

        chipToday.setOnClickListener { reset(); active(chipToday); currentDateFilter = "TODAY"; filterPedidos() }
        chipWeek.setOnClickListener { reset(); active(chipWeek); currentDateFilter = "WEEK"; filterPedidos() }
        chipMonth.setOnClickListener { reset(); active(chipMonth); currentDateFilter = "MONTH"; filterPedidos() }
    }

    private fun loadUsuarios() {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    usuariosList = response.body() ?: emptyList()
                } else {
                    Log.e("PedidosAdmin", "Error loading users: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Log.e("PedidosAdmin", "Users load failed", t)
            }
        })
    }

    private fun loadPedidos() {
        ApiClient.instance.getPedidos().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
<<<<<<< HEAD
                    val todos = response.body() ?: emptyList()
                    allPedidos = if (isClienteView) {
                        todos.filter { it.usuario_id == userId }
                    } else {
                        todos
                    }
                    filterPedidos()
=======
                    var todos = response.body() ?: emptyList()
                    if (isClienteView) {
                        todos = todos.filter { it.usuario_id == userId }
                    }
                    val filtrados = if (filterEstado != null) {
                        todos.filter { it.estado == filterEstado }
                    } else {
                        todos
                    }
                    adapter.updateData(filtrados)
                    val title = if (filterEstado != null) "Pedidos ($filterEstado)" else "Pedidos (Admin)"
                    supportActionBar?.title = title
>>>>>>> origin/main
                } else {
                    Toast.makeText(this@PedidosAdminActivity, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

<<<<<<< HEAD
    private fun filterPedidos() {
        var filtered = allPedidos
        if (!isClienteView && currentDateFilter != "ALL") {
            val cal = Calendar.getInstance()
            when (currentDateFilter) {
                "TODAY" -> cal.add(Calendar.DAY_OF_YEAR, -1)
                "WEEK" -> cal.add(Calendar.DAY_OF_YEAR, -7)
                "MONTH" -> cal.add(Calendar.MONTH, -1)
            }
            val cutoff = cal.time
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            filtered = filtered.filter { p ->
                try {
                    val date = sdf.parse(p.fecha.take(10))
                    date != null && date.after(cutoff)
                } catch (e: Exception) { true }
            }
        }
        adapter.updateData(filtered)
=======
    private fun showFilterDialog() {
        val opciones = arrayOf("Todos", "PENDIENTE", "CONFIRMADO", "EN_REVISION", "APROBADO", "ASIGNADO", "EN_CAMINO", "ENTREGADO", "CANCELADO")
        val seleccion = when (filterEstado) {
            null -> 0
            else -> opciones.indexOfFirst { it == filterEstado }
        }

        AlertDialog.Builder(this)
            .setTitle("Filtrar por estado")
            .setSingleChoiceItems(opciones, if (seleccion >= 0) seleccion else 0) { dialog, which ->
                filterEstado = if (which == 0) null else opciones[which]
                dialog.dismiss()
                loadPedidos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
>>>>>>> origin/main
    }

    private fun showCreateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_pedido, null)
        val spUsuario = view.findViewById<Spinner>(R.id.spUsuario)
        val etTotal = view.findViewById<EditText>(R.id.etTotal)
        val etDireccionEntrega = view.findViewById<EditText>(R.id.etDireccionEntrega)
        val etNotasEntrega = view.findViewById<EditText>(R.id.etNotasEntrega)
        val spEstado = view.findViewById<Spinner>(R.id.spEstado)

        val userNames = usuariosList.map { "${it.nombre} - ${it.numero_documento ?: ""}" }
        spUsuario.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, userNames)
        spEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Pedido")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                if (usuariosList.isEmpty() || etTotal.text.isEmpty()) {
                    Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = usuariosList[spUsuario.selectedItemPosition].id_usuario
                val request = PedidoRequest(
                    usuario_id = userId,
                    total = etTotal.text.toString().toDoubleOrNull() ?: 0.0,
                    estado = estados[spEstado.selectedItemPosition],
                    direccion_entrega = etDireccionEntrega.text.toString(),
                    notas_entrega = etNotasEntrega.text.toString()
                )

                ApiClient.instance.createPedido(request).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@PedidosAdminActivity, "Pedido creado", Toast.LENGTH_SHORT).show()
                        loadPedidos()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("PedidosAdmin", "Create order failed", t)
                        Toast.makeText(this@PedidosAdminActivity, "Error al crear pedido", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(pedido: Pedido) {
        val scrollView = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(24, 24, 24, 24)
        }
        scrollView.addView(container)

        // Info section
        container.addView(TextView(this).apply {
            text = "Pedido #${pedido.id_pedido}"
            textSize = 18f; setTypeface(null, android.graphics.Typeface.BOLD)
        })
        container.addView(TextView(this).apply {
            text = "Cliente: ${pedido.usuario_nombre ?: "N/A"}"
            textSize = 14f; setTextColor(ContextCompat.getColor(this@PedidosAdminActivity, R.color.text_secondary))
        })
        container.addView(TextView(this).apply {
            text = "Total: ${"$"}${String.format("%,.2f", pedido.total)}"
            textSize = 14f; setTypeface(null, android.graphics.Typeface.BOLD)
        })
        container.addView(TextView(this).apply {
            text = "Estado: ${pedido.estado}"
            textSize = 14f
        })

        // Timeline
        if (!isClienteView) {
            container.addView(TextView(this).apply {
                text = "\nTimeline de cambios"
                textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 16, 0, 8)
            })

            val estadosTimeline = listOf("PENDIENTE", "CONFIRMADO", "ASIGNADO", "EN_CAMINO", "ENTREGADO")
            val currentIdx = estadosTimeline.indexOf(pedido.estado)
            for ((i, est) in estadosTimeline.withIndex()) {
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL }
                val dotColor = when {
                    i < currentIdx -> ContextCompat.getColor(this@PedidosAdminActivity, R.color.success)
                    i == currentIdx -> ContextCompat.getColor(this@PedidosAdminActivity, R.color.warning)
                    else -> Color.parseColor("#E5E7EB")
                }
                val dot = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(12, 12).apply { marginEnd = 12 }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL; setColor(dotColor)
                    }
                }
                row.addView(dot)
                row.addView(TextView(this).apply {
                    text = est; textSize = 14f
                    setTypeface(null, if (i <= currentIdx) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                })
                container.addView(row)
            }

            // Action buttons for payment
            if (pedido.estado == "PENDIENTE" || pedido.estado == "CONFIRMADO") {
                val btnRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 16 }
                }

                btnRow.addView(Button(this).apply {
                    text = "Aprobar Pago"
                    setTextColor(Color.WHITE)
                    setBackgroundColor(ContextCompat.getColor(this@PedidosAdminActivity, R.color.success))
                    layoutParams = LinearLayout.LayoutParams(0, 48, 1f).apply { marginEnd = 8 }
                    setOnClickListener {
                        cambiarEstadoPedido(pedido.id_pedido, "PAGADO")
                    }
                })

                btnRow.addView(Button(this).apply {
                    text = "Rechazar Pago"
                    setTextColor(Color.WHITE)
                    setBackgroundColor(ContextCompat.getColor(this@PedidosAdminActivity, R.color.error_text))
                    layoutParams = LinearLayout.LayoutParams(0, 48, 1f)
                    setOnClickListener {
                        val input = EditText(this@PedidosAdminActivity).apply { hint = "Motivo del rechazo" }
                        AlertDialog.Builder(this@PedidosAdminActivity)
                            .setTitle("Rechazar Pago")
                            .setView(input)
                            .setPositiveButton("Rechazar") { _, _ ->
                                cambiarEstadoPedido(pedido.id_pedido, "CANCELADO")
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                })
                container.addView(btnRow)
            }

            container.addView(Button(this).apply {
                text = "Chatear"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    48
                ).apply { topMargin = 8 }
                setOnClickListener {
                    Toast.makeText(this@PedidosAdminActivity, "Chat próximamente", Toast.LENGTH_SHORT).show()
                }
            })

            container.addView(Button(this).apply {
                text = "Ver Comprobante"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    48
                ).apply { topMargin = 8 }
                setOnClickListener {
                    Toast.makeText(this@PedidosAdminActivity, "Comprobante próximamente", Toast.LENGTH_SHORT).show()
                }
            })
        }

        AlertDialog.Builder(this)
            .setTitle("Detalle del Pedido")
            .setView(scrollView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun cambiarEstadoPedido(pedidoId: Int, nuevoEstado: String) {
        val request = PedidoRequest(
            usuario_id = 0,
            total = 0.0,
            estado = nuevoEstado,
            direccion_entrega = null,
            notas_entrega = null
        )
        ApiClient.instance.updatePedido(pedidoId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@PedidosAdminActivity, "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                    loadPedidos()
                } else {
                    Toast.makeText(this@PedidosAdminActivity, "Error (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@PedidosAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deletePedido(pedido: Pedido) {
        if (isClienteView) {
            AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("¿Deseas cancelar el pedido #${pedido.id_pedido}?")
                .setPositiveButton("Sí") { _, _ ->
                    ApiClient.instance.cancelarPedido(pedido.id_pedido).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@PedidosAdminActivity, "Pedido cancelado con éxito", Toast.LENGTH_SHORT).show()
                                loadPedidos()
                            } else {
                                Toast.makeText(this@PedidosAdminActivity, "No se pudo cancelar el pedido", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("PedidosAdmin", "Cancel order failed", t)
                            Toast.makeText(this@PedidosAdminActivity, "Fallo de red al cancelar", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Deseas eliminar el pedido #${pedido.id_pedido}?")
                .setPositiveButton("Sí") { _, _ ->
                    ApiClient.instance.deletePedido(pedido.id_pedido).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Toast.makeText(this@PedidosAdminActivity, "Pedido eliminado", Toast.LENGTH_SHORT).show()
                            loadPedidos()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("PedidosAdmin", "Delete order failed", t)
                            Toast.makeText(this@PedidosAdminActivity, "Error al eliminar pedido", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun descargarTicket(pedido: Pedido) {
        ApiClient.instance.getPedidoTicket(pedido.id_pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { pedidoTicket ->
                        generarHtmlYPdf(pedidoTicket)
                    } ?: run {
                        Toast.makeText(this@PedidosAdminActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PedidosAdminActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Log.e("PedidosAdmin", "Ticket download failed", t)
                Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generarHtmlYPdf(pedido: Pedido) {
        val detalles = pedido.detalles ?: emptyList()
        val filasProductos = if (detalles.isNotEmpty()) {
            detalles.joinToString("") { d ->
                """
                <tr>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;">${d.producto_nombre}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:center;">${d.cantidad}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">${"$"}${d.precio_unitario}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">${"$"}${d.subtotal}</td>
                </tr>
                """
            }
        } else {
            "<tr><td colspan='4' style='padding:12px;text-align:center;color:#94a3b8;'>Este pedido no tiene productos detallados</td></tr>"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8"/>
                <title>Comprobante de Pedido - #${pedido.id_pedido}</title>
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Inter', sans-serif; background: #e2e8f0; padding: 40px 20px; color: #1e293b; }
                    .ticket { max-width: 600px; margin: 0 auto; background: #fff; border-radius: 4px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); overflow: hidden; border-top: 6px solid #0f172a; }
                    .ticket-header { padding: 32px 32px 16px 32px; display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 1px solid #f1f5f9; }
                    .ticket-header .brand { font-size: 1.75rem; font-weight: 700; color: #0f172a; margin-bottom: 4px; letter-spacing: -0.5px; }
                    .ticket-header .doc-type { font-size: 0.85rem; color: #64748b; text-transform: uppercase; letter-spacing: 1px; font-weight: 600; }
                    .ticket-header .order-id { text-align: right; }
                    .ticket-header .order-id-label { font-size: 0.75rem; color: #64748b; text-transform: uppercase; font-weight: 600; margin-bottom: 4px; }
                    .ticket-header .order-id-value { font-size: 1.25rem; font-weight: 700; color: #0f172a; }
                    .ticket-body { padding: 32px; }
                    .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 32px; background: #f8fafc; padding: 20px; border-radius: 6px; border: 1px solid #f1f5f9; }
                    .info-item label { display: block; font-size: 0.75rem; text-transform: uppercase; color: #64748b; font-weight: 600; margin-bottom: 6px; }
                    .info-item span { font-size: 0.95rem; font-weight: 500; color: #0f172a; }
                    .badge { display: inline-block; padding: 4px 12px; border-radius: 4px; font-size: 0.8rem; font-weight: 600; border: 1px solid transparent; }
                    .badge-pendiente { background: #fffbeb; color: #b45309; border-color: #fde68a; }
                    .badge-pagado { background: #f0fdf4; color: #15803d; border-color: #bbf7d0; }
                    .badge-entregado { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
                    .badge-cancelado { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 32px; }
                    thead th { border-bottom: 2px solid #e2e8f0; padding: 12px 12px; font-size: 0.8rem; text-transform: uppercase; color: #64748b; font-weight: 600; text-align: left; }
                    thead th:nth-child(2), thead th:nth-child(3), thead th:nth-child(4) { text-align: center; }
                    thead th:nth-child(3), thead th:nth-child(4) { text-align: right; }
                    tbody td { padding: 14px 12px; font-size: 0.95rem; color: #334155; border-bottom: 1px solid #f1f5f9; }
                    .total-section { display: flex; justify-content: flex-end; }
                    .total-box { width: 250px; }
                    .total-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; }
                    .total-row.final { border-top: 2px solid #0f172a; padding-top: 16px; margin-top: 4px; }
                    .total-row .label { font-size: 0.9rem; font-weight: 600; color: #64748b; }
                    .total-row.final .label { font-size: 1.1rem; font-weight: 700; color: #0f172a; }
                    .total-row .amount { font-size: 1rem; font-weight: 600; color: #334155; }
                    .total-row.final .amount { font-size: 1.5rem; font-weight: 700; color: #0f172a; }
                    .ticket-footer { text-align: center; padding: 24px 32px; background: #fff; border-top: 1px solid #f1f5f9; }
                    .ticket-footer p { font-size: 0.85rem; color: #64748b; line-height: 1.5; }
                    .ticket-footer .doc-info { font-size: 0.75rem; color: #94a3b8; margin-top: 12px; }
                    @media print {
                      body { background: #fff; padding: 0; }
                      .ticket { box-shadow: none; border: none; border-top: 6px solid #0f172a; }
                    }
                </style>
            </head>
            <body>
                <div class="ticket">
                    <div class="ticket-header">
                        <div>
                            <div class="brand">Nexbit</div>
                            <div class="doc-type">Comprobante de Pedido</div>
                        </div>
                        <div class="order-id">
                            <div class="order-id-label">Nº de Pedido</div>
                            <div class="order-id-value">${String.format("%06d", pedido.id_pedido)}</div>
                        </div>
                    </div>
                    <div class="ticket-body">
                        <div class="info-grid">
                            <div class="info-item">
                                <label>Cliente</label>
                                <span>${pedido.usuario_nombre ?: "N/A"}</span>
                            </div>
                            <div class="info-item">
                                <label>Documento de Identidad</label>
                                <span>${pedido.numero_documento ?: "N/A"}</span>
                            </div>
                            <div class="info-item">
                                <label>Fecha de Emisión</label>
                                <span>${pedido.fecha}</span>
                            </div>
                            <div class="info-item">
                                <label>Estado del Pedido</label>
                                <div>
                                    <span class="badge badge-${pedido.estado?.lowercase() ?: "pendiente"}">${pedido.estado ?: "PENDIENTE"}</span>
                                </div>
                            </div>
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th>Descripción del Producto</th>
                                    <th style="text-align:center;">Cantidad</th>
                                    <th style="text-align:right;">Precio Unitario</th>
                                    <th style="text-align:right;">Subtotal</th>
                                </tr>
                            </thead>
                            <tbody>
                                $filasProductos
                            </tbody>
                        </table>
                        <div class="total-section">
                            <div class="total-box">
                                <div class="total-row final">
                                    <span class="label">Total a Pagar</span>
                                    <span class="amount">${"$"}${pedido.total}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="ticket-footer">
                        <p>Este documento constituye el comprobante oficial de su pedido en Nexbit.</p>
                        <p>Para consultas o reclamos, por favor conserve este número de pedido.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        doWebViewPrint(htmlContent)
    }

    private fun doWebViewPrint(htmlContent: String) {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(view)
                printWebView = null
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
        printWebView = webView
    }

    private fun createWebPrintJob(webView: WebView) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter("Pedido_Nexbit")
        val printJobName = getString(R.string.app_name) + " Document"
        printManager.print(printJobName, printAdapter, PrintAttributes.Builder().build())
    }
}
