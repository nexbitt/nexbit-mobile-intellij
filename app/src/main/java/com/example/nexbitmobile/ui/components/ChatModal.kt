package com.example.nexbitmobile.ui.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.nexbitmobile.R

object ChatModal {

    fun show(context: Context, pedidoId: Int, onSendMessage: (String) -> Unit) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_chat_modal, null)

        val tvChatTitle = view.findViewById<TextView>(R.id.tvChatTitle)
        val etChatMessage = view.findViewById<EditText>(R.id.etChatMessage)
        val btnChatSend = view.findViewById<Button>(R.id.btnChatSend)

        tvChatTitle.text = "Chat - Pedido #$pedidoId"

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setNegativeButton("Cerrar", null)
            .create()

        btnChatSend.setOnClickListener {
            val msg = etChatMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                onSendMessage(msg)
                etChatMessage.text.clear()
                Toast.makeText(context, "Mensaje enviado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Escribe un mensaje", Toast.LENGTH_SHORT).show()
            }
        }

        etChatMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                btnChatSend.performClick()
                true
            } else false
        }

        dialog.show()
    }
}
