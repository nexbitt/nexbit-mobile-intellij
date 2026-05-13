package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private val onAddToCart: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvCategory: TextView = view.findViewById(R.id.tvProductCategory)
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvStock: TextView = view.findViewById(R.id.tvProductStock)
        val btnAdd: Button = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvName.text = producto.nombre
        holder.tvPrice.text = formatter.format(producto.precio_venta)
        holder.tvStock.text = "Stock: ${producto.stock_actual}"

        // Category badge
        if (!producto.categoria_nombre.isNullOrBlank()) {
            holder.tvCategory.text = producto.categoria_nombre
            holder.tvCategory.visibility = View.VISIBLE
        } else {
            holder.tvCategory.visibility = View.GONE
        }

        // Image
        if (!producto.imagen_url.isNullOrBlank()) {
            Glide.with(holder.ivImage.context)
                .load(producto.imagen_url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_placeholder)
        }

        // Disable add if no stock
        if (producto.stock_actual <= 0) {
            holder.btnAdd.isEnabled = false
            holder.btnAdd.text = "Sin stock"
            holder.btnAdd.alpha = 0.5f
        } else {
            holder.btnAdd.isEnabled = true
            holder.btnAdd.text = "Agregar"
            holder.btnAdd.alpha = 1.0f
        }

        holder.btnAdd.setOnClickListener {
            onAddToCart(producto)
        }
    }

    override fun getItemCount() = productos.size

    fun updateList(newList: List<Producto>) {
        productos = newList
        notifyDataSetChanged()
    }
}
