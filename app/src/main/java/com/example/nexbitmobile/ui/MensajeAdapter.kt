package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Mensaje

class MensajeAdapter(
    private val mensajes: MutableList<Mensaje>,
    private val currentUserId: Int
) : RecyclerView.Adapter<MensajeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mensaje, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mensajes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        val isSent = mensaje.remitente_id == currentUserId

        holder.tvTexto.text = mensaje.mensaje

        val bubble = holder.llBubble
        val container = holder.itemView.findViewById<LinearLayout>(R.id.llMensajeContainer)

        if (isSent) {
            bubble.setBackgroundResource(R.drawable.bg_chat_sent)
            holder.tvTexto.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))
            container.gravity = android.view.Gravity.END
            (container.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = 80
                marginEnd = 0
            }
        } else {
            bubble.setBackgroundResource(R.drawable.bg_chat_received)
            holder.tvTexto.setTextColor(holder.itemView.resources.getColor(android.R.color.black, null))
            container.gravity = android.view.Gravity.START
            (container.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = 0
                marginEnd = 80
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llBubble: LinearLayout = itemView.findViewById(R.id.llMensajeBubble)
        val tvTexto: TextView = itemView.findViewById(R.id.tvMensajeTexto)
    }

    fun addMessage(mensaje: Mensaje) {
        mensajes.add(mensaje)
        notifyItemInserted(mensajes.size - 1)
    }
}
