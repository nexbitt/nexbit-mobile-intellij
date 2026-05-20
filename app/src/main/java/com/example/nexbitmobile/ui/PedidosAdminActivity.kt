package com.example.nexbitmobile.ui

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import com.example.nexbitmobile.model.PedidoRequest
import com.example.nexbitmobile.model.Usuario
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidosAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PedidoAdminAdapter
    private lateinit var fabAdd: FloatingActionButton

    private var usuariosList = listOf<Usuario>()
    private val estados = arrayOf("PENDIENTE", "PAGADO", "ENTREGADO", "CANCELADO")

    // Referencia al WebView para impresión (debe mantenerse para que no sea recolectada por el GC)
    private var printWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos_admin)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PedidoAdminAdapter(emptyList(), this::showEditDialog, this::deletePedido, this::descargarTicket)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showCreateDialog() }

        loadUsuarios()
        loadPedidos()
    }

    private fun loadUsuarios() {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    usuariosList = response.body() ?: emptyList()
                } else {
                    Log.e("PedidosAdmin", "Error loading users: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Log.e("PedidosAdmin", "Users load failed", t)
            }
        })
    }

    private fun loadPedidos() {
        ApiClient.instance.getPedidos().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
                    adapter.updateData(response.body() ?: emptyList())
                } else {
                    Toast.makeText(this@PedidosAdminActivity, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_pedido, null)
        val spUsuario = view.findViewById<Spinner>(R.id.spUsuario)
        val etTotal = view.findViewById<EditText>(R.id.etTotal)
        val spEstado = view.findViewById<Spinner>(R.id.spEstado)

        val userNames = usuariosList.map { "${it.nombre} - ${it.numero_documento ?: ""}" }
        spUsuario.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, userNames)
        spEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Pedido")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                if (usuariosList.isEmpty() || etTotal.text.isEmpty()) {
                    Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = usuariosList[spUsuario.selectedItemPosition].id_usuario
                val request = PedidoRequest(
                    usuario_id = userId,
                    total = etTotal.text.toString().toDoubleOrNull() ?: 0.0,
                    estado = estados[spEstado.selectedItemPosition]
                )

                ApiClient.instance.createPedido(request).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@PedidosAdminActivity, "Pedido creado", Toast.LENGTH_SHORT).show()
                        loadPedidos()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("PedidosAdmin", "Create order failed", t)
                        Toast.makeText(this@PedidosAdminActivity, "Error al crear pedido", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(pedido: Pedido) {
        val view = layoutInflater.inflate(R.layout.dialog_pedido, null)
        val spUsuario = view.findViewById<Spinner>(R.id.spUsuario)
        val etTotal = view.findViewById<EditText>(R.id.etTotal)
        val spEstado = view.findViewById<Spinner>(R.id.spEstado)

        val userNames = usuariosList.map { "${it.nombre} - ${it.numero_documento ?: ""}" }
        spUsuario.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, userNames)
        spEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        // Set current values
        val userIndex = usuariosList.indexOfFirst { it.id_usuario == pedido.usuario_id }
        if (userIndex >= 0) {
            spUsuario.setSelection(userIndex)
            spUsuario.isEnabled = false // Disable changing user
        }
        
        etTotal.setText(pedido.total.toString())
        
        val estadoIndex = estados.indexOf(pedido.estado)
        if (estadoIndex >= 0) spEstado.setSelection(estadoIndex)

        AlertDialog.Builder(this)
            .setTitle("Editar Pedido")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val request = PedidoRequest(
                    usuario_id = pedido.usuario_id,
                    total = etTotal.text.toString().toDoubleOrNull() ?: 0.0,
                    estado = estados[spEstado.selectedItemPosition]
                )

                ApiClient.instance.updatePedido(pedido.id_pedido, request).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@PedidosAdminActivity, "Pedido actualizado", Toast.LENGTH_SHORT).show()
                        loadPedidos()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("PedidosAdmin", "Update order failed", t)
                        Toast.makeText(this@PedidosAdminActivity, "Error al actualizar pedido", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deletePedido(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Deseas eliminar el pedido #${pedido.id_pedido}?")
            .setPositiveButton("Sí") { _, _ ->
                ApiClient.instance.deletePedido(pedido.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@PedidosAdminActivity, "Pedido eliminado", Toast.LENGTH_SHORT).show()
                        loadPedidos()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("PedidosAdmin", "Delete order failed", t)
                        Toast.makeText(this@PedidosAdminActivity, "Error al eliminar pedido", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun descargarTicket(pedido: Pedido) {
        ApiClient.instance.getPedidoTicket(pedido.id_pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { pedidoTicket ->
                        generarHtmlYPdf(pedidoTicket)
                    } ?: run {
                        Toast.makeText(this@PedidosAdminActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PedidosAdminActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Log.e("PedidosAdmin", "Ticket download failed", t)
                Toast.makeText(this@PedidosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generarHtmlYPdf(pedido: Pedido) {
        val detalles = pedido.detalles ?: emptyList()
        val filasProductos = if (detalles.isNotEmpty()) {
            detalles.joinToString("") { d ->
                """
                <tr>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;">${d.producto_nombre}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:center;">${d.cantidad}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">${"$"}${d.precio_unitario}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">${"$"}${d.subtotal}</td>
                </tr>
                """
            }
        } else {
            "<tr><td colspan='4' style='padding:12px;text-align:center;color:#94a3b8;'>Este pedido no tiene productos detallados</td></tr>"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8"/>
                <title>Comprobante de Pedido - #${pedido.id_pedido}</title>
                <style>
                    body { font-family: sans-serif; padding: 20px; color: #1e293b; }
                    .ticket { max-width: 600px; margin: 0 auto; }
                    .header { text-align: center; margin-bottom: 20px; }
                    .brand { font-size: 24px; font-weight: bold; }
                    .info { margin-bottom: 20px; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                    th, td { padding: 10px; border-bottom: 1px solid #e2e8f0; text-align: left; }
                    th { background: #f8fafc; }
                    .total { text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="ticket">
                    <div class="header">
                        <div class="brand">Nexbit</div>
                        <div>Comprobante de Pedido #${String.format("%06d", pedido.id_pedido)}</div>
                    </div>
                    <div class="info">
                        <p><strong>Cliente:</strong> ${pedido.usuario_nombre ?: "N/A"}</p>
                        <p><strong>Documento:</strong> ${pedido.numero_documento ?: "N/A"}</p>
                        <p><strong>Fecha:</strong> ${pedido.fecha}</p>
                        <p><strong>Estado:</strong> ${pedido.estado}</p>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th>Producto</th>
                                <th style="text-align:center;">Cant.</th>
                                <th style="text-align:right;">P. Unit</th>
                                <th style="text-align:right;">Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            $filasProductos
                        </tbody>
                    </table>
                    <div class="total">
                        Total a Pagar: ${"$"}${pedido.total}
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        doWebViewPrint(htmlContent)
    }

    private fun doWebViewPrint(htmlContent: String) {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(view)
                printWebView = null
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
        printWebView = webView
    }

    private fun createWebPrintJob(webView: WebView) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter("Pedido_Nexbit")
        val printJobName = getString(R.string.app_name) + " Document"
        printManager.print(printJobName, printAdapter, PrintAttributes.Builder().build())
    }
}
