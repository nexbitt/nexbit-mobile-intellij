package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R

class AyudaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ayuda)

        val toolbar = findViewById<Toolbar>(R.id.toolbarAyuda)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(toolbar.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val faqAnswers = mapOf(
            R.id.faq1 to "Para realizar un pedido:\n1. Navega por el catálogo de productos\n2. Agrega los productos al carrito\n3. Ve al carrito y confirma tu pedido\n4. Realiza el pago y recibe la confirmación",
            R.id.faq2 to "Para rastrear tu entrega:\n1. Ve a 'Mis Pedidos'\n2. Selecciona el pedido activo\n3. Verás el estado actual (Pendiente, En Camino, Entregado)\n4. También puedes contactar al repartidor",
            R.id.faq3 to "Para cancelar un pedido:\n1. Ve a 'Mis Pedidos'\n2. Selecciona el pedido a cancelar\n3. Presiona 'Cancelar Pedido'\n4. Confirma la cancelación\nNota: Solo se pueden cancelar pedidos en estado Pendiente",
            R.id.faq4 to "Política de devoluciones:\n• Plazo máximo: 30 días hábiles\n• El producto debe estar en su empaque original\n• Debes presentar la factura de compra\n• Los costos de envío corren por cuenta del cliente",
            R.id.faq5 to "Métodos de pago aceptados:\n• Tarjetas de crédito y débito (Visa, Mastercard, American Express)\n• Transferencia bancaria\n• PSE\n• Efectivo contra entrega (sujeto a disponibilidad)"
        )

        for ((viewId, answer) in faqAnswers) {
            findViewById<TextView>(viewId)?.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Ayuda")
                    .setMessage(answer)
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
        }

        findViewById<Button>(R.id.btnContactar).setOnClickListener {
            startActivity(Intent(this, ContactoActivity::class.java))
        }

        findViewById<Button>(R.id.btnVerGuia).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Guía de Usuario")
                .setMessage("Bienvenido a Nexbit!\n\nGestiona tus pedidos, productos y clientes desde un solo lugar. Usa el menú de navegación para acceder a todas las funcionalidades.\n\nPara más ayuda, contacta a soporte técnico.")
                .setPositiveButton("Cerrar", null)
                .show()
        }
    }
}
