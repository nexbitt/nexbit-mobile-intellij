package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Categoria

class CategoriaAdapter(
    private var categorias: List<Categoria>,
    private val onEdit: (Categoria) -> Unit,
    private val onDelete: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria_admin, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.tvNombre.text = categoria.nombre
        holder.tvDescripcion.text = categoria.descripcion ?: "Sin descripción"

        holder.btnEdit.setOnClickListener { onEdit(categoria) }
        holder.btnDelete.setOnClickListener { onDelete(categoria) }
    }

    override fun getItemCount() = categorias.size

    fun updateData(newCategorias: List<Categoria>) {
        this.categorias = newCategorias
        notifyDataSetChanged()
    }
}
