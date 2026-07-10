package com.example.nexbitmobile.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import java.text.NumberFormat
import java.util.Locale

class PagoTransferenciaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PEDIDO_ID = "extra_pedido_id"
        const val EXTRA_TOTAL = "extra_total"
    }

    private lateinit var tvTotalPagar: TextView
    private lateinit var tvPedidoId: TextView
    private lateinit var btnSubirComprobante: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_pago)

        val pedidoId = intent.getIntExtra(EXTRA_PEDIDO_ID, 0)
        val total = intent.getDoubleExtra(EXTRA_TOTAL, 0.0)
        val fmt = NumberFormat.getNumberInstance(Locale.US)

        tvTotalPagar = findViewById(R.id.tvTotalPagar )
        tvPedidoId = findViewById(R.id.tvPedidoId)
        btnSubirComprobante = findViewById(R.id.btnSubirComprobante)

        tvPedidoId.text = getString(R.string.pedido_numero, pedidoId)
        tvTotalPagar.text = getString(R.string.total_a_pagar, fmt.format(total))

        btnSubirComprobante.setOnClickListener {
            val intent = android.content.Intent(this, ConfirmacionPagoActivity::class.java)
            intent.putExtra(EXTRA_PEDIDO_ID, pedidoId)
            startActivity(intent)
        }
    }
}
