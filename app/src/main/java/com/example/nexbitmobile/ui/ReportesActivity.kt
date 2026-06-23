package com.example.nexbitmobile.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class ReportesActivity : AppCompatActivity() {

    private lateinit var tabContainer: LinearLayout
    private lateinit var contentContainer: LinearLayout
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private val numFmt = NumberFormat.getNumberInstance(Locale("es", "CO"))

    private val tabs = listOf(
        "Ventas" to 0, "Inventario" to 1, "Seguridad" to 2, "Carritos" to 3, "Logística" to 4
    )
    private var activeTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        tabContainer = findViewById(R.id.tabContainer)
        contentContainer = findViewById(R.id.contentContainer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        tabs.forEach { (label, idx) ->
            val tab = TextView(this).apply {
                text = label
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(24, 0, 24, 0)
                setOnClickListener { switchTab(idx) }
            }
            tabContainer.addView(tab, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT
            ))
        }

        switchTab(0)
    }

    private fun switchTab(idx: Int) {
        activeTab = idx
        for (i in 0 until tabContainer.childCount) {
            val tv = tabContainer.getChildAt(i) as TextView
            tv.setTextColor(ContextCompat.getColor(this,
                if (i == idx) R.color.black else R.color.menu_divider
            ))
            tv.setTypeface(null, if (i == idx) Typeface.BOLD else Typeface.NORMAL)
            tv.setBackgroundColor(ContextCompat.getColor(this,
                if (i == idx) android.R.color.transparent else android.R.color.transparent
            ))
            if (i == idx) {
                tv.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                tv.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                tv.background = null
                tv.setTextColor(ContextCompat.getColor(this, R.color.menu_divider))
            }
        }
        contentContainer.removeAllViews()
        when (idx) {
            0 -> cargarVentas()
            1 -> cargarInventario()
            2 -> cargarSeguridad()
            3 -> cargarCarritos()
            4 -> cargarLogistica()
        }
    }

    // ─── VENTAS ─────────────────────────────────────────────────────────────────

    private fun cargarVentas() {
        mostrarCargando()
        ApiClient.instance.getReporteVentasKpis().enqueue(object : Callback<com.example.nexbitmobile.model.VentaKpi> {
            override fun onResponse(c: Call<com.example.nexbitmobile.model.VentaKpi>, r: Response<com.example.nexbitmobile.model.VentaKpi>) {
                if (r.isSuccessful) {
                    val kpi = r.body()!!
                    contentContainer.removeAllViews()
                    agregarKPICards(listOf(
                        "Total Tickets" to numFmt.format(kpi.total_tickets),
                        "Ingresos" to formatter.format(kpi.total_ingresos),
                        "Promedio" to formatter.format(kpi.promedio_ticket)
                    ))
                    agregarTitulo("Top Productos")
                    kpi.top_productos.forEach { tp ->
                        agregarItem("${tp.producto}", "${tp.total_uds} uds vendidas")
                    }
                    cargarListaVentas()
                }
            }
            override fun onFailure(c: Call<com.example.nexbitmobile.model.VentaKpi>, t: Throwable) {
                mostrarError()
            }
        })
    }

    private fun cargarListaVentas() {
        ApiClient.instance.getReporteVentas().enqueue(object : Callback<List<com.example.nexbitmobile.model.VentaRow>> {
            override fun onResponse(c: Call<List<com.example.nexbitmobile.model.VentaRow>>, r: Response<List<com.example.nexbitmobile.model.VentaRow>>) {
                if (r.isSuccessful) {
                    val rows = r.body() ?: emptyList()
                    if (rows.isNotEmpty()) {
                        agregarTitulo("Últimas Ventas")
                        for (v in rows.take(15)) {
                            agregarItem(
                                "Factura #${v.Factura_No ?: "—"} — ${v.Cliente ?: "—"}",
                                "${formatter.format(v.Total_Factura ?: 0.0)}  •  ${v.Estado_Pago ?: "—"}"
                            )
                        }
                    }
                }
            }
            override fun onFailure(c: Call<List<com.example.nexbitmobile.model.VentaRow>>, t: Throwable) {}
        })
    }

    // ─── INVENTARIO ─────────────────────────────────────────────────────────────

    private fun cargarInventario() {
        mostrarCargando()
        ApiClient.instance.getReporteInventarioKpis().enqueue(object : Callback<com.example.nexbitmobile.model.InventarioKpi> {
            override fun onResponse(c: Call<com.example.nexbitmobile.model.InventarioKpi>, r: Response<com.example.nexbitmobile.model.InventarioKpi>) {
                if (r.isSuccessful) {
                    val kpi = r.body()!!
                    contentContainer.removeAllViews()
                    agregarKPICards(listOf(
                        "Total" to numFmt.format(kpi.total_productos),
                        "Agotados" to numFmt.format(kpi.agotados),
                        "Stock Bajo" to numFmt.format(kpi.stock_bajo)
                    ))
                    agregarTitulo("Valor Inventario: ${formatter.format(kpi.valor_total_costo)}")
                    agregarItem("Ganancia Potencial", formatter.format(kpi.ganancia_potencial))
                    if (kpi.top_margen.isNotEmpty()) {
                        agregarTitulo("Top Margen %")
                        kpi.top_margen.forEach { m ->
                            agregarItem(m.producto, "${m.margen_pct}%")
                        }
                    }
                    cargarListaInventario()
                }
            }
            override fun onFailure(c: Call<com.example.nexbitmobile.model.InventarioKpi>, t: Throwable) {
                mostrarError()
            }
        })
    }

    private fun cargarListaInventario() {
        ApiClient.instance.getReporteInventario().enqueue(object : Callback<List<com.example.nexbitmobile.model.InventarioRow>> {
            override fun onResponse(c: Call<List<com.example.nexbitmobile.model.InventarioRow>>, r: Response<List<com.example.nexbitmobile.model.InventarioRow>>) {
                if (r.isSuccessful) {
                    val rows = r.body() ?: emptyList()
                    if (rows.isNotEmpty()) {
                        agregarTitulo("Alertas de Stock")
                        for (inv in rows.take(20)) {
                            val alertaIcon = when (inv.Alerta_Stock) {
                                "AGOTADO" -> "🔴"
                                "STOCK BAJO" -> "🟡"
                                else -> "🟢"
                            }
                            agregarItem(
                                "$alertaIcon ${inv.Producto ?: "—"} (Stock: ${inv.Stock_Disponible ?: 0})",
                                "${inv.Categoria ?: "—"} • Margen: ${inv.Margen_Pct ?: 0}%"
                            )
                        }
                    }
                }
            }
            override fun onFailure(c: Call<List<com.example.nexbitmobile.model.InventarioRow>>, t: Throwable) {}
        })
    }

    // ─── SEGURIDAD ──────────────────────────────────────────────────────────────

    private fun cargarSeguridad() {
        mostrarCargando()
        ApiClient.instance.getReporteSeguridadKpis().enqueue(object : Callback<com.example.nexbitmobile.model.SeguridadKpi> {
            override fun onResponse(c: Call<com.example.nexbitmobile.model.SeguridadKpi>, r: Response<com.example.nexbitmobile.model.SeguridadKpi>) {
                if (r.isSuccessful) {
                    val kpi = r.body()!!
                    contentContainer.removeAllViews()
                    agregarKPICards(listOf(
                        "Total" to numFmt.format(kpi.total_usuarios),
                        "Activos" to numFmt.format(kpi.activos),
                        "Inactivos" to numFmt.format(kpi.inactivos)
                    ))
                    agregarTitulo("Usuarios por Rol")
                    kpi.por_rol.forEach { rc ->
                        agregarItem(rc.rol ?: "Sin rol", numFmt.format(rc.cantidad))
                    }
                    cargarListaSeguridad()
                }
            }
            override fun onFailure(c: Call<com.example.nexbitmobile.model.SeguridadKpi>, t: Throwable) {
                mostrarError()
            }
        })
    }

    private fun cargarListaSeguridad() {
        ApiClient.instance.getReporteSeguridad().enqueue(object : Callback<List<com.example.nexbitmobile.model.SeguridadRow>> {
            override fun onResponse(c: Call<List<com.example.nexbitmobile.model.SeguridadRow>>, r: Response<List<com.example.nexbitmobile.model.SeguridadRow>>) {
                if (r.isSuccessful) {
                    val rows = r.body() ?: emptyList()
                    if (rows.isNotEmpty()) {
                        agregarTitulo("Auditoría de Usuarios")
                        for (s in rows.take(20)) {
                            agregarItem(
                                "${s.Nombre_Usuario ?: "—"} (${s.Email_Login ?: "—"})",
                                "${s.Rol ?: "—"}  •  ${s.Estado_Cuenta ?: "—"}"
                            )
                        }
                    }
                }
            }
            override fun onFailure(c: Call<List<com.example.nexbitmobile.model.SeguridadRow>>, t: Throwable) {}
        })
    }

    // ─── CARRITOS ACTIVOS ───────────────────────────────────────────────────────

    private fun cargarCarritos() {
        mostrarCargando()
        ApiClient.instance.getReporteCarritosKpis().enqueue(object : Callback<com.example.nexbitmobile.model.CarritosKpi> {
            override fun onResponse(c: Call<com.example.nexbitmobile.model.CarritosKpi>, r: Response<com.example.nexbitmobile.model.CarritosKpi>) {
                if (r.isSuccessful) {
                    val kpi = r.body()!!
                    contentContainer.removeAllViews()
                    agregarKPICards(listOf(
                        "Items" to numFmt.format(kpi.total_items),
                        "Valor Potencial" to formatter.format(kpi.valor_potencial),
                        "Problemas Stock" to numFmt.format(kpi.con_problema_stock)
                    ))
                    cargarListaCarritos()
                }
            }
            override fun onFailure(c: Call<com.example.nexbitmobile.model.CarritosKpi>, t: Throwable) {
                mostrarError()
            }
        })
    }

    private fun cargarListaCarritos() {
        ApiClient.instance.getReporteCarritos().enqueue(object : Callback<List<com.example.nexbitmobile.model.CarritoRow>> {
            override fun onResponse(c: Call<List<com.example.nexbitmobile.model.CarritoRow>>, r: Response<List<com.example.nexbitmobile.model.CarritoRow>>) {
                if (r.isSuccessful) {
                    val rows = r.body() ?: emptyList()
                    if (rows.isNotEmpty()) {
                        agregarTitulo("Carritos Activos")
                        for (cr in rows.take(20)) {
                            agregarItem(
                                "${cr.Usuario ?: "Invitado"} — ${cr.Producto ?: "—"}",
                                "${formatter.format(cr.Total_Proyectado ?: 0.0)}  •  ${cr.Disponibilidad ?: "—"}"
                            )
                        }
                    }
                }
            }
            override fun onFailure(c: Call<List<com.example.nexbitmobile.model.CarritoRow>>, t: Throwable) {}
        })
    }

    // ─── LOGÍSTICA ──────────────────────────────────────────────────────────────

    private fun cargarLogistica() {
        mostrarCargando()
        ApiClient.instance.getReporteRepartidoresKpis().enqueue(object : Callback<com.example.nexbitmobile.model.LogisticaKpi> {
            override fun onResponse(c: Call<com.example.nexbitmobile.model.LogisticaKpi>, r: Response<com.example.nexbitmobile.model.LogisticaKpi>) {
                if (r.isSuccessful) {
                    val kpi = r.body()!!
                    contentContainer.removeAllViews()
                    agregarKPICards(listOf(
                        "Total Pedidos" to numFmt.format(kpi.total_pedidos),
                        "A Tiempo" to numFmt.format(kpi.a_tiempo),
                        "Con Retraso" to numFmt.format(kpi.con_retraso)
                    ))
                    agregarTitulo("Pedidos por Repartidor")
                    kpi.por_repartidor.forEach { rc ->
                        agregarItem(rc.repartidor ?: "—", numFmt.format(rc.cantidad))
                    }
                    cargarListaLogistica()
                }
            }
            override fun onFailure(c: Call<com.example.nexbitmobile.model.LogisticaKpi>, t: Throwable) {
                mostrarError()
            }
        })
    }

    private fun cargarListaLogistica() {
        ApiClient.instance.getReporteRepartidores().enqueue(object : Callback<List<com.example.nexbitmobile.model.LogisticaRow>> {
            override fun onResponse(c: Call<List<com.example.nexbitmobile.model.LogisticaRow>>, r: Response<List<com.example.nexbitmobile.model.LogisticaRow>>) {
                if (r.isSuccessful) {
                    val rows = r.body() ?: emptyList()
                    if (rows.isNotEmpty()) {
                        agregarTitulo("Entregas Detalle")
                        for (lg in rows.take(15)) {
                            val icon = when (lg.Cumplimiento) {
                                "A TIEMPO" -> "✅"
                                "TARDE" -> "⚠️"
                                else -> "⏳"
                            }
                            agregarItem(
                                "$icon ${lg.Repartidor ?: "—"} → ${lg.Cliente ?: "—"}",
                                "${lg.Cumplimiento ?: "—"}  •  ${formatter.format(lg.Total_Pedido ?: 0.0)}"
                            )
                        }
                    }
                }
            }
            override fun onFailure(c: Call<List<com.example.nexbitmobile.model.LogisticaRow>>, t: Throwable) {}
        })
    }

    // ─── HELPERS DE UI ──────────────────────────────────────────────────────────

    private fun mostrarCargando() {
        contentContainer.removeAllViews()
        val tv = TextView(this).apply {
            text = "Cargando..."
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        }
        contentContainer.addView(tv)
    }

    private fun mostrarError() {
        contentContainer.removeAllViews()
        val tv = TextView(this).apply {
            text = "Error al cargar datos"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        }
        contentContainer.addView(tv)
    }

    private fun agregarTitulo(texto: String) {
        val tv = TextView(this).apply {
            text = texto
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 24, 0, 8)
        }
        contentContainer.addView(tv)
    }

    private fun agregarItem(label: String, sub: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(ContextCompat.getColor(this@ReportesActivity, R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 6) }
        }
        val tvLabel = TextView(this).apply {
            text = label
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }
        val tvSub = TextView(this).apply {
            text = sub
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@ReportesActivity, R.color.menu_divider))
        }
        card.addView(tvLabel)
        card.addView(tvSub)
        contentContainer.addView(card)
    }

    private fun agregarKPICards(kpis: List<Pair<String, String>>) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        for ((label, value) in kpis) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(12, 16, 12, 16)
                setBackgroundColor(ContextCompat.getColor(this@ReportesActivity, R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply { setMargins(0, 0, 6, 0) }
            }
            val tvVal = TextView(this).apply {
                text = value
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(this@ReportesActivity, R.color.white))
            }
            val tvLbl = TextView(this).apply {
                text = label
                textSize = 10f
                setTextColor(ContextCompat.getColor(this@ReportesActivity, R.color.white))
            }
            card.addView(tvVal)
            card.addView(tvLbl)
            row.addView(card)
        }
        contentContainer.addView(row)
    }
}
