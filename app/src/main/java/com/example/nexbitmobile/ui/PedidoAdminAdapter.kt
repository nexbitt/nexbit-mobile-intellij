package com.example.nexbitmobile.ui

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Pedido

class PedidoAdminAdapter(
    private var pedidos: List<Pedido>,
    private val isClienteView: Boolean = false,
    private val onEdit: (Pedido) -> Unit,
    private val onDelete: (Pedido) -> Unit,
    private val onTicket: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdminAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnTicket: ImageButton = view.findViewById(R.id.btnTicket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido_admin, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.tvId.text = "Pedido #${String.format("%06d", pedido.id_pedido)}"
        holder.tvCliente.text = "Cliente: ${pedido.usuario_nombre ?: pedido.usuario_id}"
        holder.tvTotal.text = "Total: $${pedido.total}"
        holder.tvEstado.text = pedido.estado

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, OrderDetailActivity::class.java)
            intent.putExtra("pedido_id", pedido.id_pedido)
            context.startActivity(intent)
        }

        when (pedido.estado) {
            "PENDIENTE" -> holder.tvEstado.setTextColor(Color.parseColor("#f59f00")) // Naranja
            "CONFIRMADO" -> holder.tvEstado.setTextColor(Color.parseColor("#8b5cf6")) // Morado
            "ASIGNADO" -> holder.tvEstado.setTextColor(Color.parseColor("#3b82f6")) // Azul
            "EN_CAMINO" -> holder.tvEstado.setTextColor(Color.parseColor("#f59e0b")) // Amarillo
            "CANCELADO" -> holder.tvEstado.setTextColor(Color.parseColor("#e03131")) // Rojo
            "PAGADO", "ENTREGADO" -> holder.tvEstado.setTextColor(Color.parseColor("#2f9e44")) // Verde
        }

        if (isClienteView) {
            holder.tvCliente.visibility = View.GONE // Cliente no necesita ver su propio nombre en la tarjeta
            
            // Si el pedido está PENDIENTE, permitimos cancelarlo (reutilizando btnDelete con icono de basura o cambiar lógica)
            if (pedido.estado == "PENDIENTE") {
                holder.btnDelete.visibility = View.VISIBLE
                holder.btnDelete.setOnClickListener { onDelete(pedido) }
            } else {
                holder.btnDelete.visibility = View.GONE
            }
            holder.btnEdit.visibility = View.GONE
        } else {
            holder.tvCliente.visibility = View.VISIBLE
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
            
            holder.btnEdit.setOnClickListener { onEdit(pedido) }
            holder.btnDelete.setOnClickListener { onDelete(pedido) }
        }
        
        holder.btnTicket.setOnClickListener { onTicket(pedido) }
    }

    override fun getItemCount() = pedidos.size

    fun updateData(newPedidos: List<Pedido>) {
        this.pedidos = newPedidos
        notifyDataSetChanged()
    }
}
