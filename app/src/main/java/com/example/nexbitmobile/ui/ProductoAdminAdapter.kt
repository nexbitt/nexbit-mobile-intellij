package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Producto
import java.text.NumberFormat
import java.util.Locale

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
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_admin, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        val ctx = holder.itemView.context

        holder.tvNombre.text = producto.nombre
        holder.tvCategoria.text = "${producto.categoria_nombre ?: "General"} • ${producto.proveedor_nombre ?: "Sin proveedor"}"

        // Stock with low-stock warning
        val isLowStock = producto.stock_actual <= producto.stock_minimo
        holder.tvStock.text = "Stock: ${producto.stock_actual} (mín: ${producto.stock_minimo})"
        holder.tvStock.setTextColor(
            if (isLowStock) ContextCompat.getColor(ctx, R.color.error_text)
            else ContextCompat.getColor(ctx, R.color.text_secondary)
        )

        // Estado chip
        val isActive = producto.activo == 1
        holder.tvEstado.text = if (isActive) "Activo" else "Inactivo"
        holder.tvEstado.setTextColor(
            if (isActive) ContextCompat.getColor(ctx, R.color.success)
            else ContextCompat.getColor(ctx, R.color.text_light)
        )

        Glide.with(ctx)
            .load(producto.imagen_url)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
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
