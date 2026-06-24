package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Producto

class ProductoAdminAdapter(
    private var productos: List<Producto>,
    private val onEdit: (Producto) -> Unit,
    private val onStock: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdminAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnStock: View = view.findViewById(R.id.btnStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_admin, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        val ctx = holder.itemView.context

        holder.tvNombre.text = producto.nombre

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        holder.tvSubtitle.text = "${producto.stock_actual} unidades · ${format.format(producto.precio_venta)}"

        holder.btnEdit.setOnClickListener { onEdit(producto) }
        holder.btnStock.setOnClickListener { onStock(producto) }
    }

    override fun getItemCount() = productos.size

    fun updateData(newProductos: List<Producto>) {
        this.productos = newProductos
        notifyDataSetChanged()
    }
}
