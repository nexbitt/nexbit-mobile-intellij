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
    private val onAddToCart: (Producto) -> Unit,
    private val onItemClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductCategory: TextView = view.findViewById(R.id.tvProductCategory)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvProductStock: TextView = view.findViewById(R.id.tvProductStock)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvProductName.text = producto.nombre
        holder.tvProductCategory.text = producto.categoria_nombre ?: "General"
        holder.tvProductStock.text = "Stock: ${producto.stock_actual}"

        // Format price to COP
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        holder.tvProductPrice.text = format.format(producto.precio_venta)

        Glide.with(holder.itemView.context)
            .load(producto.imagen_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivProductImage)

        // Set click listener on the entire item card
        holder.itemView.setOnClickListener { onItemClick(producto) }

        // Set click listener on the add to cart button
        holder.btnAddToCart.setOnClickListener { onAddToCart(producto) }
    }

    override fun getItemCount() = productos.size

    fun updateList(newList: List<Producto>) {
        productos = newList
        notifyDataSetChanged()
    }
}
