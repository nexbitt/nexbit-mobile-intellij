package com.example.nexbitmobile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import com.example.nexbitmobile.model.PedidoRequest
import com.example.nexbitmobile.model.Usuario
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class PedidosAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PedidoAdminAdapter
    private var allPedidos = listOf<Pedido>()
    private var isClienteView: Boolean = false
    private var userId: Int = 0
    private var ticketWebView: WebView? = null

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos_admin)

        isClienteView = intent.getBooleanExtra("isClienteView", false)
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)

        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        btnAdd.visibility = View.GONE

        if (isClienteView) {
            btnAdd.visibility = View.GONE
            findViewById<TextView>(R.id.tvTitle).text = "Mis Pedidos"
        }

        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrar(s.toString())
            }
        })

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PedidoAdminAdapter(
            pedidos = emptyList(),
            onDetalle = { pedido -> mostrarDetalle(pedido) },
            onDownload = { pedido -> descargarTicket(pedido) },
            onEdit = { pedido -> mostrarDialogoEditar(pedido) }
        )
        recyclerView.adapter = adapter

        loadPedidos()
    }

    private fun filtrar(query: String) {
        if (query.isBlank()) {
            adapter.updateData(allPedidos)
            return
        }
        val q = query.lowercase()
        val filtrados = allPedidos.filter { p ->
            p.id_pedido.toString().contains(q) ||
            (p.usuario_nombre?.lowercase()?.contains(q) == true)
        }
        adapter.updateData(filtrados)
    }

    private fun loadPedidos() {
        ApiClient.instance.getPedidos().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
                    var todos = response.body() ?: emptyList()
                    if (isClienteView) {
                        todos = todos.filter { it.usuario_id == userId }
                    }
                    allPedidos = todos
                    adapter.updateData(todos)
                } else {
                    Toast.makeText(this@PedidosAdminActivity, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ──────────── DETALLE ────────────

    private fun mostrarDetalle(pedido: Pedido) {
        val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val layout = layoutInflater.inflate(R.layout.dialog_pedido_repartidor, null)

        layout.findViewById<TextView>(R.id.tvDialogTitulo).text = "Detalle Pedido #${pedido.id_pedido}"

        val generalInfo = buildString {
            append("Cliente: ${pedido.usuario_nombre ?: "N/A"}\n")
            append("Dirección: ${pedido.direccion_entrega ?: "No especificada"}\n")
            append("Total: ${fmt.format(pedido.total)}\n")
        }
        layout.findViewById<TextView>(R.id.tvDialogGeneral).text = generalInfo

        val sb = StringBuilder()
        val detalles = pedido.detalles
        if (!detalles.isNullOrEmpty()) {
            for (d in detalles) {
                sb.append("${d.cantidad}x ${d.producto_nombre ?: "Producto"} — ${fmt.format(d.subtotal)}\n")
            }
        } else {
            sb.append("Sin productos detallados")
        }
        layout.findViewById<TextView>(R.id.tvDialogProductos).text = sb.toString()

        layout.findViewById<TextView>(R.id.btnCerrarDialog).setOnClickListener {
            (layout.parent as? android.app.Dialog)?.dismiss()
        }

        AlertDialog.Builder(this)
            .setView(layout)
            .show()
    }

    private fun descargarTicket(pedido: Pedido) {
        ApiClient.instance.getPedidoTicket(pedido.id_pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { t -> generarHtmlYPdf(t) }
                        ?: Toast.makeText(this@PedidosAdminActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PedidosAdminActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Log.e("PedidosAdmin", "Ticket error", t)
                Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun subirComprobante(pedido: Pedido) {
        val intent = Intent(this, ConfirmarPedidoActivity::class.java)
        intent.putExtra("pedido_id", pedido.id_pedido)
        startActivity(intent)
    }

    private fun confirmarEliminar(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Pedido #${pedido.id_pedido}")
            .setMessage("¿Estás seguro de eliminar este pedido? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                ApiClient.instance.deletePedido(pedido.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@PedidosAdminActivity, "Pedido eliminado", Toast.LENGTH_SHORT).show()
                            loadPedidos()
                        } else {
                            Toast.makeText(this@PedidosAdminActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun generarHtmlYPdf(pedido: Pedido) {
        val detalles = pedido.detalles ?: emptyList()
        val filas = if (detalles.isNotEmpty()) {
            detalles.joinToString("") { d ->
                """<tr><td style="padding:8px;border-bottom:1px solid #e2e8f0;">${d.producto_nombre}</td><td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:center;">${d.cantidad}</td><td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.precio_unitario}</td><td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.subtotal}</td></tr>"""
            }
        } else {
            "<tr><td colspan='4' style='padding:12px;text-align:center;color:#94a3b8;'>Sin productos detallados</td></tr>"
        }

        val html = """
            <!DOCTYPE html><html><head><meta charset="UTF-8"/><title>Ticket #${pedido.id_pedido}</title>
            <style>*{margin:0;padding:0;box-sizing:border-box}
            body{font-family:sans-serif;background:#e2e8f0;padding:40px 20px;color:#1e293b}
            .ticket{max-width:600px;margin:0 auto;background:#fff;border-radius:4px;padding:32px;border-top:6px solid #0f172a}
            .hdr{display:flex;justify-content:space-between;margin-bottom:24px}
            .brand{font-size:1.75rem;font-weight:700;color:#0f172a}
            table{width:100%;border-collapse:collapse;margin:24px 0}
            th{text-align:left;padding:12px;border-bottom:2px solid #e2e8f0;font-size:.8rem;color:#64748b;text-transform:uppercase}
            td{padding:12px;border-bottom:1px solid #f1f5f9}
            .total{text-align:right;font-size:1.5rem;font-weight:700;margin-top:16px}
            </style></head><body>
            <div class="ticket">
            <div class="hdr"><div class="brand">Nexbit</div><div class="order-id">#${String.format("%06d", pedido.id_pedido)}</div></div>
            <p><strong>Cliente:</strong> ${pedido.usuario_nombre ?: "N/A"}</p>
            <p><strong>Fecha:</strong> ${pedido.fecha ?: pedido.fecha_pedido ?: "N/A"}</p>
            <p><strong>Estado:</strong> ${pedido.estado}</p>
            <table><thead><tr><th>Producto</th><th>Cant</th><th>Precio</th><th>Subtotal</th></tr></thead><tbody>$filas</tbody></table>
            <div class="total">Total: $${pedido.total}</div>
            </div></body></html>
        """.trimIndent()

        doImpresion(html)
    }

    private fun doImpresion(html: String) {
        val wv = WebView(this)
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                (getSystemService(Context.PRINT_SERVICE) as PrintManager)
                    .print("Nexbit Ticket", view.createPrintDocumentAdapter("Pedido"), PrintAttributes.Builder().build())
                ticketWebView = null
            }
        }
        wv.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
        ticketWebView = wv
    }

    // ──────────── EDITAR PEDIDO ────────────
    private fun mostrarDialogoEditar(pedido: Pedido) {
        val view = layoutInflater.inflate(R.layout.dialog_editar_pedido, null)
        val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // Título
        view.findViewById<TextView>(R.id.tvDialogTitulo).text = "Editar Pedido #${pedido.id_pedido}"

        // Total actual
        view.findViewById<TextView>(R.id.tvTotalActual).text = "Total actual: ${fmt.format(pedido.total)}"

        // Cargar estados válidos según FSM del backend
        val estadosDisponibles = obtenerEstadosPermitidos(pedido.estado)
        val spinnerEstado = view.findViewById<Spinner>(R.id.spinnerEstado)
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estadosDisponibles)
        spinnerEstado.adapter = adapterEstado
        // Seleccionar estado actual
        val currentIndex = estadosDisponibles.indexOf(pedido.estado)
        if (currentIndex >= 0) spinnerEstado.setSelection(currentIndex)

        // Dirección actual
        val etDireccion = view.findViewById<TextInputEditText>(R.id.etDireccionEntrega)
        etDireccion.setText(pedido.direccion_entrega ?: "")

        // Notas actuales
        val etNotas = view.findViewById<TextInputEditText>(R.id.etNotasEntrega)
        etNotas.setText(pedido.notas_entrega ?: "")

        // Total editable
        val etTotal = view.findViewById<TextInputEditText>(R.id.etTotal)
        etTotal.setText(pedido.total.toInt().toString())

        // Spinner Repartidores - cargar desde API
        val spinnerRepartidor = view.findViewById<Spinner>(R.id.spinnerRepartidor)
        val tvRepartidorHint = view.findViewById<TextView>(R.id.tvRepartidorHint)
        cargarRepartidores(spinnerRepartidor, tvRepartidorHint)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .show()

        // Botón Cancelar
        view.findViewById<TextView>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        // Botón Guardar
        view.findViewById<TextView>(R.id.btnGuardar).setOnClickListener {
            val nuevoEstado = spinnerEstado.selectedItem.toString()
            val direccion = etDireccion.text.toString().trim().ifEmpty { null }
            val notas = etNotas.text.toString().trim().ifEmpty { null }
            val totalStr = etTotal.text.toString().trim()
            val total = if (totalStr.isNotEmpty()) totalStr.toDoubleOrNull() else null

            // Obtener repartidor_id seleccionado
            val repartidorId: Int? = (spinnerRepartidor.tag as? List<Usuario>)?.getOrNull(spinnerRepartidor.selectedItemPosition)?.id_usuario

            val request = PedidoRequest(
                usuario_id = pedido.usuario_id,
                total = total ?: pedido.total,
                estado = nuevoEstado,
                direccion_entrega = direccion,
                notas_entrega = notas,
                repartidor_id = repartidorId
            )

            ApiClient.instance.updatePedido(pedido.id_pedido, request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PedidosAdminActivity, "Pedido actualizado", Toast.LENGTH_SHORT).show()
                        loadPedidos()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@PedidosAdminActivity, "Error al actualizar (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun obtenerEstadosPermitidos(estadoActual: String): List<String> {
        return when (estadoActual) {
            "PENDIENTE" -> listOf("PENDIENTE", "CONFIRMADO")
            "EN_REVISION" -> listOf("EN_REVISION", "APROBADO", "RECHAZADO")
            "APROBADO" -> listOf("APROBADO", "ASIGNADO")
            "ASIGNADO" -> listOf("ASIGNADO", "EN_CAMINO")
            "EN_CAMINO" -> listOf("EN_CAMINO", "ENTREGADO", "CANCELADO")
            "RECHAZADO" -> listOf("RECHAZADO", "PENDIENTE") // Puede volver a pendiente para re-subir comprobante
            else -> listOf(estadoActual) // ENTREGADO, CANCELADO no permiten cambios
        }
    }

    private fun cargarRepartidores(spinner: Spinner, hintView: TextView) {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    val usuarios = response.body() ?: emptyList()
                    val repartidores = usuarios.filter { it.rol_id == 4 } // rol_id 4 = Repartidor
                    if (repartidores.isNotEmpty()) {
                        val nombres = repartidores.map { "${it.nombre} (${it.email})" }
                        val adapter = ArrayAdapter(this@PedidosAdminActivity, android.R.layout.simple_spinner_dropdown_item, nombres)
                        spinner.adapter = adapter
                        hintView.visibility = View.GONE
                        // Guardamos los IDs en el tag para recuperarlos después
                        spinner.tag = repartidores
                    } else {
                        hintView.text = "No hay repartidores disponibles"
                    }
                }
            }
            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                hintView.text = "Error al cargar repartidores"
            }
        })
    }
}
