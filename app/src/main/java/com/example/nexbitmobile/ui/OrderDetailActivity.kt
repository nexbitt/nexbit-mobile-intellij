package com.example.nexbitmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
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

        findViewById<Button>(R.id.btnAcceptOrder).setOnClickListener {
            Toast.makeText(this, "Pedido #${pedido.id_pedido} aceptado", Toast.LENGTH_SHORT).show()
            finish()
        }
        findViewById<Button>(R.id.btnRejectOrder).setOnClickListener {
            Toast.makeText(this, "Pedido #${pedido.id_pedido} rechazado", Toast.LENGTH_SHORT).show()
            finish()
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
}
