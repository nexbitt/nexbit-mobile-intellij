package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Pedido

class EntregaAdapter(
    private var entregas: List<Pedido>,
    private val onTicket: (Pedido) -> Unit
) : RecyclerView.Adapter<EntregaAdapter.EntregaViewHolder>() {

    class EntregaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val btnTicket: ImageButton = view.findViewById(R.id.btnTicket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntregaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entrega, parent, false)
        return EntregaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntregaViewHolder, position: Int) {
        val pedido = entregas[position]
        holder.tvId.text = "Entrega #${String.format("%06d", pedido.id_pedido)}"
        holder.tvCliente.text = "Cliente: ${pedido.usuario_nombre ?: pedido.usuario_id}"
        holder.tvTotal.text = "Total: $${pedido.total}"
        holder.tvFecha.text = "Fecha: ${pedido.fecha}"
        holder.tvEstado.text = pedido.estado

        val (textColor, bgColor) = when (pedido.estado) {
            "ENTREGADO" -> "#2f9e44" to "#d1fae5"
            "EN_TRANSITO" -> "#1e7e34" to "#bfdbfe"
            "PENDIENTE" -> "#f59e0b" to "#fef3c7"
            else -> "#6b7280" to "#e5e7eb"
        }
        holder.tvEstado.setTextColor(android.graphics.Color.parseColor(textColor))
        holder.tvEstado.setBackgroundColor(android.graphics.Color.parseColor(bgColor))
        holder.btnTicket.setOnClickListener { onTicket(pedido) }
    }

    override fun getItemCount() = entregas.size

    fun updateData(newEntregas: List<Pedido>) {
        entregas = newEntregas
        notifyDataSetChanged()
    }
}