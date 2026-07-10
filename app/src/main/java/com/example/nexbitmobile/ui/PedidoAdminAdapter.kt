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
import java.util.Locale

class PedidoAdminAdapter(
    private var pedidos: List<Pedido>,
    private val onDetalle: (Pedido) -> Unit,
    private val onDownload: (Pedido) -> Unit,
    private val onEdit: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdminAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val btnDetalle: View = view.findViewById(R.id.btnDetalle)
        val btnDownload: View = view.findViewById(R.id.btnDownload)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido_admin, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = pedidos.size

    override fun onBindViewHolder(h: ViewHolder, pos: Int) {
        val p = pedidos[pos]

        h.tvCliente.text = p.usuario_nombre ?: "Sin nombre"
        h.tvTotal.text = formatter.format(p.total)

        val (badgeBg, badgeTextColor, badgeLabel) = when {
            p.estado.contains("ENTREG", true) -> Triple(
                R.drawable.bg_badge_ios_delivered, "#34C759", "ENTREGADO"
            )
            p.estado.contains("CONFIRM", true) -> Triple(
                R.drawable.bg_badge_ios_delivered, "#34C759", "CONFIRMADO"
            )
            p.estado.contains("CANCEL", true) -> Triple(
                R.drawable.bg_badge_ios_cancelled, "#FF3B30", "CANCELADO"
            )
            p.estado.contains("PENDIENTE", true) -> Triple(
                R.drawable.bg_badge_ios_pending, "#FF9500", "PENDIENTE DE PAGO"
            )
            p.estado.contains("RUTA", true) || p.estado.contains("CAMINO", true) -> Triple(
                R.drawable.bg_badge_ios_pending, "#FF9500", "EN CAMINO"
            )
            else -> Triple(R.drawable.bg_badge_ios_pending, "#FF9500", p.estado)
        }

        h.tvEstado.background = ContextCompat.getDrawable(h.itemView.context, badgeBg)
        h.tvEstado.setTextColor(Color.parseColor(badgeTextColor))
        h.tvEstado.text = badgeLabel

        h.itemView.setOnClickListener { onDetalle(p) }
        h.btnDetalle.setOnClickListener { onDetalle(p) }
        h.btnDownload.setOnClickListener { onDownload(p) }
        h.btnEdit.setOnClickListener { onEdit(p) }
    }

    fun updateData(newPedidos: List<Pedido>) {
        this.pedidos = newPedidos
        notifyDataSetChanged()
    }
}
