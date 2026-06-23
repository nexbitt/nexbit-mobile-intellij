package com.example.nexbitmobile.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class MisPedidosActivity : AppCompatActivity() {

    private lateinit var rvPedidos: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmpty: LinearLayout
    private var userId = 0
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private var currentImageUri: Uri? = null
    private var printWebView: WebView? = null

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { currentImageUri = it }
    }

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
                        rvPedidos.adapter = MisPedidosAdapter(
                            pedidos, formatter,
                            ::onCancelarClick, ::onDetallleClick,
                            ::onSubirComprobanteClick, ::onReintentarClick, ::descargarTicket
                        )
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

    private fun onDetallleClick(pedido: Pedido) {
        val scrollView = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        scrollView.addView(container)

        container.addView(TextView(this).apply {
            text = "📦 Pedido #${pedido.id_pedido}"
            textSize = 18f; setTypeface(null, android.graphics.Typeface.BOLD)
        })
        container.addView(TextView(this).apply {
            text = "Estado: ${pedido.estado}"
            textSize = 14f; setPadding(0, 8, 0, 4)
        })
        container.addView(TextView(this).apply {
            text = "Fecha: ${pedido.fecha_pedido?.take(16)?.replace("T", " ") ?: pedido.fecha.take(16).replace("T", " ")}"
            textSize = 14f
        })
        container.addView(TextView(this).apply {
            text = "Dirección: ${pedido.direccion_entrega ?: "No especificada"}"
        })
        container.addView(TextView(this).apply {
            text = "Total: ${formatter.format(pedido.total)}"
            textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 8, 0, 8)
        })

        val detalles = pedido.detalles
        if (!detalles.isNullOrEmpty()) {
            container.addView(TextView(this).apply {
                text = "Productos:"
                setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 8, 0, 4)
            })
            for (d in detalles) {
                container.addView(TextView(this).apply {
                    text = "  • ${d.cantidad}x ${d.producto_nombre ?: "Producto"} → ${formatter.format(d.subtotal)}"
                })
            }
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Detalle del Pedido")
            .setView(scrollView)
            .setPositiveButton("Cerrar", null)

        if (pedido.estado == "PENDIENTE") {
            builder.setNeutralButton("Cancelar Pedido") { _, _ -> onCancelarClick(pedido) }
        }
        builder.show()
    }

    private fun onSubirComprobanteClick(pedido: Pedido) {
        selectImageLauncher.launch("image/*")
        currentImageUri?.let { uri ->
            subirComprobante(pedido.id_pedido, uri)
        }
    }

    private fun onReintentarClick(pedido: Pedido) {
        onSubirComprobanteClick(pedido)
    }

    private fun subirComprobante(pedidoId: Int, uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File.createTempFile("comprobante_", ".jpg", cacheDir)
            inputStream?.use { it.copyTo(FileOutputStream(file)) }
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val imagePart = MultipartBody.Part.createFormData(
                "comprobante", file.name, file.asRequestBody(mimeType.toMediaTypeOrNull())
            )

            Toast.makeText(this, "Enviando comprobante...", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "Comprobante enviado con éxito", Toast.LENGTH_SHORT).show()
            cargarPedidos()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun descargarTicket(pedido: Pedido) {
        generarHtmlYPdf(pedido)
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
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: sans-serif; background: #e2e8f0; padding: 40px 20px; color: #1e293b; }
                    .ticket { max-width: 600px; margin: 0 auto; background: #fff; border-radius: 4px; overflow: hidden; border-top: 6px solid #0f172a; }
                    .ticket-header { padding: 32px 32px 16px; display: flex; justify-content: space-between; border-bottom: 1px solid #f1f5f9; }
                    .ticket-header .brand { font-size: 1.75rem; font-weight: 700; color: #0f172a; }
                    .ticket-header .doc-type { font-size: 0.85rem; color: #64748b; text-transform: uppercase; }
                    .ticket-header .order-id { text-align: right; }
                    .ticket-header .order-id-value { font-size: 1.25rem; font-weight: 700; color: #0f172a; }
                    .ticket-body { padding: 32px; }
                    .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 32px; background: #f8fafc; padding: 20px; border-radius: 6px; }
                    .info-item label { display: block; font-size: 0.75rem; text-transform: uppercase; color: #64748b; font-weight: 600; margin-bottom: 6px; }
                    .info-item span { font-size: 0.95rem; font-weight: 500; color: #0f172a; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 32px; }
                    thead th { border-bottom: 2px solid #e2e8f0; padding: 12px; font-size: 0.8rem; text-transform: uppercase; color: #64748b; }
                    tbody td { padding: 14px 12px; border-bottom: 1px solid #f1f5f9; }
                    .total-row.final { border-top: 2px solid #0f172a; padding-top: 16px; display: flex; justify-content: space-between; }
                    .total-row.final .label { font-size: 1.1rem; font-weight: 700; color: #0f172a; }
                    .total-row.final .amount { font-size: 1.5rem; font-weight: 700; color: #0f172a; }
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
                            <div class="order-id-value">${String.format("%06d", pedido.id_pedido)}</div>
                        </div>
                    </div>
                    <div class="ticket-body">
                        <div class="info-grid">
                            <div class="info-item"><label>Cliente</label><span>${pedido.usuario_nombre ?: "N/A"}</span></div>
                            <div class="info-item"><label>Estado</label><span>${pedido.estado}</span></div>
                        </div>
                        <table>
                            <thead><tr><th>Producto</th><th>Cant</th><th>P.Unit</th><th>Subtotal</th></tr></thead>
                            <tbody>$filasProductos</tbody>
                        </table>
                        <div class="total-row final">
                            <span class="label">Total</span>
                            <span class="amount">${"$"}${pedido.total}</span>
                        </div>
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
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter("Pedido_Nexbit")
        val printJobName = getString(R.string.app_name) + " Document"
        printManager.print(printJobName, printAdapter, PrintAttributes.Builder().build())
    }
}

// ─── Adapter interno ──────────────────────────────────────────────────────────

class MisPedidosAdapter(
    private val pedidos: List<Pedido>,
    private val fmt: NumberFormat,
    private val onCancelar: (Pedido) -> Unit,
    private val onDetalle: (Pedido) -> Unit,
    private val onSubirComprobante: (Pedido) -> Unit,
    private val onReintentar: (Pedido) -> Unit,
    private val onDescargarTicket: (Pedido) -> Unit
) : RecyclerView.Adapter<MisPedidosAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView      = view.findViewById(R.id.tvPedidoId)
        val tvFecha: TextView   = view.findViewById(R.id.tvPedidoFecha)
        val tvDir: TextView     = view.findViewById(R.id.tvPedidoDireccion)
        val tvTotal: TextView   = view.findViewById(R.id.tvPedidoTotal)
        val tvEstado: TextView  = view.findViewById(R.id.tvPedidoEstado)
        val btnCancelar: Button = view.findViewById(R.id.btnCancelarPedido)
        val btnSubirComprobante: Button = view.findViewById(R.id.btnSubirComprobante)
        val btnReintentar: Button = view.findViewById(R.id.btnReintentar)
        val btnDescargarTicket: Button = view.findViewById(R.id.btnDescargarTicket)
        val ivProductoImage: ImageView = view.findViewById(R.id.ivProductoImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_pedido_cliente, parent, false))

    override fun getItemCount() = pedidos.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val p = pedidos[pos]
        h.tvId.text     = "Pedido #${p.id_pedido}"
        h.tvFecha.text  = p.fecha_pedido?.take(16)?.replace("T", " ") ?: p.fecha.take(16).replace("T", " ")
        h.tvDir.text    = "📍 ${p.direccion_entrega ?: "Sin dirección"}"
        h.tvTotal.text  = fmt.format(p.total)

        val (label, color) = when (p.estado) {
            "PENDIENTE"  -> "⏳ Pendiente"  to Color.parseColor("#f59e0b")
            "CONFIRMADO" -> "✅ Confirmado" to Color.parseColor("#3b82f6")
            "ASIGNADO"   -> "🚴 Asignado"   to Color.parseColor("#8b5cf6")
            "EN_CAMINO"  -> "🚚 En camino"  to Color.parseColor("#f97316")
            "ENTREGADO"  -> "✅ Entregado"  to Color.parseColor("#10b981")
            "CANCELADO"  -> "❌ Cancelado"  to Color.parseColor("#ef4444")
            else         -> p.estado        to Color.parseColor("#64748b")
        }
        h.tvEstado.text = label
        h.tvEstado.setTextColor(color)

        // Solo PENDIENTE puede cancelarse
        if (p.estado == "PENDIENTE") {
            h.btnCancelar.visibility = View.VISIBLE
            h.btnCancelar.setOnClickListener { onCancelar(p) }
        } else {
            h.btnCancelar.visibility = View.GONE
        }

        // Subir comprobante (solo PENDIENTE)
        h.btnSubirComprobante.visibility = if (p.estado == "PENDIENTE") View.VISIBLE else View.GONE
        h.btnSubirComprobante.setOnClickListener { onSubirComprobante(p) }

        // Reintentar (solo RECHAZADO)
        h.btnReintentar.visibility = if (p.estado == "RECHAZADO") View.VISIBLE else View.GONE
        h.btnReintentar.setOnClickListener { onReintentar(p) }

        // Ticket (siempre visible)
        h.btnDescargarTicket.setOnClickListener { onDescargarTicket(p) }

        // Product image - load first product image if available
        val detalles = p.detalles
        if (!detalles.isNullOrEmpty()) {
            val imgUrl = detalles.firstOrNull()?.imagen_url
            if (!imgUrl.isNullOrEmpty()) {
                Glide.with(h.itemView.context)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(h.ivProductoImage)
            }
        }

        h.itemView.setOnClickListener { onDetalle(p) }
    }
}
