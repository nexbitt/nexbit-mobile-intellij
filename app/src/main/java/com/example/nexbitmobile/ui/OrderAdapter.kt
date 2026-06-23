package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Pedido

class OrderAdapter(
    private var pedidos: List<Pedido>,
    private val onItemClick: (Pedido) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProduct: ImageView = view.findViewById(R.id.ivOrderProduct)
        val tvProductName: TextView = view.findViewById(R.id.tvOrderProductName)
        val tvTime: TextView = view.findViewById(R.id.tvOrderTime)
        val tvQty: TextView = view.findViewById(R.id.tvOrderQty)
        val tvBuyer: TextView = view.findViewById(R.id.tvOrderBuyer)
        val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pedido = pedidos[position]

        holder.tvProductName.text = "Pedido #${pedido.id_pedido}"
        holder.tvTime.text = pedido.fecha_pedido ?: pedido.fecha ?: ""
        holder.tvQty.text = "Total: $${pedido.total}"
        holder.tvBuyer.text = pedido.usuario_nombre ?: "Cliente"
        holder.tvOrderId.text = "#DXZ_${pedido.id_pedido.toString().padStart(5, '0')}"

        val status = pedido.estado.lowercase()
        val (statusText, bgRes, textColorRes) = when {
            status.contains("pend") -> Triple("Pendiente", R.drawable.bg_status_new, R.color.info)
            status.contains("revision") -> Triple("En Revisión", R.drawable.bg_status_revision, R.color.warning)
            status.contains("confirm") -> Triple("Confirmado", R.drawable.bg_status_confirmed, R.color.success)
            status.contains("envia") -> Triple("Enviado", R.drawable.bg_status_shipped, R.color.crud_primary)
            status.contains("entrega") -> Triple("Entregado", R.drawable.bg_status_delivered, R.color.text_secondary)
            status.contains("cancel") -> Triple("Cancelado", R.drawable.bg_status_cancelled, R.color.error_text)
            else -> Triple(pedido.estado, R.drawable.bg_chip, R.color.text_secondary)
        }

        holder.tvStatus.text = statusText
        holder.tvStatus.setBackgroundResource(bgRes)
        holder.tvStatus.setTextColor(
            ContextCompat.getColor(holder.itemView.context, textColorRes)
        )

        Glide.with(holder.itemView.context)
            .load(R.drawable.ic_placeholder)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivProduct)

        holder.itemView.setOnClickListener { onItemClick(pedido) }
    }

    override fun getItemCount() = pedidos.size

    fun updateList(newList: List<Pedido>) {
        pedidos = newList
        notifyDataSetChanged()
    }
}
