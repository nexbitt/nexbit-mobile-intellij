package com.example.nexbitmobile.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
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
                        rvPedidos.adapter = MisPedidosAdapter(pedidos, formatter, ::onCancelarClick, ::onDetallleClick)
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
        val sb = StringBuilder()
        sb.append("📦 Pedido #${pedido.id_pedido}\n")
        sb.append("────────────────────────\n")
        sb.append("Estado:    ${pedido.estado}\n")
        sb.append("Fecha:     ${pedido.fecha_pedido?.take(16)?.replace("T", " ") ?: pedido.fecha.take(16).replace("T", " ")}\n")
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
    private val onDetalle: (Pedido) -> Unit
) : RecyclerView.Adapter<MisPedidosAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView      = view.findViewById(R.id.tvPedidoId)
        val tvFecha: TextView   = view.findViewById(R.id.tvPedidoFecha)
        val tvDir: TextView     = view.findViewById(R.id.tvPedidoDireccion)
        val tvTotal: TextView   = view.findViewById(R.id.tvPedidoTotal)
        val tvEstado: TextView  = view.findViewById(R.id.tvPedidoEstado)
        val btnCancelar: Button = view.findViewById(R.id.btnCancelarPedido)
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

        h.itemView.setOnClickListener { onDetalle(p) }
    }
}
