package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Proveedor

class ProveedorAdapter(
    private var proveedores: List<Proveedor>,
    private val onEdit: (Proveedor) -> Unit,
    private val onDelete: (Proveedor) -> Unit
) : RecyclerView.Adapter<ProveedorAdapter.ProveedorViewHolder>() {

    class ProveedorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProveedorNombre: TextView = view.findViewById(R.id.tvProveedorNombre)
        val tvProveedorNit: TextView = view.findViewById(R.id.tvProveedorNit)
        val btnEditProveedor: View = view.findViewById(R.id.btnEditProveedor)
        val btnDeleteProveedor: View = view.findViewById(R.id.btnDeleteProveedor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_proveedor, parent, false)
        return ProveedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        val proveedor = proveedores[position]
        holder.tvProveedorNombre.text = proveedor.nombre
        holder.tvProveedorNit.text = "NIT: ${proveedor.nit}"

        holder.btnEditProveedor.setOnClickListener { onEdit(proveedor) }
        holder.btnDeleteProveedor.setOnClickListener { onDelete(proveedor) }
    }

    override fun getItemCount() = proveedores.size

    fun updateData(newProveedores: List<Proveedor>) {
        this.proveedores = newProveedores
        notifyDataSetChanged()
    }
}
