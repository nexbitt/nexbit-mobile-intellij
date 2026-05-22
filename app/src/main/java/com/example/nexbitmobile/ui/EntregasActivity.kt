package com.example.nexbitmobile.ui

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntregasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EntregaAdapter
    private lateinit var tvEmpty: TextView
    private var printWebView: WebView? = null
    private var rolId: Int = 2
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_entregas)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        rolId = prefs.getInt("rolId", 2)
        userId = prefs.getInt("userId", 0)

        try {
            val mainView = findViewById<android.view.View>(R.id.main)
            if (mainView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            } else {
                Log.w("EntregasActivity", "View with id 'main' not found in layout")
            }
        } catch (e: Exception) {
            Log.e("EntregasActivity", "Error setting window insets listener", e)
        }

        try {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            if (toolbar != null) {
                setSupportActionBar(toolbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = if (rolId == 4) "Mis Entregas" else "Entregas"
                toolbar.setNavigationOnClickListener { finish() }
            } else {
                Log.w("EntregasActivity", "Toolbar not found in layout")
            }
        } catch (e: Exception) {
            Log.e("EntregasActivity", "Error initializing toolbar", e)
        }

        try {
            recyclerView = findViewById(R.id.recyclerView) ?: throw NullPointerException("RecyclerView not found")
            tvEmpty = findViewById(R.id.tvEmpty) ?: throw NullPointerException("Empty TextView not found")

            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = EntregaAdapter(emptyList(), this::descargarTicket)
            recyclerView.adapter = adapter

            loadEntregas()
        } catch (e: Exception) {
            Log.e("EntregasActivity", "Error initializing UI components", e)
            Toast.makeText(this, "Error al inicializar la pantalla: \${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadEntregas() {
        Log.d("EntregasActivity", "Loading entregas for rolId=$rolId, userId=$userId...")
        try {
            ApiClient.instance.getPedidos().enqueue(object : Callback<List<Pedido>> {
                override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                    Log.d("EntregasActivity", "API Response: code=\${response.code()}, isSuccessful=\${response.isSuccessful}")
                    if (response.isSuccessful) {
                        try {
                            val allPedidos = response.body() ?: emptyList()
                            val entregas = when {
                                rolId == 1 -> allPedidos.filter { it.estado == "ENTREGADO" }
                                rolId == 4 -> allPedidos.filter { 
                                    it.repartidor_id == userId && it.estado != "ENTREGADO"
                                }
                                else -> emptyList()
                            }
                            Log.d("EntregasActivity", "Filtered \${entregas.size} entregas from \${allPedidos.size} total pedidos")
                            adapter.updateData(entregas)
                            tvEmpty.visibility = if (entregas.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                        } catch (e: Exception) {
                            Log.e("EntregasActivity", "Error processing response", e)
                            Toast.makeText(this@EntregasActivity, "Error al procesar entregas", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.w("EntregasActivity", "API response not successful: \${response.code()}")
                        Toast.makeText(this@EntregasActivity, "Error al cargar entregas (\${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                    Log.e("EntregasActivity", "API call failed", t)
                    Toast.makeText(this@EntregasActivity, "Error de conexión: \${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("EntregasActivity", "Exception in loadEntregas", e)
            Toast.makeText(this, "Excepción al cargar entregas: \${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun descargarTicket(pedido: Pedido) {
        ApiClient.instance.getPedidoTicket(pedido.id_pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { pedidoTicket ->
                        generarHtmlYPdf(pedidoTicket)
                    } ?: run {
                        Toast.makeText(this@EntregasActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@EntregasActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Toast.makeText(this@EntregasActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                Log.e("EntregasActivity", "Ticket download failed", t)
            }
        })
    }

    private fun generarHtmlYPdf(pedido: Pedido) {
        val detalles = pedido.detalles ?: emptyList()
        val filasProductos = if (detalles.isNotEmpty()) {
            detalles.joinToString("") { d ->
                """
                <tr>
                    <td style=\"padding:8px 12px;border-bottom:1px solid #e2e8f0;\">${d.producto_nombre}</td>
                    <td style=\"padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:center;\">${d.cantidad}</td>
                    <td style=\"padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;\">$${d.precio_unitario}</td>
                    <td style=\"padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;\">$${d.subtotal}</td>
                </tr>
                """.trimIndent()
            }
        } else {
            "<tr><td colspan='4' style='padding:12px;text-align:center;color:#94a3b8;'>Este pedido no tiene productos detallados</td></tr>"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html lang=\"es\">
            <head>
                <meta charset=\"UTF-8\"/>
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
                <div class=\"ticket\">
                    <div class=\"header\">
                        <div class=\"brand\">Nexbit</div>
                        <div>Comprobante de Pedido #${String.format("%06d", pedido.id_pedido)}</div>
                    </div>
                    <div class=\"info\">
                        <p><strong>Cliente:</strong> ${pedido.usuario_nombre ?: pedido.usuario_id}</p>
                        <p><strong>Estado:</strong> ${pedido.estado}</p>
                        <p><strong>Fecha:</strong> ${pedido.fecha}</p>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th>Producto</th>
                                <th>Cant.</th>
                                <th>Precio</th>
                                <th>Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            $filasProductos
                        </tbody>
                    </table>
                    <div class=\"total\">Total: $${pedido.total}</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printWebView = WebView(this).apply {
            webViewClient = WebViewClient()
            loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
        }

        printWebView?.post {
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = printWebView?.createPrintDocumentAdapter("Pedido_${pedido.id_pedido}")
            if (printAdapter != null) {
                printManager.print("Pedido_${pedido.id_pedido}", printAdapter, PrintAttributes.Builder().build())
            }
        }
    }
}