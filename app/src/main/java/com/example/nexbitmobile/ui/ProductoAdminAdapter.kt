package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Producto

class ProductoAdminAdapter(
    private var productos: List<Producto>,
    private val onEdit: (Producto) -> Unit,
    private val onDelete: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdminAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProducto: ImageView = view.findViewById(R.id.ivProducto)
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoria)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_admin, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvNombre.text = producto.nombre
        holder.tvCategoria.text = "Categoría: ${producto.categoria_nombre ?: "General"}"
        holder.tvStock.text = "Stock: ${producto.stock_actual}"

        Glide.with(holder.itemView.context)
            .load(producto.imagen_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivProducto)

        holder.btnEdit.setOnClickListener { onEdit(producto) }
        holder.btnDelete.setOnClickListener { onDelete(producto) }
    }

    override fun getItemCount() = productos.size

    fun updateData(newProductos: List<Producto>) {
        this.productos = newProductos
        notifyDataSetChanged()
    }
}
