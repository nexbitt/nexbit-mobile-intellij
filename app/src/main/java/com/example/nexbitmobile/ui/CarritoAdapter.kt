package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.CarritoItem
import java.text.NumberFormat
import java.util.Locale

class CarritoAdapter(
    private var items: List<CarritoItem>,
    private val onQuantityChange: (CarritoItem, Int) -> Unit,
    private val onRemove: (CarritoItem) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivCartItemImage)
        val tvName: TextView = view.findViewById(R.id.tvCartItemName)
        val tvPrice: TextView = view.findViewById(R.id.tvCartItemPrice)
        val tvQty: TextView = view.findViewById(R.id.tvCartItemQty)
        val tvSubtotal: TextView = view.findViewById(R.id.tvCartItemSubtotal)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.nombre
        holder.tvPrice.text = formatter.format(item.precio)
        holder.tvQty.text = item.cantidad.toString()
        holder.tvSubtotal.text = formatter.format(item.subtotal)

        // Image
        if (!item.imagen_url.isNullOrBlank()) {
            Glide.with(holder.ivImage.context)
                .load(item.imagen_url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_placeholder)
        }

        // Quantity controls
        holder.btnMinus.setOnClickListener {
            if (item.cantidad > 1) {
                onQuantityChange(item, item.cantidad - 1)
            }
        }

        holder.btnPlus.setOnClickListener {
            if (item.cantidad < item.stock_actual) {
                onQuantityChange(item, item.cantidad + 1)
            }
        }

        holder.btnRemove.setOnClickListener {
            onRemove(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<CarritoItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
