package com.example.nexbitmobile.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.RepartidorListado

class RepartidorAdminAdapter(
    private var repartidores: List<RepartidorListado>,
    private val onToggleActivo: (RepartidorListado) -> Unit,
    private val onVerDetalle: (RepartidorListado) -> Unit
) : RecyclerView.Adapter<RepartidorAdminAdapter.RepartidorViewHolder>() {

    class RepartidorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvTotalPedidos: TextView = view.findViewById(R.id.tvTotalPedidos)
        val btnToggleActivo: ImageButton = view.findViewById(R.id.btnToggleActivo)
        val btnVerDetalle: ImageButton = view.findViewById(R.id.btnVerDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepartidorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_repartidor_admin, parent, false)
        return RepartidorViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepartidorViewHolder, position: Int) {
        val rep = repartidores[position]
        holder.tvId.text = "#${rep.id_usuario}"
        holder.tvNombre.text = rep.nombre
        holder.tvEmail.text = rep.email
        holder.tvTelefono.text = "Teléfono: ${rep.telefono ?: "N/A"}"
        holder.tvTotalPedidos.text = "Total: ${rep.total_pedidos} | Activos: ${rep.pedidos_activos}"

        val context = holder.itemView.context
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
        }
        if (rep.activo) {
            bg.setColor(ContextCompat.getColor(context, R.color.success_light))
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.success))
            holder.tvEstado.text = "Activo"
        } else {
            bg.setColor(Color.parseColor("#FEE2E2"))
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.error_text))
            holder.tvEstado.text = "Inactivo"
        }
        holder.tvEstado.background = bg

        holder.btnToggleActivo.setOnClickListener { onToggleActivo(rep) }
        holder.btnVerDetalle.setOnClickListener { onVerDetalle(rep) }
    }

    override fun getItemCount() = repartidores.size

    fun updateData(newList: List<RepartidorListado>) {
        this.repartidores = newList
        notifyDataSetChanged()
    }
}
