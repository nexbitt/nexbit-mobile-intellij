package com.example.nexbitmobile.ui.components

import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.nexbitmobile.R

object AdminCheckoutDialog {

    fun show(
        context: Context,
        pedidoId: Int,
        clienteNombre: String,
        total: Double,
        onApprove: () -> Unit,
        onReject: () -> Unit
    ) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_admin_checkout, null)

        view.findViewById<TextView>(R.id.tvCheckoutPedidoId).text = "Pedido #$pedidoId"
        view.findViewById<TextView>(R.id.tvCheckoutCliente).text = "Cliente: $clienteNombre"
        view.findViewById<TextView>(R.id.tvCheckoutTotal).text = "Total: $${String.format("%,.2f", total)}"

        AlertDialog.Builder(context)
            .setTitle("Confirmar Pedido")
            .setView(view)
            .setPositiveButton("Aprobar") { _, _ -> onApprove() }
            .setNegativeButton("Rechazar") { _, _ -> onReject() }
            .setNeutralButton("Cancelar", null)
            .show()
    }
}
