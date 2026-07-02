package com.example.nexbitmobile.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class MisPedidosActivity : AppCompatActivity() {

    private lateinit var rvPedidos: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmpty: LinearLayout
    private var userId = 0
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mis_pedidos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)

        rvPedidos   = findViewById(R.id.rvMisPedidos)
        progressBar = findViewById(R.id.progressBar)
        llEmpty     = findViewById(R.id.llEmpty)

        rvPedidos.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnGoToCatalog).setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
            finish()
        }

        cargarPedidos()
    }

    private fun abrirChat(pedidoId: Int) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("pedidoId", pedidoId)
        }
        startActivity(intent)
    }

    private fun cargarPedidos() {
        if (userId == 0) { showEmpty(); return }
        progressBar.visibility = View.VISIBLE
        llEmpty.visibility = View.GONE

        ApiClient.instance.getMisPedidos(userId).enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val pedidos = response.body() ?: emptyList()
                    if (pedidos.isEmpty()) showEmpty()
                    else {
                        llEmpty.visibility = View.GONE
                        rvPedidos.visibility = View.VISIBLE
                        rvPedidos.adapter = MisPedidosAdapter(pedidos, formatter, ::onCancelarClick, ::onDetallleClick, ::abrirChat, ::onTicketClick)
                    }
                } else {
                    Toast.makeText(this@MisPedidosActivity, "Error al cargar pedidos (${response.code()})", Toast.LENGTH_SHORT).show()
                    showEmpty()
                }
            }
            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MisPedidosActivity, "Sin conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                showEmpty()
            }
        })
    }

    private fun showEmpty() {
        rvPedidos.visibility = View.GONE
        llEmpty.visibility   = View.VISIBLE
    }

    private fun onCancelarClick(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Pedido #${pedido.id_pedido}")
            .setMessage("¿Estás seguro de que deseas cancelar este pedido?\nTotal: ${formatter.format(pedido.total)}")
            .setPositiveButton("Sí, cancelar") { _, _ -> cancelarPedido(pedido.id_pedido) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelarPedido(pedidoId: Int) {
        ApiClient.instance.cancelarPedido(pedidoId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MisPedidosActivity, "Pedido cancelado", Toast.LENGTH_SHORT).show()
                    cargarPedidos()
                } else {
                    val msg = response.errorBody()?.string() ?: "Error al cancelar"
                    Toast.makeText(this@MisPedidosActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MisPedidosActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var printWebView: WebView? = null

    private fun onTicketClick(pedido: Pedido) {
        ApiClient.instance.getPedidoTicket(pedido.id_pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { pedidoTicket ->
                        generarHtmlYPdf(pedidoTicket)
                    } ?: run {
                        Toast.makeText(this@MisPedidosActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MisPedidosActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Log.e("MisPedidos", "Ticket download failed", t)
                Toast.makeText(this@MisPedidosActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
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
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.precio_unitario}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.subtotal}</td>
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
                    @media print { body { background: #fff; padding: 0; } .ticket { box-shadow: none; border: none; border-top: 6px solid #0f172a; } }
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
                                <span>${pedido.fecha ?: pedido.fecha_pedido ?: "N/A"}</span>
                            </div>
                            <div class="info-item">
                                <label>Estado del Pedido</label>
                                <div><span class="badge badge-${pedido.estado?.lowercase() ?: "pendiente"}">${pedido.estado ?: "PENDIENTE"}</span></div>
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
                                    <span class="amount">$${pedido.total}</span>
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
        val printJobName = getString(R.string.app_name) + " - Ticket"
        printManager.print(printJobName, printAdapter, PrintAttributes.Builder().build())
    }

    private fun onDetallleClick(pedido: Pedido) {
        val sb = StringBuilder()
        sb.append("📦 Pedido #${pedido.id_pedido}\n")
        sb.append("────────────────────────\n")
        sb.append("Estado:    ${pedido.estado}\n")
        sb.append("Fecha:     ${pedido.fecha_pedido?.take(16)?.replace("T", " ") ?: pedido.fecha?.take(16)?.replace("T", " ") ?: "N/A"}\n")
        sb.append("Dirección: ${pedido.direccion_entrega ?: "No especificada"}\n")
        sb.append("Total:     ${formatter.format(pedido.total)}\n")

        val detalles = pedido.detalles
        if (!detalles.isNullOrEmpty()) {
            sb.append("\n🛍 Productos:\n")
            for (d in detalles) {
                val nombre = d.producto_nombre ?: "Producto"
                sb.append("  • ${d.cantidad}x $nombre → ${formatter.format(d.subtotal)}\n")
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Detalle del Pedido")
            .setMessage(sb.toString())
            .setPositiveButton("Cerrar", null)
            .create()

        dialog.setOnShowListener {
            val aceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            if (pedido.estado == "PENDIENTE") {
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancelar Pedido") { _, _ ->
                    onCancelarClick(pedido)
                }
            }

            if (pedido.estado == "PENDIENTE" || pedido.estado == "CONFIRMADO") {
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Subir Comprobante") { _, _ ->
                    val intent = Intent(this@MisPedidosActivity, ConfirmarPedidoActivity::class.java)
                    intent.putExtra("pedido_id", pedido.id_pedido)
                    startActivity(intent)
                }
            }

            if (pedido.estado != "CANCELADO" && pedido.estado != "ENTREGADO") {
                val chatLabel = if (pedido.estado == "PENDIENTE") "Chat" else "Chat"
                dialog.setButton(android.content.DialogInterface.BUTTON3, chatLabel) { _, _ ->
                    val intent = Intent(this@MisPedidosActivity, ChatActivity::class.java)
                    intent.putExtra("pedido_id", pedido.id_pedido)
                    startActivity(intent)
                }
            }
        }

        dialog.show()
    }
}

// ─── Adapter interno ──────────────────────────────────────────────────────────

class MisPedidosAdapter(
    private val pedidos: List<Pedido>,
    private val fmt: NumberFormat,
    private val onCancelar: (Pedido) -> Unit,
    private val onDetalle: (Pedido) -> Unit,
    private val onChat: (Int) -> Unit,
    private val onTicket: (Pedido) -> Unit
) : RecyclerView.Adapter<MisPedidosAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView      = view.findViewById(R.id.tvIdPedido)
        val tvFecha: TextView   = view.findViewById(R.id.tvFechaPedido)
        val tvDir: TextView     = view.findViewById(R.id.tvDireccionPedido)
        val tvTotal: TextView   = view.findViewById(R.id.tvTotalPedido)
        val tvEstadoBadge: TextView = view.findViewById(R.id.tvEstadoBadge)
        val viewEstadoColor: View   = view.findViewById(R.id.viewEstadoColor)
        val btnVerDetalles: Button  = view.findViewById(R.id.btnVerDetalles)
        val btnTicket: Button       = view.findViewById(R.id.btnTicket)
        val btnBorrar: Button       = view.findViewById(R.id.btnBorrar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false))

    override fun getItemCount() = pedidos.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val p = pedidos[pos]
        h.tvId.text     = "Pedido #${p.id_pedido}"
        h.tvFecha.text  = "Fecha: ${p.fecha_pedido?.take(16)?.replace("T", " ") ?: p.fecha?.take(16)?.replace("T", " ") ?: "N/A"}"
        h.tvDir.text    = "Dirección: ${p.direccion_entrega ?: "Sin dirección"}"
        h.tvTotal.text  = fmt.format(p.total)

        val badgeBg = when (p.estado) {
            "PENDIENTE" -> R.drawable.bg_badge_info
            "CONFIRMADO" -> R.drawable.bg_badge_confirmed
            "ASIGNADO" -> R.drawable.bg_badge_assigned
            "EN_CAMINO" -> R.drawable.bg_badge_info
            "ENTREGADO" -> R.drawable.bg_badge_success
            "CANCELADO" -> R.drawable.bg_badge_cancelled
            else -> R.drawable.bg_chip
        }
        val colorStr = when (p.estado) {
            "PENDIENTE" -> "#F59E0B"
            "CONFIRMADO" -> "#3B82F6"
            "ASIGNADO" -> "#3F51B5"
            "EN_CAMINO" -> "#F97316"
            "ENTREGADO" -> "#2E7D32"
            "CANCELADO" -> "#EF4444"
            else -> "#64748B"
        }
        h.tvEstadoBadge.text = when (p.estado) {
            "EN_CAMINO" -> "EN CAMINO"
            else -> p.estado
        }
        h.tvEstadoBadge.setBackgroundResource(badgeBg)
        h.tvEstadoBadge.setTextColor(Color.parseColor(colorStr))
        h.viewEstadoColor.setBackgroundColor(Color.parseColor(colorStr))

        h.btnTicket.visibility = View.VISIBLE
        h.btnTicket.setOnClickListener { onTicket(p) }

        h.btnVerDetalles.visibility = View.VISIBLE
        h.btnVerDetalles.setOnClickListener { onDetalle(p) }

        if (p.estado == "PENDIENTE") {
            h.btnBorrar.visibility = View.VISIBLE
            h.btnBorrar.text = "Cancelar"
            h.btnBorrar.setOnClickListener { onCancelar(p) }
        } else {
            h.btnBorrar.visibility = View.GONE
        }

        h.itemView.setOnClickListener { onDetalle(p) }
    }
}
