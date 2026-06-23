package com.example.nexbitmobile.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepartidoresAdminActivity : AppCompatActivity() {

    private lateinit var rvRepartidores: RecyclerView
    private lateinit var adapter: RepartidorAdminAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: EditText
    private lateinit var chipAll: TextView
    private lateinit var chipActive: TextView
    private lateinit var chipInactive: TextView
    private var allRepartidores: List<RepartidorListado> = emptyList()
    private var currentFilter = "ALL" // ALL, ACTIVE, INACTIVE
    private var pedidosSinAsignar: List<PedidoRepartidor> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_repartidores_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvRepartidores = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        etSearch = findViewById(R.id.etSearch)
        chipAll = findViewById(R.id.chipAll)
        chipActive = findViewById(R.id.chipActive)
        chipInactive = findViewById(R.id.chipInactive)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        adapter = RepartidorAdminAdapter(
            emptyList(),
            onToggleActivo = { rep -> toggleActivo(rep) },
            onVerDetalle = { rep -> verDetalle(rep) }
        )
        rvRepartidores.layoutManager = LinearLayoutManager(this)
        rvRepartidores.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { filterRepartidores() }
        })

        setupChips()
        loadRepartidores()
    }

    private fun setupChips() {
        val activeChip = { chip: TextView ->
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(resources.getColor(R.color.chip_selected_text, theme))
        }
        val inactiveChip = { chip: TextView ->
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(resources.getColor(R.color.chip_text, theme))
        }
        val resetAll = {
            inactiveChip(chipAll)
            inactiveChip(chipActive)
            inactiveChip(chipInactive)
        }

        resetAll()
        activeChip(chipAll)

        chipAll.setOnClickListener {
            resetAll(); activeChip(chipAll); currentFilter = "ALL"; filterRepartidores()
        }
        chipActive.setOnClickListener {
            resetAll(); activeChip(chipActive); currentFilter = "ACTIVE"; filterRepartidores()
        }
        chipInactive.setOnClickListener {
            resetAll(); activeChip(chipInactive); currentFilter = "INACTIVE"; filterRepartidores()
        }
    }

    private fun loadRepartidores() {
        progressBar.visibility = View.VISIBLE
        ApiClient.instance.getRepartidores().enqueue(object : Callback<List<RepartidorListado>> {
            override fun onResponse(call: Call<List<RepartidorListado>>, response: Response<List<RepartidorListado>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    allRepartidores = response.body() ?: emptyList()
                    filterRepartidores()
                } else {
                    Toast.makeText(this@RepartidoresAdminActivity, "Error (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<RepartidorListado>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@RepartidoresAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterRepartidores() {
        val query = etSearch.text.toString().trim().lowercase()
        var filtered = allRepartidores

        when (currentFilter) {
            "ACTIVE" -> filtered = filtered.filter { it.activo }
            "INACTIVE" -> filtered = filtered.filter { !it.activo }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter { it.nombre.lowercase().contains(query) || it.email.lowercase().contains(query) }
        }

        adapter.updateData(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        rvRepartidores.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun toggleActivo(rep: RepartidorListado) {
        val nuevoEstado = !rep.activo
        ApiClient.instance.toggleActivoRepartidor(rep.id_usuario, mapOf("activo" to nuevoEstado))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RepartidoresAdminActivity, if (nuevoEstado) "Activado" else "Desactivado", Toast.LENGTH_SHORT).show()
                        loadRepartidores()
                    } else {
                        Toast.makeText(this@RepartidoresAdminActivity, "Error (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@RepartidoresAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun verDetalle(rep: RepartidorListado) {
        progressBar.visibility = View.VISIBLE
        ApiClient.instance.getRepartidor(rep.id_usuario).enqueue(object : Callback<RepartidorResponse> {
            override fun onResponse(call: Call<RepartidorResponse>, response: Response<RepartidorResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) showDetailDialog(rep, data)
                } else {
                    Toast.makeText(this@RepartidoresAdminActivity, "Error al cargar detalle", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RepartidorResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@RepartidoresAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
        loadPedidosSinAsignar()
    }

    private fun loadPedidosSinAsignar() {
        ApiClient.instance.getPedidosSinAsignar().enqueue(object : Callback<List<PedidoRepartidor>> {
            override fun onResponse(call: Call<List<PedidoRepartidor>>, response: Response<List<PedidoRepartidor>>) {
                if (response.isSuccessful) {
                    pedidosSinAsignar = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<PedidoRepartidor>>, t: Throwable) {}
        })
    }

    private fun showDetailDialog(rep: RepartidorListado, detalle: RepartidorResponse) {
        val scrollView = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }
        scrollView.addView(container)

        // Info card
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            radius = 12f
            cardElevation = 3f
            setContentPadding(16, 16, 16, 16)
        }
        val cardContent = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        cardContent.addView(TextView(this).apply {
            text = "${rep.nombre}"
            textSize = 20f; setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, theme))
        })
        cardContent.addView(TextView(this).apply {
            text = "Email: ${rep.email}"; textSize = 14f
            setTextColor(resources.getColor(R.color.text_secondary, theme))
        })
        cardContent.addView(TextView(this).apply {
            text = "Tel: ${rep.telefono ?: "N/A"}"; textSize = 14f
            setTextColor(resources.getColor(R.color.text_secondary, theme))
        })
        cardContent.addView(TextView(this).apply {
            text = "Estado: ${if (rep.activo) "Activo" else "Inactivo"}"
            textSize = 14f; setTextColor(resources.getColor(if (rep.activo) R.color.success else R.color.error_text, theme))
        })
        card.addView(cardContent)
        container.addView(card)

        // Stats
        val statsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        val pedidos = detalle.pedidos_repartidor ?: emptyList()
        val entregados = pedidos.count { it.estado == "ENTREGADO" }
        val cancelados = pedidos.count { it.estado == "CANCELADO" }
        val pendientes = pedidos.count { it.estado == "ASIGNADO" || it.estado == "EN_CAMINO" }

        listOf(
            Triple("Entregados", entregados.toString(), R.color.success),
            Triple("Cancelados", cancelados.toString(), R.color.error_text),
            Triple("Pendientes", pendientes.toString(), R.color.warning)
        ).forEach { (label, value, color) ->
            statsRow.addView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 8 }
                setPadding(12, 12, 12, 12)
                setBackgroundResource(R.drawable.bg_card)
                addView(TextView(this@RepartidoresAdminActivity).apply {
                    text = value; textSize = 22f; setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(resources.getColor(color, theme))
                })
                addView(TextView(this@RepartidoresAdminActivity).apply {
                    text = label; textSize = 11f
                    setTextColor(resources.getColor(R.color.text_secondary, theme))
                })
            })
        }
        container.addView(statsRow)

        // Asignar pedido section
        container.addView(TextView(this).apply {
            text = "Asignar Nuevo Pedido"
            textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, theme))
        })
        val assignRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8; bottomMargin = 16 }
        }
        val spPedidos = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            adapter = ArrayAdapter(this@RepartidoresAdminActivity, android.R.layout.simple_spinner_dropdown_item,
                pedidosSinAsignar.map { "Ped. #${it.id_pedido} - ${it.cliente?.nombre ?: "N/A"}" })
        }
        assignRow.addView(spPedidos)
        assignRow.addView(Button(this).apply {
            text = "Asignar"
            setOnClickListener {
                if (pedidosSinAsignar.isNotEmpty()) {
                    val selectedPedido = pedidosSinAsignar[spPedidos.selectedItemPosition]
                    asignarPedido(rep.id_usuario, selectedPedido.id_pedido)
                }
            }
        })
        container.addView(assignRow)

        // Pedidos asignados list
        container.addView(TextView(this).apply {
            text = "Pedidos Asignados (${pedidos.size})"
            textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, theme))
        })
        for (p in pedidos) {
            val pedidoCard = com.google.android.material.card.MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
                radius = 8f; cardElevation = 2f; setContentPadding(12, 12, 12, 12)
            }
            val pedidoContent = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            pedidoContent.addView(TextView(this).apply {
                text = "Pedido #${p.id_pedido} - ${p.cliente?.nombre ?: "N/A"}"
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            pedidoContent.addView(TextView(this).apply {
                text = "Total: ${"$"}${String.format("%,.2f", p.total)} | Estado: ${p.estado}"
                textSize = 13f
                setTextColor(resources.getColor(R.color.text_secondary, theme))
            })
            if (p.estado != "ENTREGADO" && p.estado != "CANCELADO") {
                pedidoContent.addView(Button(this).apply {
                    text = "Desasignar"
                    setTextColor(resources.getColor(R.color.error_text, theme))
                    setOnClickListener { desasignarPedido(p.id_pedido, rep) }
                })
            }
            pedidoCard.addView(pedidoContent)
            container.addView(pedidoCard)
        }

        AlertDialog.Builder(this)
            .setTitle("Detalle Repartidor")
            .setView(scrollView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun asignarPedido(repartidorId: Int, pedidoId: Int) {
        val request = AsignarPedidoRequest(pedidoId)
        ApiClient.instance.asignarPedido(repartidorId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RepartidoresAdminActivity, "Pedido asignado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@RepartidoresAdminActivity, "Error al asignar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RepartidoresAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun desasignarPedido(pedidoId: Int, rep: RepartidorListado) {
        ApiClient.instance.desasignarPedido(pedidoId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RepartidoresAdminActivity, "Pedido desasignado", Toast.LENGTH_SHORT).show()
                    verDetalle(rep)
                } else {
                    Toast.makeText(this@RepartidoresAdminActivity, "Error al desasignar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RepartidoresAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
