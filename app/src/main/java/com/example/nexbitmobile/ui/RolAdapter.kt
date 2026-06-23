package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Rol

class RolAdapter(
    private var roles: List<Rol>,
    private val onEdit: (Rol) -> Unit
) : RecyclerView.Adapter<RolAdapter.RolViewHolder>() {

    class RolViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rol_admin, parent, false)
        return RolViewHolder(view)
    }

    override fun onBindViewHolder(holder: RolViewHolder, position: Int) {
        val rol = roles[position]
        holder.tvNombre.text = rol.nombre
        holder.tvDescripcion.text = rol.descripcion ?: "Sin descripción"
        holder.btnEdit.setOnClickListener { onEdit(rol) }
    }

    override fun getItemCount() = roles.size

    fun updateData(newRoles: List<Rol>) {
        this.roles = newRoles
        notifyDataSetChanged()
    }
}
