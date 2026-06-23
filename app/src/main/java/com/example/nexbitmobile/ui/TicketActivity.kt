package com.example.nexbitmobile.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.print.PrintHelper
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import com.example.nexbitmobile.model.PedidoDetalle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TicketActivity : AppCompatActivity() {

    private var pedidoId: Int = -1

    private lateinit var ticketCard: View
    private lateinit var tvTicketOrderId: TextView
    private lateinit var tvTicketCliente: TextView
    private lateinit var tvTicketDireccion: TextView
    private lateinit var tvTicketNit: TextView
    private lateinit var tvTicketFecha: TextView
    private lateinit var tvTicketSubtotal: TextView
    private lateinit var tvTicketIva: TextView
    private lateinit var tvTicketTotal: TextView
    private lateinit var containerItems: LinearLayout

    private val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)

        pedidoId = intent.getIntExtra("pedido_id", -1)
        if (pedidoId == -1) {
            Toast.makeText(this, "Pedido inválido", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        ticketCard = findViewById(R.id.ticketCard)
        tvTicketOrderId = findViewById(R.id.tvTicketOrderId)
        tvTicketCliente = findViewById(R.id.tvTicketCliente)
        tvTicketDireccion = findViewById(R.id.tvTicketDireccion)
        tvTicketNit = findViewById(R.id.tvTicketNit)
        tvTicketFecha = findViewById(R.id.tvTicketFecha)
        tvTicketSubtotal = findViewById(R.id.tvTicketSubtotal)
        tvTicketIva = findViewById(R.id.tvTicketIva)
        tvTicketTotal = findViewById(R.id.tvTicketTotal)
        containerItems = findViewById(R.id.containerTicketItems)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnDownloadPdf).setOnClickListener { descargarPdf() }
        findViewById<View>(R.id.btnSharePrint).setOnClickListener { compartirTicket() }

        cargarPedido()
    }

    private fun cargarPedido() {
        ApiClient.instance.getPedido(pedidoId).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { renderTicket(it) }
                } else {
                    Toast.makeText(this@TicketActivity, "Error al cargar pedido", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Toast.makeText(this@TicketActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderTicket(pedido: Pedido) {
        tvTicketOrderId.text = "#AOXZ_${pedido.id_pedido.toString().padStart(5, '0')}"
        tvTicketCliente.text = pedido.usuario_nombre ?: "Cliente"
        tvTicketDireccion.text = pedido.direccion_entrega ?: "Sin dirección"
        tvTicketNit.text = "NIT: 901.XXX.XXX-X"

        val fecha = pedido.fecha_pedido ?: pedido.fecha ?: ""
        tvTicketFecha.text = try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val output = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))
            output.format(input.parse(fecha))
        } catch (e: Exception) { fecha }

        val subtotal = if (pedido.detalles != null && pedido.detalles.isNotEmpty()) {
            renderDetalles(pedido.detalles)
            pedido.detalles.sumOf { it.subtotal }
        } else {
            renderDetalles(null)
            pedido.total / 1.19
        }

        val iva = subtotal * 0.19
        val total = subtotal + iva

        tvTicketSubtotal.text = format.format(subtotal)
        tvTicketIva.text = format.format(iva)
        tvTicketTotal.text = format.format(total)
    }

    private fun renderDetalles(detalles: List<PedidoDetalle>?) {
        containerItems.removeAllViews()
        if (detalles.isNullOrEmpty()) {
            val row = TextView(this).apply {
                text = "—"
                textSize = 13f
                setTextColor(resources.getColor(R.color.text_secondary, theme))
                gravity = android.view.Gravity.CENTER
            }
            containerItems.addView(row)
            return
        }
        for (item in detalles) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_ticket_row, containerItems, false)
            row.findViewById<TextView>(R.id.tvRowNombre).text = item.producto_nombre ?: "Producto #${item.producto_id}"
            row.findViewById<TextView>(R.id.tvRowCantidad).text = "${item.cantidad}"
            row.findViewById<TextView>(R.id.tvRowSubtotal).text = format.format(item.subtotal)
            containerItems.addView(row)
        }
    }

    private fun descargarPdf() {
        val bitmap = Bitmap.createBitmap(ticketCard.width, ticketCard.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        ticketCard.draw(canvas)

        val printHelper = PrintHelper(this)
        printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
        printHelper.printBitmap("Ticket_Nexbit_${pedidoId}", bitmap)
    }

    private fun compartirTicket() {
        val texto = buildString {
            appendLine("◆ NEXBIT LOGISTICS")
            appendLine("${tvTicketNit.text}")
            appendLine()
            appendLine("${tvTicketOrderId.text}")
            appendLine("Cliente: ${tvTicketCliente.text}")
            appendLine("Dirección: ${tvTicketDireccion.text}")
            appendLine()
            appendLine("Subtotal: ${tvTicketSubtotal.text}")
            appendLine("IVA (19%): ${tvTicketIva.text}")
            appendLine("TOTAL: ${tvTicketTotal.text}")
            appendLine()
            append("— Nexbit System")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
            putExtra(Intent.EXTRA_SUBJECT, "Ticket #AOXZ_${pedidoId.toString().padStart(5, '0')}")
        }
        startActivity(Intent.createChooser(intent, "Compartir ticket"))
    }
}
