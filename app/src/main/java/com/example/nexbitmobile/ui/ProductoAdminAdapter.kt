package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale
import androidx.core.content.ContextCompat
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
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_admin, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        val ctx = holder.itemView.context

        holder.tvNombre.text = producto.nombre
        holder.tvCategoria.text = producto.categoria_nombre ?: "General"

        val isLowStock = producto.stock_actual <= producto.stock_minimo
        holder.tvStock.text = "Stock: ${producto.stock_actual} (mín: ${producto.stock_minimo})"
        holder.tvStock.setTextColor(
            if (isLowStock) ContextCompat.getColor(ctx, R.color.error_text)
            else ContextCompat.getColor(ctx, R.color.text_secondary)
        )

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        holder.tvPrecio.text = format.format(producto.precio_venta)
        holder.tvPrecio.visibility = View.VISIBLE

        Glide.with(ctx)
            .load(producto.imagen_url)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.ivProducto)

        holder.itemView.setOnClickListener { onEdit(producto) }

        holder.btnMenu.visibility = View.VISIBLE
        holder.btnMenu.setOnClickListener { v ->
            val popup = PopupMenu(ctx, v)
            popup.menu.add(0, 1, 0, "Editar")
            popup.menu.add(0, 2, 0, "Eliminar")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { onEdit(producto); true }
                    2 -> { onDelete(producto); true }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = productos.size

    fun updateData(newProductos: List<Producto>) {
        this.productos = newProductos
        notifyDataSetChanged()
    }
}
