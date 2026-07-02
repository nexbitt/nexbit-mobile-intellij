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
import java.text.NumberFormat
import java.util.Locale

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onAddToCart: (Producto) -> Unit,
    private val onItemClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProducto: ImageView = view.findViewById(R.id.ivProducto)
        val tvNombreProducto: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvCategoriaProducto: TextView = view.findViewById(R.id.tvCategoriaProducto)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val btnAgregarCarrito: ImageButton = view.findViewById(R.id.btnAgregarCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvNombreProducto.text = producto.nombre
        holder.tvCategoriaProducto.text = producto.categoria_nombre ?: "General"

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        holder.tvPrecio.text = format.format(producto.precio_venta)

        Glide.with(holder.itemView.context)
            .load(producto.imagen_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivProducto)

        if (producto.stock_actual <= 0) {
            holder.btnAgregarCarrito.alpha = 0.3f
            holder.btnAgregarCarrito.isEnabled = false
        } else {
            holder.btnAgregarCarrito.alpha = 1f
            holder.btnAgregarCarrito.isEnabled = true
            holder.btnAgregarCarrito.setOnClickListener { onAddToCart(producto) }
        }

        holder.itemView.setOnClickListener { onItemClick(producto) }
    }

    override fun getItemCount() = productos.size

    fun updateList(newList: List<Producto>) {
        productos = newList
        notifyDataSetChanged()
    }
}
