package com.example.nexbitmobile.ui

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Mensaje

class ChatAdapter(
    private var mensajes: List<Mensaje>,
    private val currentUserId: Int
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvChatNombre)
        val tvMensaje: TextView = view.findViewById(R.id.tvChatMensaje)
        val tvHora: TextView = view.findViewById(R.id.tvChatHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_bubble, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = mensajes[position]
        val esMio = msg.usuario_id == currentUserId

        val parentLayout = holder.itemView as LinearLayout
        parentLayout.gravity = if (esMio) Gravity.END else Gravity.START

        holder.tvNombre.text = if (esMio) "Tú" else (msg.usuario_nombre ?: "Usuario")
        holder.tvNombre.textAlignment = if (esMio) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START

        holder.tvMensaje.text = msg.mensaje

        if (esMio) {
            holder.tvMensaje.setBackgroundColor(Color.parseColor("#111827"))
            holder.tvMensaje.setTextColor(Color.WHITE)
            holder.tvMensaje.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        } else {
            holder.tvMensaje.setBackgroundColor(Color.parseColor("#F3F4F6"))
            holder.tvMensaje.setTextColor(Color.parseColor("#111827"))
            holder.tvMensaje.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        }

        val hora = msg.fecha?.take(16)?.replace("T", " ") ?: ""
        holder.tvHora.text = hora
        holder.tvHora.textAlignment = if (esMio) View.TEXT_ALIGNMENT_TEXT_END else View.TEXT_ALIGNMENT_TEXT_START
    }

    override fun getItemCount(): Int = mensajes.size

    fun updateData(newMensajes: List<Mensaje>) {
        this.mensajes = newMensajes
        notifyDataSetChanged()
    }
}
