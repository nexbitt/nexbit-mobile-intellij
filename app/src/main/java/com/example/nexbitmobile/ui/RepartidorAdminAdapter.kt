package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Usuario

class RepartidorAdminAdapter(
    private var repartidores: List<Usuario>,
    private val onEdit: (Usuario) -> Unit,
    private val onDelete: (Usuario) -> Unit
) : RecyclerView.Adapter<RepartidorAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repartidor_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rep = repartidores[position]
        holder.tvNombre.text = rep.nombre
        holder.tvTelefono.text = rep.telefono ?: "Sin teléfono"
        holder.btnEdit.setOnClickListener { onEdit(rep) }
        holder.btnDelete.setOnClickListener { onDelete(rep) }
    }

    override fun getItemCount() = repartidores.size

    fun updateData(newData: List<Usuario>) {
        repartidores = newData
        notifyDataSetChanged()
    }
}
