package com.example.nexbitmobile.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class RepartidorDetailActivity : AppCompatActivity() {

    private var repartidorId: Int = 0
    private var pedidosDisponibles = listOf<PedidoRepartidor>()
    private var pedidosAsignados = listOf<PedidoRepartidor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repartidor_detalle)

        repartidorId = intent.getIntExtra("repartidor_id", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = intent.getStringExtra("repartidor_nombre") ?: "Repartidor"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<TextView>(R.id.tvDetNombre).text = intent.getStringExtra("repartidor_nombre")
        findViewById<TextView>(R.id.tvDetEmail).text = intent.getStringExtra("repartidor_email")
        findViewById<TextView>(R.id.tvDetTelefono).text = intent.getStringExtra("repartidor_telefono")
        findViewById<TextView>(R.id.tvDetDireccion).text = intent.getStringExtra("repartidor_direccion")
        findViewById<TextView>(R.id.tvDetDocumento).text = intent.getStringExtra("repartidor_documento") ?: "N/A"

        val spPedidos = findViewById<Spinner>(R.id.spPedidosConfirmados)
        val btnAsignar = findViewById<View>(R.id.btnAsignar)
        val tvNoPedidos = findViewById<TextView>(R.id.tvNoPedidos)

        cargarPedidosDisponibles(spPedidos, tvNoPedidos)

        btnAsignar.setOnClickListener {
            val selectedPos = spPedidos.selectedItemPosition
            if (selectedPos < 0 || selectedPos >= pedidosDisponibles.size) {
                Toast.makeText(this, "Seleccione un pedido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val pedidoId = pedidosDisponibles[selectedPos].id_pedido
            ApiClient.instance.asignarPedido(repartidorId, AsignarPedidoRequest(pedidoId))
                .enqueue(object : Callback<Void> {
                    override fun onResponse(c: Call<Void>, res: Response<Void>) {
                        if (res.isSuccessful) {
                            Toast.makeText(this@RepartidorDetailActivity, "Pedido asignado", Toast.LENGTH_SHORT).show()
                            cargarPedidosDisponibles(spPedidos, tvNoPedidos)
                            cargarPedidosAsignados()
                        } else Toast.makeText(this@RepartidorDetailActivity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(c: Call<Void>, t: Throwable) {
                        Toast.makeText(this@RepartidorDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        cargarPedidosAsignados()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun cargarPedidosDisponibles(spPedidos: Spinner, tvNoPedidos: TextView) {
        ApiClient.instance.getPedidosSinAsignar().enqueue(object : Callback<List<PedidoRepartidor>> {
            override fun onResponse(c: Call<List<PedidoRepartidor>>, res: Response<List<PedidoRepartidor>>) {
                if (res.isSuccessful) {
                    pedidosDisponibles = res.body() ?: emptyList()
                    val nombres = pedidosDisponibles.map {
                        "Pedido #${it.id_pedido} - ${it.cliente?.nombre ?: "N/A"}"
                    }.toMutableList()
                    if (nombres.isEmpty()) {
                        nombres.add("No hay pedidos confirmados pendientes por asignar")
                        tvNoPedidos.visibility = View.VISIBLE
                    } else {
                        tvNoPedidos.visibility = View.GONE
                    }
                    spPedidos.adapter = ArrayAdapter(this@RepartidorDetailActivity,
                        android.R.layout.simple_spinner_dropdown_item, nombres)
                }
            }
            override fun onFailure(c: Call<List<PedidoRepartidor>>, t: Throwable) {
                tvNoPedidos.visibility = View.VISIBLE
            }
        })
    }

    private fun cargarPedidosAsignados() {
        ApiClient.instance.getRepartidor(repartidorId).enqueue(object : Callback<RepartidorResponse> {
            override fun onResponse(c: Call<RepartidorResponse>, res: Response<RepartidorResponse>) {
                if (res.isSuccessful) {
                    val data = res.body()
                    pedidosAsignados = data?.pedidos_repartidor ?: emptyList()
                    val container = findViewById<LinearLayout>(R.id.layoutPedidosContainer)
                    container.removeAllViews()
                    val tvEmpty = findViewById<TextView>(R.id.tvEmptyPedidos)

                    if (pedidosAsignados.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        return
                    }
                    tvEmpty.visibility = View.GONE

                    for (pedido in pedidosAsignados) {
                        val card = LayoutInflater.from(this@RepartidorDetailActivity)
                            .inflate(R.layout.item_pedido_asignado, container, false)

                        card.findViewById<TextView>(R.id.tvPedidoId).text = "Pedido #${pedido.id_pedido}"
                        card.findViewById<TextView>(R.id.tvClienteNombre).text =
                            pedido.cliente?.nombre ?: "Cliente"
                        card.findViewById<TextView>(R.id.tvDireccion).text =
                            pedido.direccion_entrega ?: "Sin dirección"
                        card.findViewById<TextView>(R.id.tvBadgeEstado).let { badge ->
                            when (pedido.estado) {
                                "EN_CAMINO" -> { badge.text = "● En Camino"; badge.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark)) }
                                "ASIGNADO" -> { badge.text = "● Asignado"; badge.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark)) }
                                "ENTREGADO" -> { badge.text = "✓ Entregado"; badge.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark)) }
                                else -> { badge.text = "✓ A tiempo"; badge.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark)) }
                            }
                        }

                        card.findViewById<View>(R.id.btnViewPedido).setOnClickListener {
                            mostrarDetallePedido(pedido)
                        }

                        container.addView(card)
                    }
                }
            }
            override fun onFailure(c: Call<RepartidorResponse>, t: Throwable) {
                Toast.makeText(this@RepartidorDetailActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDetallePedido(pedido: PedidoRepartidor) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pedido_repartidor, null)

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0; maximumFractionDigits = 0
        }

        view.findViewById<TextView>(R.id.tvDialogTitulo).text = "Detalle Pedido #${pedido.id_pedido}"

        val sbGeneral = StringBuilder()
        sbGeneral.append("Cliente: ${pedido.cliente?.nombre ?: "N/A"}\n")
        sbGeneral.append("Dirección: ${pedido.direccion_entrega ?: "N/A"}\n")
        sbGeneral.append("Notas: ${pedido.notas_entrega ?: "Ninguna"}\n")
        sbGeneral.append("Total: ${format.format(pedido.total)}")
        view.findViewById<TextView>(R.id.tvDialogGeneral).text = sbGeneral.toString()

        val sbProductos = StringBuilder()
        if (pedido.detalle_pedido.isNullOrEmpty()) {
            sbProductos.append("Sin productos")
        } else {
            for (det in pedido.detalle_pedido) {
                sbProductos.append("${det.cantidad}x ${det.producto?.nombre ?: "Producto"} — ${format.format(det.subtotal)}\n")
            }
        }
        view.findViewById<TextView>(R.id.tvDialogProductos).text = sbProductos.toString()

        val spEstado = view.findViewById<Spinner>(R.id.spCambiarEstado)
        spEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("EN_RUTA", "ENTREGADO", "CANCELADO"))

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .show()

        view.findViewById<View>(R.id.btnActualizarEstado).setOnClickListener {
            val nuevoEstado = spEstado.selectedItem.toString()
            ApiClient.instance.cambiarEstadoPedido(pedido.id_pedido, EstadoPedidoRequest(nuevoEstado, null))
                .enqueue(object : Callback<Void> {
                    override fun onResponse(c: Call<Void>, res: Response<Void>) {
                        if (res.isSuccessful) {
                            Toast.makeText(this@RepartidorDetailActivity, "Estado actualizado", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            cargarPedidosAsignados()
                        } else Toast.makeText(this@RepartidorDetailActivity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(c: Call<Void>, t: Throwable) {
                        Toast.makeText(this@RepartidorDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        view.findViewById<View>(R.id.btnCerrarDialog).setOnClickListener { dialog.dismiss() }
    }
}
