package com.example.nexbitmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class TrashOrdersActivity : AppCompatActivity() {

    private lateinit var rvPapelera: RecyclerView
    private lateinit var tvEmpty: TextView
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_papelera)

        rvPapelera = findViewById(R.id.rvPapelera)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvPapelera.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        cargarPapelera()
    }

    private fun cargarPapelera() {
        ApiClient.instance.getPedidosEliminados().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
                    val pedidos = response.body() ?: emptyList()
                    if (pedidos.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvPapelera.adapter = TrashAdapter(pedidos, formatter, ::confirmarRestaurar)
                    }
                }
            }

            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                Toast.makeText(this@TrashOrdersActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmarRestaurar(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Restaurar Pedido")
            .setMessage("¿Deseas restaurar el pedido #${pedido.id_pedido} de ${formatter.format(pedido.total)}?")
            .setPositiveButton("Restaurar") { _, _ ->
                ApiClient.instance.restaurarPedido(pedido.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@TrashOrdersActivity, "Pedido restaurado", Toast.LENGTH_SHORT).show()
                            cargarPapelera()
                        } else {
                            Toast.makeText(this@TrashOrdersActivity, "Error al restaurar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@TrashOrdersActivity, "Error de red", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

class TrashAdapter(
    private val pedidos: List<Pedido>,
    private val fmt: NumberFormat,
    private val onRestaurar: (Pedido) -> Unit
) : RecyclerView.Adapter<TrashAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvIdPedido)
        val tvCliente: TextView = view.findViewById(R.id.tvDireccionPedido)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalPedido)
        val btnRestaurar: Button = view.findViewById(R.id.btnBorrar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return VH(view)
    }

    override fun getItemCount() = pedidos.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val p = pedidos[pos]
        h.tvId.text = "Pedido #${p.id_pedido} (eliminado)"
        h.tvCliente.text = "Cliente: ${p.usuario_nombre ?: "N/A"}"
        h.tvTotal.text = fmt.format(p.total)
        h.btnRestaurar.text = "Restaurar"
        h.btnRestaurar.visibility = View.VISIBLE
        h.btnRestaurar.setOnClickListener { onRestaurar(p) }
    }
}
