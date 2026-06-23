package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Producto
import java.text.NumberFormat
import java.util.Locale

data class ProductoEnPedido(
    val producto: Producto,
    val cantidad: Int
) {
    val subtotal: Double get() = producto.precio_venta * cantidad
}

class ProductoSheetAdapter(
    private var items: MutableList<ProductoEnPedido>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<ProductoSheetAdapter.ViewHolder>() {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvItemNombre)
        val tvCantidad: TextView = view.findViewById(R.id.tvItemCantidad)
        val tvSubtotal: TextView = view.findViewById(R.id.tvItemSubtotal)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_sheet, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: ViewHolder, pos: Int) {
        val item = items[pos]
        h.tvNombre.text = item.producto.nombre
        h.tvCantidad.text = "x${item.cantidad}"
        h.tvSubtotal.text = fmt.format(item.subtotal)
        h.btnRemove.setOnClickListener { onRemove(pos) }
    }

    fun updateData(newItems: MutableList<ProductoEnPedido>) {
        items = newItems
        notifyDataSetChanged()
    }
}
