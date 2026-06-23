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
import com.example.nexbitmobile.model.Producto
import com.example.nexbitmobile.model.Usuario
import com.google.android.material.bottomsheet.BottomSheetDialog
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
            onEdit = null,
            onDelete = { pedido -> confirmarEliminar(pedido) }
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
}
