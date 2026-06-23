package com.example.nexbitmobile.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Pedido
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PedidoAdminAdapter(
    private var pedidos: List<Pedido>,
    private val onDetalle: (Pedido) -> Unit,
    private val onEdit: ((Pedido) -> Unit)? = null,
    private val onDelete: ((Pedido) -> Unit)? = null
) : RecyclerView.Adapter<PedidoAdminAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val btnDetalle: FrameLayout = view.findViewById(R.id.btnDetalle)
        val btnEdit: FrameLayout = view.findViewById(R.id.btnEdit)
        val btnDelete: FrameLayout = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido_admin, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = pedidos.size

    override fun onBindViewHolder(h: ViewHolder, pos: Int) {
        val p = pedidos[pos]

        h.tvId.text = "N\u00B0 ${p.id_pedido}"
        h.tvTotal.text = formatter.format(p.total)
        h.tvFecha.text = formatearFecha(p.fecha ?: p.fecha_pedido)
        h.tvCliente.text = p.usuario_nombre ?: "Sin nombre"

        val (badgeBg, badgeTextColor, badgeLabel) = when {
            p.estado.contains("ENTREG", true) -> Triple(
                R.drawable.bg_badge_ios_delivered, "#34C759", "ENTREGADO"
            )
            p.estado.contains("PENDIENTE", true) -> Triple(
                R.drawable.bg_badge_ios_pending, "#FF9500", "PENDIENTE DE PAGO"
            )
            p.estado.contains("CANCEL", true) -> Triple(
                R.drawable.bg_badge_ios_cancelled, "#FF3B30", "CANCELADO"
            )
            p.estado.contains("CONFIRM", true) -> Triple(
                R.drawable.bg_badge_ios_delivered, "#34C759", "CONFIRMADO"
            )
            p.estado.contains("RUTA", true) || p.estado.contains("CAMINO", true) -> Triple(
                R.drawable.bg_badge_ios_pending, "#FF9500", "EN CAMINO"
            )
            else -> Triple(R.drawable.bg_badge_ios_pending, "#FF9500", p.estado)
        }

        h.tvEstado.background = ContextCompat.getDrawable(h.itemView.context, badgeBg)
        h.tvEstado.setTextColor(Color.parseColor(badgeTextColor))
        h.tvEstado.text = badgeLabel

        h.btnDelete.visibility = if (onDelete != null) View.VISIBLE else View.GONE

        h.itemView.setOnClickListener { onDetalle(p) }
        h.btnDetalle.setOnClickListener { onDetalle(p) }
        h.btnEdit.setOnClickListener { onEdit?.invoke(p) }
        h.btnDelete.setOnClickListener { onDelete?.invoke(p) }
    }

    private fun formatearFecha(fechaRaw: String?): String {
        if (fechaRaw.isNullOrBlank()) return "Fecha no disponible"
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = isoFormat.parse(fechaRaw.take(19))
            if (date != null) {
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "CO"))
                outputFormat.format(date)
            } else "Fecha no disponible"
        } catch (_: Exception) {
            try {
                val altFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                val date = altFormat.parse(fechaRaw.take(16))
                if (date != null) {
                    val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "CO"))
                    outputFormat.format(date)
                } else fechaRaw.take(16)
            } catch (_: Exception) {
                fechaRaw.take(16)
            }
        }
    }

    fun updateData(newPedidos: List<Pedido>) {
        this.pedidos = newPedidos
        notifyDataSetChanged()
    }
}
