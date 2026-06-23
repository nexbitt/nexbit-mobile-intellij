package com.example.nexbitmobile.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.PedidoRepartidor

class EntregaAdapter(
    private var entregas: List<PedidoRepartidor>,
    private val onVerMapaClick: (PedidoRepartidor) -> Unit,
    private val onAccionClick: (PedidoRepartidor) -> Unit,
    private val onItemClick: (PedidoRepartidor) -> Unit,
    private val onReportarProblemaClick: ((PedidoRepartidor) -> Unit)? = null
) : RecyclerView.Adapter<EntregaAdapter.EntregaViewHolder>() {

    class EntregaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIdEntrega: TextView = view.findViewById(R.id.tvIdEntrega)
        val tvClienteEntrega: TextView = view.findViewById(R.id.tvClienteEntrega)
        val tvDireccionEntrega: TextView = view.findViewById(R.id.tvDireccionEntrega)
        val tvTotalEntrega: TextView = view.findViewById(R.id.tvTotalEntrega)
        val tvEstadoEntrega: TextView = view.findViewById(R.id.tvEstadoEntrega)
        val btnVerMapa: ImageButton = view.findViewById(R.id.btnVerMapa)
        val btnAccion: TextView = view.findViewById(R.id.btnAccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntregaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entrega, parent, false)
        return EntregaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntregaViewHolder, position: Int) {
        val entrega = entregas[position]

        holder.tvIdEntrega.text = "Pedido #${entrega.id_pedido}"
        holder.tvClienteEntrega.text = "Destino: ${entrega.cliente?.nombre ?: "Cliente"}"
        holder.tvDireccionEntrega.text = "Dirección: ${entrega.direccion_entrega ?: "No especificada"}"
        holder.tvTotalEntrega.text = "Total: $${String.format("%,.2f", entrega.total)}"
        holder.tvEstadoEntrega.text = entrega.estado

        when (entrega.estado) {
            "PENDIENTE", "CONFIRMADO", "APROBADO" -> {
                holder.tvEstadoEntrega.text = "DISPONIBLE"
                holder.tvEstadoEntrega.setTextColor(Color.parseColor("#8b5cf6"))
                holder.btnAccion.visibility = View.VISIBLE
                holder.btnAccion.text = "Tomar Pedido"
                holder.btnAccion.setBackgroundColor(Color.parseColor("#111827"))
            }
            "ASIGNADO" -> {
                holder.tvEstadoEntrega.text = "ASIGNADO"
                holder.tvEstadoEntrega.setTextColor(Color.parseColor("#3b82f6"))
                holder.btnAccion.visibility = View.VISIBLE
                holder.btnAccion.text = "Iniciar Ruta"
                holder.btnAccion.setBackgroundColor(Color.parseColor("#111827"))
            }
            "EN_CAMINO" -> {
                holder.tvEstadoEntrega.text = "EN CAMINO"
                holder.tvEstadoEntrega.setTextColor(Color.parseColor("#f59e0b"))
                holder.btnAccion.visibility = View.VISIBLE
                holder.btnAccion.text = "Finalizar Entrega"
                holder.btnAccion.setBackgroundColor(Color.parseColor("#10b981"))
            }
            "ENTREGADO" -> {
                holder.tvEstadoEntrega.text = "ENTREGADO"
                holder.tvEstadoEntrega.setTextColor(Color.parseColor("#10b981"))
                holder.btnAccion.visibility = View.GONE
            }
            "CANCELADO" -> {
                holder.tvEstadoEntrega.text = "CANCELADO"
                holder.tvEstadoEntrega.setTextColor(Color.parseColor("#ef4444"))
                holder.btnAccion.visibility = View.GONE
            }
            else -> {
                holder.tvEstadoEntrega.setTextColor(Color.parseColor("#64748b"))
                holder.btnAccion.visibility = View.GONE
            }
        }

        holder.btnVerMapa.setOnClickListener { onVerMapaClick(entrega) }
        holder.btnAccion.setOnClickListener { onAccionClick(entrega) }
        holder.itemView.setOnClickListener { onItemClick(entrega) }
    }

    override fun getItemCount(): Int = entregas.size

    fun updateData(newEntregas: List<PedidoRepartidor>) {
        this.entregas = newEntregas
        notifyDataSetChanged()
    }
}
