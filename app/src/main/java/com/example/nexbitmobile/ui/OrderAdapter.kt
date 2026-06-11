package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        holder.tvProductName.text = "Order #${pedido.id_pedido}"
        holder.tvTime.text = pedido.fecha_pedido ?: pedido.fecha
        holder.tvQty.text = "Total: $${pedido.total}"
        holder.tvBuyer.text = pedido.usuario_nombre ?: "Cliente"
        holder.tvOrderId.text = "#DXZ_${pedido.id_pedido.toString().padStart(5, '0')}"

        val status = pedido.estado.lowercase()
        holder.tvStatus.text = when {
            status.contains("pend") || status.contains("new") -> "New Order"
            status.contains("confirm") -> "Confirmed"
            status.contains("envia") || status.contains("ship") -> "Shipped"
            status.contains("entrega") || status.contains("deliver") -> "Delivered"
            status.contains("cancel") -> "Cancelled"
            else -> pedido.estado
        }

        holder.tvStatus.setBackgroundResource(
            when {
                status.contains("pend") || status.contains("new") -> R.drawable.bg_status_new
                status.contains("confirm") -> R.drawable.bg_status_confirmed
                status.contains("envia") || status.contains("ship") -> R.drawable.bg_status_shipped
                else -> R.drawable.bg_chip
            }
        )
        holder.tvStatus.setTextColor(
            when {
                status.contains("pend") || status.contains("new") ->
                    holder.itemView.context.resources.getColor(R.color.info, holder.itemView.context.theme)
                status.contains("confirm") ->
                    holder.itemView.context.resources.getColor(R.color.success, holder.itemView.context.theme)
                status.contains("envia") || status.contains("ship") ->
                    holder.itemView.context.resources.getColor(R.color.warning, holder.itemView.context.theme)
                else ->
                    holder.itemView.context.resources.getColor(R.color.text_secondary, holder.itemView.context.theme)
            }
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
