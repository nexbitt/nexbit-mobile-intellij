package com.example.nexbitmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import com.example.nexbitmobile.model.RechazarPagoRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class RevisionPagosActivity : AppCompatActivity() {

    private lateinit var rvRevisiones: RecyclerView
    private lateinit var tvEmpty: TextView
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_revision_pagos)

        rvRevisiones = findViewById(R.id.rvRevisiones)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvRevisiones.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        cargarPedidosEnRevision()
    }

    private fun cargarPedidosEnRevision() {
        ApiClient.instance.getPedidosEnRevision().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
                    val pedidos = response.body() ?: emptyList()
                    if (pedidos.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvRevisiones.adapter = RevisionAdapter(pedidos, formatter, ::mostrarDialogoRevision)
                    }
                }
            }

            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                Toast.makeText(this@RevisionPagosActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogoRevision(pedido: Pedido) {
        val view = layoutInflater.inflate(R.layout.dialog_revision_pago, null)
        val ivComprobante = view.findViewById<ImageView>(R.id.ivComprobante)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalRevision)
        val tvCliente = view.findViewById<TextView>(R.id.tvClienteRevision)
        val btnAprobar = view.findViewById<TextView>(R.id.btnAprobarPago)
        val btnRechazar = view.findViewById<TextView>(R.id.btnRechazarPago)

        tvTotal.text = "Total: ${formatter.format(pedido.total)}"
        tvCliente.text = "Cliente: ${pedido.usuario_nombre ?: "N/A"}"

        if (!pedido.comprobante_pago_url.isNullOrEmpty()) {
            Glide.with(this).load(pedido.comprobante_pago_url).into(ivComprobante)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnAprobar.setOnClickListener {
            dialog.dismiss()
            confirmarAprobar(pedido)
        }

        btnRechazar.setOnClickListener {
            dialog.dismiss()
            mostrarDialogoRechazo(pedido)
        }

        dialog.show()
    }

    private fun confirmarAprobar(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Aprobar Pago")
            .setMessage("¿Confirmas la aprobación del pago del pedido #${pedido.id_pedido}?")
            .setPositiveButton("Sí, aprobar") { _, _ ->
                ApiClient.instance.aprobarPago(pedido.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RevisionPagosActivity, "Pago aprobado", Toast.LENGTH_SHORT).show()
                            cargarPedidosEnRevision()
                        } else {
                            Toast.makeText(this@RevisionPagosActivity, "Error al aprobar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@RevisionPagosActivity, "Error de red", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoRechazo(pedido: Pedido) {
        val input = EditText(this).apply {
            hint = "Motivo del rechazo (obligatorio)"
            setPadding(48, 32, 48, 16)
        }

        AlertDialog.Builder(this)
            .setTitle("Rechazar Pago")
            .setMessage("Indica el motivo del rechazo para el pedido #${pedido.id_pedido}:")
            .setView(input)
            .setPositiveButton("Rechazar") { _, _ ->
                val motivo = input.text.toString().trim()
                if (motivo.isEmpty()) {
                    Toast.makeText(this, "Debes escribir un motivo", Toast.LENGTH_SHORT).show()
                } else {
                    ApiClient.instance.rechazarPago(pedido.id_pedido, RechazarPagoRequest(motivo))
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@RevisionPagosActivity, "Pago rechazado", Toast.LENGTH_SHORT).show()
                                    cargarPedidosEnRevision()
                                } else {
                                    Toast.makeText(this@RevisionPagosActivity, "Error al rechazar", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(this@RevisionPagosActivity, "Error de red", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

class RevisionAdapter(
    private val pedidos: List<Pedido>,
    private val fmt: NumberFormat,
    private val onItemClick: (Pedido) -> Unit
) : RecyclerView.Adapter<RevisionAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvIdPedido)
        val tvCliente: TextView = view.findViewById(R.id.tvDireccionPedido)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return VH(view)
    }

    override fun getItemCount() = pedidos.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val p = pedidos[pos]
        h.tvId.text = "Pedido #${p.id_pedido}"
        h.tvCliente.text = "Cliente: ${p.usuario_nombre ?: "N/A"}"
        h.tvTotal.text = fmt.format(p.total)
        h.itemView.setOnClickListener { onItemClick(p) }
    }
}
