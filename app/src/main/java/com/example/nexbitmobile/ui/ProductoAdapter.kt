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
    private val onItemClick: (Producto) -> Unit,
    private val onVerFichaClick: (Producto) -> Unit = {}
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductCategory: TextView = view.findViewById(R.id.tvProductCategory)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvProductStock: TextView = view.findViewById(R.id.tvProductStock)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
        val btnVerFicha: TextView = view.findViewById(R.id.btnVerFicha)
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

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        holder.tvProductPrice.text = format.format(producto.precio_venta)

        Glide.with(holder.itemView.context)
            .load(producto.imagen_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivProductImage)

        if (producto.stock_actual <= 0) {
            holder.btnAddToCart.text = "Sin stock"
            holder.btnAddToCart.isEnabled = false
        } else {
            holder.btnAddToCart.text = "Agregar"
            holder.btnAddToCart.isEnabled = true
            holder.btnAddToCart.setOnClickListener { onAddToCart(producto) }
        }

        holder.btnVerFicha.setOnClickListener { onVerFichaClick(producto) }
        holder.itemView.setOnClickListener { onItemClick(producto) }
    }

    override fun getItemCount() = productos.size

    fun updateList(newList: List<Producto>) {
        productos = newList
        notifyDataSetChanged()
    }
}
