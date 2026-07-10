package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.GestionPedidoRequest
import com.example.nexbitmobile.model.Pedido
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private var pedidoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        pedidoId = intent.getIntExtra("pedido_id", -1)
        if (pedidoId == -1) {
            Toast.makeText(this, "Pedido inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        loadOrder()
    }

    private fun loadOrder() {
        ApiClient.instance.getPedido(pedidoId).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { populateView(it) }
                }
            }
            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Toast.makeText(this@OrderDetailActivity, "Connection error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateView(pedido: Pedido) {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }

        findViewById<TextView>(R.id.tvCustomerName).text = pedido.usuario_nombre ?: "Cliente"
        findViewById<TextView>(R.id.tvCustomerEmail).text = "ID: ${pedido.usuario_id}"
        findViewById<TextView>(R.id.tvCustomerAddress).text = pedido.direccion_entrega ?: "Sin dirección"
        findViewById<TextView>(R.id.tvCustomerPhone).text = pedido.numero_documento ?: "N/A"

        findViewById<TextView>(R.id.tvOrderAmount).text = format.format(pedido.total)
        findViewById<TextView>(R.id.tvTotalAmount).text = format.format(pedido.total)
        findViewById<TextView>(R.id.tvDelivery).text = format.format(0)

        val tax = (pedido.total * 0.19).toInt()
        findViewById<TextView>(R.id.tvTax).text = format.format(tax)

        findViewById<View>(R.id.btnViewTicket).setOnClickListener {
            val intent = Intent(this, TicketActivity::class.java)
            intent.putExtra("pedido_id", pedidoId)
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnAcceptOrder).setOnClickListener {
            showAcceptConfirmDialog(pedido)
        }
        findViewById<Button>(R.id.btnRejectOrder).setOnClickListener {
            showRejectDialog(pedido)
        }

        val container = findViewById<LinearLayout>(R.id.orderItemsContainer)
        pedido.detalles?.forEach { detalle ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_order_detail_product, container, false)
            val ivImage = itemView.findViewById<android.widget.ImageView>(R.id.ivDetailProductImage)
            val tvName = itemView.findViewById<TextView>(R.id.tvDetailProductName)
            val tvVariant = itemView.findViewById<TextView>(R.id.tvDetailProductVariant)
            val tvPrice = itemView.findViewById<TextView>(R.id.tvDetailProductPrice)

            tvName.text = detalle.producto_nombre ?: "Product #${detalle.producto_id}"
            tvVariant.text = "Cant: ${detalle.cantidad} pcs"
            tvPrice.text = format.format(detalle.subtotal)

            Glide.with(this)
                .load(detalle.imagen_url)
                .placeholder(R.drawable.ic_placeholder)
                .into(ivImage)

            container.addView(itemView)
        }
    }

    private fun showAcceptConfirmDialog(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Aceptar Pedido #${pedido.id_pedido}")
            .setMessage("¿Confirmas la aceptación de este pedido?")
            .setPositiveButton("Aceptar") { _, _ ->
                gestionarPedido(pedido.id_pedido, "ACEPTAR", null)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRejectDialog(pedido: Pedido) {
        val input = EditText(this).apply {
            hint = "Motivo del rechazo"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            maxLines = 3
            setPadding(48, 32, 48, 16)
        }

        AlertDialog.Builder(this)
            .setTitle("Rechazar Pedido #${pedido.id_pedido}")
            .setMessage("Indica el motivo del rechazo:")
            .setView(input)
            .setPositiveButton("Rechazar") { _, _ ->
                val motivo = input.text.toString().trim()
                if (motivo.isEmpty()) {
                    Toast.makeText(this, "Debes indicar un motivo", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                gestionarPedido(pedido.id_pedido, "RECHAZAR", motivo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun gestionarPedido(pedidoId: Int, accion: String, motivo: String?) {
        val request = GestionPedidoRequest(accion = accion, nota = motivo)
        ApiClient.instance.gestionarPedido(pedidoId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val msg = when (accion) {
                        "ACEPTAR" -> "Pedido #$pedidoId aceptado con éxito"
                        "RECHAZAR" -> "Pedido #$pedidoId rechazado"
                        else -> "Pedido #$pedidoId gestionado"
                    }
                    Toast.makeText(this@OrderDetailActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Error al gestionar pedido"
                    Toast.makeText(this@OrderDetailActivity, error, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@OrderDetailActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
