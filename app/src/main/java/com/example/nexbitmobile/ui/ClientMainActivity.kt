package com.example.nexbitmobile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.OnBackPressedCallback
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.util.SecurePrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class ClientMainActivity : AppCompatActivity() {

    private lateinit var contentContainer: FrameLayout
    private lateinit var toolbarSub: View
    private lateinit var toolbarDivider: View
    private lateinit var btnToolbarBack: ImageButton
    private lateinit var tvToolbarTitle: TextView

    private val navIcons = mutableListOf<ImageView>()
    private var currentNavTab = "catalogo"

    private val navStack = mutableListOf<String>()
    private var currentScreen = "catalogo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main)

        contentContainer = findViewById(R.id.contentContainer)
        toolbarSub = findViewById(R.id.toolbarSub)
        toolbarDivider = findViewById(R.id.toolbarDivider)
        btnToolbarBack = findViewById(R.id.btnToolbarBack)
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)

        setupNavItems()
        btnToolbarBack.setOnClickListener { goBack() }

        showCatalogo()
        updateNavSelection("catalogo")

        val openScreen = intent.getStringExtra("open_screen")
        if (openScreen == "mispedidos") {
            showInlineScreen("mispedidos")
            currentNavTab = "pedidos"
            currentScreen = "pedidos"
            updateNavSelection("pedidos")
        }
    }

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navStack.isNotEmpty()) { goBack(); return }
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
        }
    }

    init {
        onBackPressedDispatcher.addCallback(this, backCallback)
    }

    // ──────────── 4-BUTTON BOTTOM NAV (ICONS ONLY) ────────────

    private fun setupNavItems() {
        val configs = listOf(
            Triple(R.id.navHome, R.id.ivNavHome, "catalogo"),
            Triple(R.id.navCart, R.id.ivNavCart, "carrito"),
            Triple(R.id.navOrders, R.id.ivNavOrders, "pedidos"),
            Triple(R.id.navProfile, R.id.ivNavProfile, "perfil")
        )

        navIcons.clear()
        for ((containerId, iconId, key) in configs) {
            val icon = findViewById<ImageView>(iconId)
            navIcons.add(icon)
            findViewById<View>(containerId).setOnClickListener {
                navStack.clear()
                toolbarSub.visibility = View.GONE
                toolbarDivider.visibility = View.GONE
                when (key) {
                    "catalogo" -> showCatalogo()
                    "carrito" -> showInlineScreen("carrito")
                    "pedidos" -> showInlineScreen("mispedidos")
                    "perfil" -> showProfile()
                }
                currentNavTab = key
                currentScreen = key
                updateNavSelection(key)
            }
        }
    }

    private fun updateNavSelection(selected: String) {
        val keys = listOf("catalogo", "carrito", "pedidos", "perfil")
        val activeColor = resources.getColor(R.color.nav_active, theme)
        val inactiveColor = resources.getColor(R.color.nav_inactive, theme)
        navIcons.forEachIndexed { i, icon ->
            icon.imageTintList = android.content.res.ColorStateList.valueOf(
                if (keys[i] == selected) activeColor else inactiveColor
            )
        }
    }

    // ──────────── INLINE NAVIGATION ────────────

    private fun showInlineScreen(screenKey: String) {
        navStack.add(currentScreen)
        currentScreen = screenKey
        toolbarSub.visibility = View.VISIBLE
        toolbarDivider.visibility = View.VISIBLE
        contentContainer.removeAllViews()

        when (screenKey) {
            "carrito" -> {
                tvToolbarTitle.text = getString(R.string.mi_carrito)
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_carrito, contentContainer, false
                )
                contentContainer.addView(v); showCarritoInline(v)
            }
            "mispedidos" -> {
                val prefs = getSharedPreferences("app", MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)
                tvToolbarTitle.text = getString(R.string.mis_pedidos)
                if (userId == 0) {
                    val v = LayoutInflater.from(this).inflate(
                        R.layout.fragment_pedidos_invitado, contentContainer, false
                    )
                    contentContainer.addView(v)
                    v.findViewById<View>(R.id.btnGuestLoginRedirect).setOnClickListener {
                        showProfile()
                        currentNavTab = "perfil"
                        currentScreen = "perfil"
                        updateNavSelection("perfil")
                    }
                } else {
                    val v = LayoutInflater.from(this).inflate(
                        R.layout.inline_mispedidos, contentContainer, false
                    )
                    contentContainer.addView(v); showMisPedidosInline(v)
                }
            }
        }
    }

    private fun goBack() {
        if (navStack.isEmpty()) return
        val prev = navStack.removeAt(navStack.size - 1)
        currentScreen = prev
        if (navStack.isEmpty()) {
            toolbarSub.visibility = View.GONE
            toolbarDivider.visibility = View.GONE
        }
        when (prev) {
            "catalogo" -> showCatalogo()
            "perfil" -> showProfile()
            else -> showCatalogo()
        }
    }

    // ──────────── CATÁLOGO DE PRODUCTOS ────────────

    private fun showCatalogo() {
        contentContainer.removeAllViews()
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(0xFFF5F5F7.toInt())
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(container)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val name = prefs.getString("userName", "Cliente") ?: "Cliente"
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val userId = prefs.getInt("userId", 0)

        // Centered triangle logo header
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(20, 24, 20, 8) }
        }
        headerRow.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(48.dpToPx(), 48.dpToPx())
            setImageResource(R.drawable.ic_logo_triangle)
            scaleType = ImageView.ScaleType.FIT_CENTER
        })
        container.addView(headerRow)

        // Loading
        val progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                48.dpToPx(), 48.dpToPx()
            ).apply { gravity = android.view.Gravity.CENTER; topMargin = 32 }
        }
        container.addView(progressBar)

        ApiClient.instance.getProductosPublico().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, res: Response<List<Producto>>) {
                container.removeView(progressBar)
                if (res.isSuccessful) {
                    val productos = res.body() ?: emptyList()
                    for (p in productos) {
                        container.addView(createProductCard(p, formatter, userId))
                    }
                    if (productos.isEmpty()) {
                        container.addView(TextView(this@ClientMainActivity).apply {
                            text = getString(R.string.no_hay_productos)
                            textSize = 14f
                            setTextColor(0xFF86868B.toInt())
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(16, 32, 16, 0); gravity = android.view.Gravity.CENTER }
                        })
                    }
                }
            }
            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                container.removeView(progressBar)
                container.addView(TextView(this@ClientMainActivity).apply {
                    text = getString(R.string.error_cargar_productos)
                    textSize = 14f
                    setTextColor(0xFFEF4444.toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 32, 16, 0); gravity = android.view.Gravity.CENTER }
                })
            }
        })

        contentContainer.addView(scrollView)
    }

    private fun createProductCard(p: Producto, fmt: NumberFormat, userId: Int): View {
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 0, 16, 12) }
            radius = 12f
            cardElevation = 2f
            setCardBackgroundColor(0xFFFFFFFF.toInt())
            strokeWidth = 0
            setContentPadding(12, 12, 12, 12)
            setOnClickListener {
                val intent = Intent(this@ClientMainActivity, ProductDetailActivity::class.java)
                intent.putExtra("id_producto", p.id_producto)
                startActivity(intent)
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        // Image 105dp x 105dp
        val ivProduct = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(105.dpToPx(), 105.dpToPx())
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        if (!p.imagen_url.isNullOrEmpty()) {
            Glide.with(this).load(p.imagen_url).placeholder(R.drawable.ic_placeholder).into(ivProduct)
        } else {
            ivProduct.setImageResource(R.drawable.ic_placeholder)
        }
        root.addView(ivProduct)

        // Center column: name, price, category
        val centerCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { setMargins(12, 0, 8, 0) }
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        centerCol.addView(TextView(this).apply {
            text = p.nombre
            textSize = 17f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF1D1D1F.toInt())
        })
        centerCol.addView(TextView(this).apply {
            text = fmt.format(p.precio_venta)
            textSize = 19f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF1D1D1F.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        })
        centerCol.addView(TextView(this).apply {
            text = p.categoria_nombre ?: "General"
            textSize = 13f
            setTextColor(0xFF636366.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        })
        root.addView(centerCol)

        // Right: cart button centered vertically
        val rightCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        val btnAddCart = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx())
            setImageResource(R.drawable.ic_cart_add_premium)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                if (userId == 0) {
                    Toast.makeText(this@ClientMainActivity, getString(R.string.inicia_sesion_carrito), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val carritoReq = CarritoAddRequest(
                    producto_id = p.id_producto,
                    cantidad = 1,
                    usuario_id = userId,
                    session_id = null
                )
                ApiClient.instance.addToCarrito(carritoReq).enqueue(object : Callback<List<CarritoItem>> {
                    override fun onResponse(call: Call<List<CarritoItem>>, res: Response<List<CarritoItem>>) {
                        if (res.isSuccessful) {
                            Toast.makeText(this@ClientMainActivity, getString(R.string.agregado_carrito), Toast.LENGTH_SHORT).show()
                            updateCartBadge()
                        } else {
                            Toast.makeText(this@ClientMainActivity, getString(R.string.error_agregar), Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                        Toast.makeText(this@ClientMainActivity, getString(R.string.error_conexion), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
        rightCol.addView(btnAddCart)
        root.addView(rightCol)

        card.addView(root)
        return card
    }

    private fun updateCartBadge() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        if (userId == 0) return
        val badge = findViewById<TextView>(R.id.badgeCart)
        ApiClient.instance.getCarrito(userId).enqueue(object : Callback<List<CarritoItem>> {
            override fun onResponse(call: Call<List<CarritoItem>>, res: Response<List<CarritoItem>>) {
                if (res.isSuccessful) {
                    val items = res.body() ?: emptyList()
                    val count = items.sumOf { it.cantidad }
                    if (count > 0) {
                        badge.text = count.toString()
                        badge.visibility = View.VISIBLE
                    } else {
                        badge.visibility = View.GONE
                    }
                }
            }
            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {}
        })
    }

    // ──────────── CARRITO INLINE ────────────

    private fun showCarritoInline(root: View) {
        val rvCarrito = root.findViewById<RecyclerView>(R.id.rvCarrito)
        val progressBar = root.findViewById<ProgressBar>(R.id.progressBar)
        val llEmpty = root.findViewById<LinearLayout>(R.id.llEmpty)
        val llSummary = root.findViewById<LinearLayout>(R.id.llSummary)
        val tvItemCount = root.findViewById<TextView>(R.id.tvItemCount)
        val tvTotal = root.findViewById<TextView>(R.id.tvTotal)
        val tvTotalAmount = root.findViewById<TextView>(R.id.tvTotalAmount)
        val btnCheckout = root.findViewById<TextView>(R.id.btnCheckout)

        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val token = SecurePrefs.getToken(this) ?: ""
        var cartTotal = 0.0

        val cargarCarrito: (List<CarritoItem>) -> Unit = { items ->
            if (items.isEmpty()) {
                cartTotal = 0.0
                llEmpty.visibility = View.VISIBLE
                rvCarrito.visibility = View.GONE
                llSummary.visibility = View.GONE
            } else {
                llEmpty.visibility = View.GONE
                rvCarrito.visibility = View.VISIBLE
                llSummary.visibility = View.VISIBLE
                val subtotalTotal = items.sumOf { it.subtotal }
                val totalQty = items.sumOf { it.cantidad }
                tvItemCount.text = "Productos"
                tvTotal.text = "$totalQty"
                tvTotalAmount.text = formatter.format(subtotalTotal)
                cartTotal = subtotalTotal
                rvCarrito.adapter?.let { (it as CarritoAdapter).updateList(items) }
            }
        }

        val adapter = CarritoAdapter(
            items = emptyList(),
            onQuantityChange = { item, newQty ->
                val req = CarritoUpdateRequest(
                    cantidad = newQty,
                    usuario_id = userId,
                    session_id = null
                )
                ApiClient.instance.updateCarritoItem(item.id_carrito, req)
                    .enqueue(object : Callback<List<CarritoItem>> {
                        override fun onResponse(call: Call<List<CarritoItem>>, res: Response<List<CarritoItem>>) {
                            if (res.isSuccessful) cargarCarrito(res.body() ?: emptyList())
                            else Toast.makeText(this@ClientMainActivity, getString(R.string.error_actualizar_cantidad), Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                            Toast.makeText(this@ClientMainActivity, getString(R.string.error_conexion), Toast.LENGTH_SHORT).show()
                        }
                    })
            },
            onRemove = { item ->
                ApiClient.instance.removeFromCarrito(item.producto_id, userId)
                    .enqueue(object : Callback<List<CarritoItem>> {
                        override fun onResponse(call: Call<List<CarritoItem>>, res: Response<List<CarritoItem>>) {
                            if (res.isSuccessful) {
                                cargarCarrito(res.body() ?: emptyList())
                                Toast.makeText(this@ClientMainActivity, getString(R.string.producto_eliminado), Toast.LENGTH_SHORT).show()
                            } else Toast.makeText(this@ClientMainActivity, getString(R.string.error_eliminar_item), Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                            Toast.makeText(this@ClientMainActivity, getString(R.string.error_conexion), Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        )
        rvCarrito.layoutManager = LinearLayoutManager(this)
        rvCarrito.adapter = adapter

        fun loadCart() {
            if (userId == 0 || token.isEmpty()) {
                llEmpty.visibility = View.VISIBLE
                rvCarrito.visibility = View.GONE
                llSummary.visibility = View.GONE
                return
            }
            progressBar.visibility = View.VISIBLE
            ApiClient.instance.getCarrito(userId).enqueue(object : Callback<List<CarritoItem>> {
                override fun onResponse(call: Call<List<CarritoItem>>, res: Response<List<CarritoItem>>) {
                    progressBar.visibility = View.GONE
                    if (res.isSuccessful) cargarCarrito(res.body() ?: emptyList())
                    else Toast.makeText(this@ClientMainActivity, getString(R.string.error_cargando_carrito, res.code()), Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ClientMainActivity, getString(R.string.sin_conexion_servidor), Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnCheckout.setOnClickListener {
            if (cartTotal <= 0.0) {
                Toast.makeText(this, getString(R.string.carrito_vacio), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val direccion = prefs.getString("userAddress", "Sin especificar") ?: "Sin especificar"
            progressBar.visibility = View.VISIBLE
            ApiClient.instance.checkout(CheckoutRequest(
                usuario_id = userId,
                direccion_entrega = direccion
            )).enqueue(object : Callback<CheckoutResponse> {
                override fun onResponse(call: Call<CheckoutResponse>, res: Response<CheckoutResponse>) {
                    progressBar.visibility = View.GONE
                    if (res.isSuccessful) {
                        val pedidoId = res.body()?.id_pedido
                        val intent = Intent(this@ClientMainActivity, PagoTransferenciaActivity::class.java)
                        intent.putExtra(PagoTransferenciaActivity.EXTRA_PEDIDO_ID, pedidoId ?: 0)
                        intent.putExtra(PagoTransferenciaActivity.EXTRA_TOTAL, cartTotal)
                        startActivity(intent)
                    } else {
                        val errorMsg = res.errorBody()?.string() ?: getString(R.string.error_desconocido)
                        Toast.makeText(this@ClientMainActivity, getString(R.string.error_msg, errorMsg), Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ClientMainActivity, getString(R.string.error_conexion_msg, t.message), Toast.LENGTH_SHORT).show()
                }
            })
        }

        loadCart()
    }

    // ──────────── MIS PEDIDOS INLINE ────────────

    private fun showMisPedidosInline(root: View) {
        val rvPedidos = root.findViewById<RecyclerView>(R.id.rvMisPedidos)
        val progressBar = root.findViewById<ProgressBar>(R.id.progressBar)
        val llEmpty = root.findViewById<LinearLayout>(R.id.llEmpty)

        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)

        rvPedidos.layoutManager = LinearLayoutManager(this)

        lateinit var cargarPedidos: () -> Unit

        val cancelarPedido: (Int) -> Unit = { pedidoId ->
            ApiClient.instance.cancelarPedido(pedidoId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, res: Response<Void>) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@ClientMainActivity, getString(R.string.pedido_cancelado), Toast.LENGTH_SHORT).show()
                        cargarPedidos()
                    } else {
                        val msg = res.errorBody()?.string() ?: getString(R.string.error_al_cancelar)
                        Toast.makeText(this@ClientMainActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ClientMainActivity, getString(R.string.error_conexion), Toast.LENGTH_SHORT).show()
                }
            })
        }

        cargarPedidos = {
            if (userId == 0) {
                rvPedidos.visibility = View.GONE
                llEmpty.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.VISIBLE
                llEmpty.visibility = View.GONE
                ApiClient.instance.getMisPedidos(userId).enqueue(object : Callback<List<Pedido>> {
                    override fun onResponse(call: Call<List<Pedido>>, res: Response<List<Pedido>>) {
                        progressBar.visibility = View.GONE
                        if (res.isSuccessful) {
                            val pedidos = res.body() ?: emptyList()
                            if (pedidos.isEmpty()) {
                                rvPedidos.visibility = View.GONE
                                llEmpty.visibility = View.VISIBLE
                            } else {
                                llEmpty.visibility = View.GONE
                                rvPedidos.visibility = View.VISIBLE
                                rvPedidos.adapter = MisPedidosAdapter(pedidos, formatter,
                                    onCancelar = { pedido ->
                                        AlertDialog.Builder(this@ClientMainActivity)
                                            .setTitle("${getString(R.string.cancelar_pedido)} #${pedido.id_pedido}")
                                            .setMessage("¿Estás seguro de que deseas cancelar este pedido?\nTotal: ${formatter.format(pedido.total)}")
                                            .setPositiveButton(getString(R.string.si_cancelar)) { _, _ -> cancelarPedido(pedido.id_pedido) }
                                            .setNegativeButton(getString(R.string.no), null)
                                            .show()
                                    },
                                    onDetalle = { pedido ->
                                        val dialogView = LayoutInflater.from(this@ClientMainActivity)
                                            .inflate(R.layout.dialog_detalle_pedido, null)
                                        val dialog = android.app.Dialog(this@ClientMainActivity, android.R.style.ThemeOverlay_Material_Dialog)
                                        dialog.setContentView(dialogView)
                                        dialog.window?.setLayout(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                                        val titleTv = dialogView.findViewById<TextView>(R.id.tvDetalleTitle)
                                        val cerrarBtn = dialogView.findViewById<ImageButton>(R.id.btnCerrarDetalle)
                                        val ticketBtn = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDescargarTicket)
                                        val clienteTv = dialogView.findViewById<TextView>(R.id.tvClienteValue)
                                        val fechaTv = dialogView.findViewById<TextView>(R.id.tvFechaValue)
                                        val estadoBadge = dialogView.findViewById<TextView>(R.id.tvEstadoBadge)
                                        val totalTv = dialogView.findViewById<TextView>(R.id.tvTotalValue)
                                        val productosContainer = dialogView.findViewById<LinearLayout>(R.id.llProductosDetalle)
                                        val accionesContainer = dialogView.findViewById<LinearLayout>(R.id.llAccionesDetalle)

                                        titleTv.text = "Pedido #${String.format(Locale.US, "%06d", pedido.id_pedido)}"
                                        cerrarBtn.setOnClickListener { dialog.dismiss() }
                                        ticketBtn.setOnClickListener { onTicketClick(pedido); dialog.dismiss() }
                                        clienteTv.text = pedido.usuario_nombre ?: "Cliente"
                                        val rawFecha = pedido.fecha ?: pedido.fecha_pedido ?: ""
                                        fechaTv.text = rawFecha.take(16).replace("T", " ")
                                        totalTv.text = formatter.format(pedido.total)

                                        val badgeBg = when (pedido.estado) {
                                            "PENDIENTE" -> R.drawable.bg_badge_info
                                            "CONFIRMADO" -> R.drawable.bg_badge_confirmed
                                            "ASIGNADO" -> R.drawable.bg_badge_assigned
                                            "EN_CAMINO" -> R.drawable.bg_badge_info
                                            "ENTREGADO" -> R.drawable.bg_badge_success
                                            "CANCELADO" -> R.drawable.bg_badge_cancelled
                                            else -> R.drawable.bg_chip
                                        }
                                        val badgeColor = when (pedido.estado) {
                                            "PENDIENTE" -> "#F59E0B"
                                            "CONFIRMADO" -> "#3B82F6"
                                            "ASIGNADO" -> "#3F51B5"
                                            "EN_CAMINO" -> "#F97316"
                                            "ENTREGADO" -> "#2E7D32"
                                            "CANCELADO" -> "#EF4444"
                                            else -> "#64748B"
                                        }
                                        estadoBadge.text = pedido.estado.replace("_", " ")
                                        estadoBadge.setBackgroundResource(badgeBg)
                                        estadoBadge.setTextColor(android.graphics.Color.parseColor(badgeColor))

                                        val detalles = pedido.detalles
                                        if (!detalles.isNullOrEmpty()) {
                                            for (d in detalles) {
                                                val card = LinearLayout(this@ClientMainActivity).apply {
                                                    orientation = LinearLayout.HORIZONTAL
                                                    gravity = android.view.Gravity.CENTER_VERTICAL
                                                    layoutParams = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                    ).apply { setMargins(0, 0, 0, 8) }
                                                    setPadding(12, 12, 12, 12)
                                                    setBackgroundResource(android.R.color.white)
                                                    elevation = 1f
                                                    outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
                                                    clipToOutline = true
                                                }

                                                val imgContainer = ImageView(this@ClientMainActivity).apply {
                                                    layoutParams = LinearLayout.LayoutParams(50.dpToPx(), 50.dpToPx())
                                                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                                                    setImageResource(R.drawable.ic_package)
                                                    setColorFilter(0xFF9CA3AF.toInt())
                                                    setBackgroundColor(0xFFF3F4F6.toInt())
                                                }
                                                card.addView(imgContainer)

                                                val centerCol = LinearLayout(this@ClientMainActivity).apply {
                                                    orientation = LinearLayout.VERTICAL
                                                    layoutParams = LinearLayout.LayoutParams(
                                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                                    ).apply { setMargins(12, 0, 8, 0) }
                                                }
                                                centerCol.addView(TextView(this@ClientMainActivity).apply {
                                                    text = d.producto_nombre ?: "Producto"
                                                    textSize = 15f
                                                    setTypeface(null, android.graphics.Typeface.BOLD)
                                                    setTextColor(0xFF1C1C1E.toInt())
                                                })
                                                centerCol.addView(TextView(this@ClientMainActivity).apply {
                                                    text = "Cantidad: ${d.cantidad}  •  Precio unitario: ${formatter.format(d.precio_unitario)}"
                                                    textSize = 12f
                                                    setTextColor(0xFF666666.toInt())
                                                    layoutParams = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                    ).apply { topMargin = 4 }
                                                })
                                                card.addView(centerCol)

                                                card.addView(TextView(this@ClientMainActivity).apply {
                                                    text = formatter.format(d.subtotal)
                                                    textSize = 15f
                                                    setTypeface(null, android.graphics.Typeface.BOLD)
                                                    setTextColor(0xFF1C1C1E.toInt())
                                                    layoutParams = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                    )
                                                })

                                                productosContainer.addView(card)
                                            }
                                        }

                                        // Action buttons based on estado
                                        if (pedido.estado == "PENDIENTE") {
                                            com.google.android.material.button.MaterialButton(this@ClientMainActivity).apply {
                                                layoutParams = LinearLayout.LayoutParams(0, 36.dpToPx(), 1f).also { it.setMargins(0, 0, 4, 0) }
                                                text = "Cancelar pedido"
                                                textSize = 12f
                                                setTextColor(0xFFEF4444.toInt())
                                                setAllCaps(false)
                                                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                                                strokeColor = ColorStateList.valueOf(Color.parseColor("#FECACA"))
                                                strokeWidth = 1
                                                cornerRadius = 8.dpToPx()
                                                stateListAnimator = null
                                                setOnClickListener {
                                                    dialog.dismiss()
                                                    AlertDialog.Builder(this@ClientMainActivity)
                                                        .setTitle("${getString(R.string.cancelar_pedido)} #${pedido.id_pedido}")
                                                        .setMessage("¿Estás seguro de que deseas cancelar este pedido?\nTotal: ${formatter.format(pedido.total)}")
                                                        .setPositiveButton(getString(R.string.si_cancelar)) { _, _ -> cancelarPedido(pedido.id_pedido) }
                                                        .setNegativeButton(getString(R.string.no), null)
                                                        .show()
                                                }
                                                accionesContainer.addView(this)
                                            }
                                        }
                                        if (pedido.estado == "PENDIENTE" || pedido.estado == "CONFIRMADO") {
                                            com.google.android.material.button.MaterialButton(this@ClientMainActivity).apply {
                                                layoutParams = LinearLayout.LayoutParams(0, 36.dpToPx(), 1f).also { it.setMargins(4, 0, 4, 0) }
                                                text = "Subir comprobante"
                                                textSize = 12f
                                                setTextColor(0xFF3B82F6.toInt())
                                                setAllCaps(false)
                                                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DBEAFE"))
                                                strokeColor = ColorStateList.valueOf(Color.parseColor("#BFDBFE"))
                                                strokeWidth = 1
                                                cornerRadius = 8.dpToPx()
                                                stateListAnimator = null
                                                setOnClickListener {
                                                    dialog.dismiss()
                                                    val intent = Intent(this@ClientMainActivity, ConfirmarPedidoActivity::class.java)
                                                    intent.putExtra("pedido_id", pedido.id_pedido)
                                                    startActivity(intent)
                                                }
                                                accionesContainer.addView(this)
                                            }
                                        }
                                        if (pedido.estado != "CANCELADO" && pedido.estado != "ENTREGADO") {
                                            com.google.android.material.button.MaterialButton(this@ClientMainActivity).apply {
                                                layoutParams = LinearLayout.LayoutParams(0, 36.dpToPx(), 1f).also { it.setMargins(4, 0, 0, 0) }
                                                text = "Chat"
                                                textSize = 12f
                                                setTextColor(0xFF1C1C1E.toInt())
                                                setAllCaps(false)
                                                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F2F2F7"))
                                                strokeColor = ColorStateList.valueOf(Color.parseColor("#D1D5DB"))
                                                strokeWidth = 1
                                                cornerRadius = 8.dpToPx()
                                                stateListAnimator = null
                                                setOnClickListener {
                                                    dialog.dismiss()
                                                    val intent = Intent(this@ClientMainActivity, ChatActivity::class.java)
                                                    intent.putExtra("pedido_id", pedido.id_pedido)
                                                    startActivity(intent)
                                                }
                                                accionesContainer.addView(this)
                                            }
                                        }

                                        dialog.show()
                                    },
                                    onChat = { pedidoId ->
                                        val intent = Intent(this@ClientMainActivity, ChatActivity::class.java).apply {
                                            putExtra("pedidoId", pedidoId)
                                        }
                                        startActivity(intent)
                                    },
                                    onTicket = { pedido -> onTicketClick(pedido) }
                                )
                            }
                        } else {
                            Toast.makeText(this@ClientMainActivity, getString(R.string.error_cargar_pedidos, res.code()), Toast.LENGTH_SHORT).show()
                            rvPedidos.visibility = View.GONE
                            llEmpty.visibility = View.VISIBLE
                        }
                    }
                    override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@ClientMainActivity, getString(R.string.sin_conexion_msg, t.message), Toast.LENGTH_SHORT).show()
                        rvPedidos.visibility = View.GONE
                        llEmpty.visibility = View.VISIBLE
                    }
                })
            }
        }

        cargarPedidos()
    }

    private var ticketWebView: WebView? = null

    private fun onTicketClick(pedido: Pedido) {
        ApiClient.instance.getPedidoTicket(pedido.id_pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { pedidoTicket ->
                        generarHtmlYPdf(pedidoTicket)
                    } ?: run {
                        Toast.makeText(this@ClientMainActivity, getString(R.string.error_obtener_ticket), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ClientMainActivity, getString(R.string.error_obtener_ticket), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Log.e("ClientMain", "Ticket download failed", t)
                Toast.makeText(this@ClientMainActivity, getString(R.string.fallo_conexion), Toast.LENGTH_SHORT).show()
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
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.precio_unitario}</td>
                    <td style="padding:8px 12px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.subtotal}</td>
                </tr>
                """
            }
        } else {
            "<tr><td colspan='4' style='padding:12px;text-align:center;color:#94a3b8;'>Sin productos detallados</td></tr>"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8"/>
                <title>Ticket - #${pedido.id_pedido}</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Inter', sans-serif; background: #e2e8f0; padding: 40px 20px; color: #1e293b; }
                    .ticket { max-width: 600px; margin: 0 auto; background: #fff; border-radius: 4px; padding: 32px; }
                    .header { display: flex; justify-content: space-between; margin-bottom: 24px; }
                    .brand { font-size: 1.75rem; font-weight: 700; color: #0f172a; }
                    .order-id { font-size: 1.25rem; font-weight: 700; color: #0f172a; }
                    table { width: 100%; border-collapse: collapse; margin: 24px 0; }
                    th { text-align: left; padding: 12px; border-bottom: 2px solid #e2e8f0; font-size: 0.8rem; color: #64748b; text-transform: uppercase; }
                    td { padding: 12px; border-bottom: 1px solid #f1f5f9; }
                    .total { text-align: right; font-size: 1.5rem; font-weight: 700; margin-top: 16px; }
                </style>
            </head>
            <body>
                <div class="ticket">
                    <div class="header"><div class="brand">Nexbit</div><div class="order-id">#${String.format(Locale.US, "%06d", pedido.id_pedido)}</div></div>
                    <p><strong>Cliente:</strong> ${pedido.usuario_nombre ?: "N/A"}</p>
                    <p><strong>Fecha:</strong> ${pedido.fecha ?: pedido.fecha_pedido ?: "N/A"}</p>
                    <p><strong>Estado:</strong> ${pedido.estado}</p>
                    <table><thead><tr><th>Producto</th><th>Cant</th><th>Precio</th><th>Subtotal</th></tr></thead><tbody>$filasProductos</tbody></table>
                    <div class="total">Total: $${pedido.total}</div>
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
                ticketWebView = null
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
        ticketWebView = webView
    }

    private fun createWebPrintJob(webView: WebView) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter("Pedido_Nexbit")
        printManager.print("Nexbit Ticket", printAdapter, PrintAttributes.Builder().build())
    }

    // ──────────── PERFIL + CONFIGURACIÓN ────────────

    private var isProfileEditing = false

    private fun showProfile() {
        contentContainer.removeAllViews()
        val root = layoutInflater.inflate(R.layout.fragment_profile_client, contentContainer, false)
        contentContainer.addView(root)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val isLoggedIn = userId != 0
        val userName = prefs.getString("userName", "") ?: ""
        val userEmail = prefs.getString("userEmail", "") ?: ""

        // ── Views ──
        val ivAvatar = root.findViewById<ImageView>(R.id.ivProfileAvatar)
        val tvAvatarInitial = root.findViewById<TextView>(R.id.tvAvatarInitial)
        val tvName = root.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = root.findViewById<TextView>(R.id.tvProfileEmail)
        val tvStatusBadge = root.findViewById<TextView>(R.id.tvStatusBadge)

        val etPhone = root.findViewById<EditText>(R.id.etProfilePhone)
        val etAddress = root.findViewById<EditText>(R.id.etProfileAddress)

        val btnSave = root.findViewById<Button>(R.id.btnSaveProfile)

        val rowMisPedidos = root.findViewById<View>(R.id.rowMisPedidos)
        val rowFacturas = root.findViewById<View>(R.id.rowFacturas)
        val rowAyuda = root.findViewById<View>(R.id.rowAyuda)
        val rowContacto = root.findViewById<View>(R.id.rowContacto)

        val switchLang = root.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchLang)
        val switchNotif = root.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchNotifications)
        val tvLangLabel = root.findViewById<TextView>(R.id.tvLangLabel)
        val tvLangSub = root.findViewById<TextView>(R.id.tvLangSub)

        val guestCta = root.findViewById<View>(R.id.guestCtaContainer)
        val btnGuestLogin = root.findViewById<Button>(R.id.btnGuestLogin)
        val tvGuestRegister = root.findViewById<TextView>(R.id.tvGuestRegister)
        val btnLogout = root.findViewById<Button>(R.id.btnLogout)

        if (isLoggedIn) {
            // ── Logged-in client state ──
            tvName.text = userName.ifEmpty { "Cliente" }
            tvEmail.text = userEmail
            tvStatusBadge.text = "● Conectado"
            tvStatusBadge.visibility = View.VISIBLE

            if (userName.isNotEmpty()) {
                tvAvatarInitial.text = userName.first().uppercase()
                tvAvatarInitial.visibility = View.VISIBLE
            }

            val avatarUrl = prefs.getString("userAvatar", "") ?: ""
            if (avatarUrl.isNotEmpty()) {
                Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar)
                tvAvatarInitial.visibility = View.GONE
            }

            etPhone.setText(prefs.getString("userPhone", ""))
            etAddress.setText(prefs.getString("userAddress", ""))

            guestCta.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE

            // Load remote data
            ApiClient.instance.getUsuario(userId).enqueue(object : Callback<Usuario> {
                override fun onResponse(c: Call<Usuario>, res: Response<Usuario>) {
                    if (res.isSuccessful) {
                        val u = res.body() ?: return
                        tvName.text = u.nombre
                        tvEmail.text = u.email
                        etPhone.setText(u.telefono ?: "")
                        etAddress.setText(u.direccion ?: "")
                        prefs.edit()
                            .putString("userName", u.nombre)
                            .putString("userEmail", u.email)
                            .putString("userPhone", u.telefono)
                            .putString("userAddress", u.direccion)
                            .apply()
                    }
                }
                override fun onFailure(c: Call<Usuario>, t: Throwable) {}
            })

            // Edit / Save toggle
            btnSave.visibility = View.VISIBLE
            btnSave.text = getString(R.string.editar_datos)
            btnSave.setOnClickListener {
                if (!isProfileEditing) {
                    isProfileEditing = true
                    btnSave.text = getString(R.string.guardar)
                    etPhone.isEnabled = true
                    etPhone.isClickable = true
                    etPhone.isFocusable = true
                    etPhone.isFocusableInTouchMode = true
                    etAddress.isEnabled = true
                    etAddress.isClickable = true
                    etAddress.isFocusable = true
                    etAddress.isFocusableInTouchMode = true
                } else {
                    val newPhone = etPhone.text.toString().trim()
                    val newAddress = etAddress.text.toString().trim()
                    ApiClient.instance.updateUsuario(userId, UsuarioUpdateRequest(
                        telefono = newPhone.ifEmpty { null },
                        direccion = newAddress.ifEmpty { null }
                    )).enqueue(object : Callback<Usuario> {
                        override fun onResponse(c: Call<Usuario>, res: Response<Usuario>) {
                            if (res.isSuccessful) {
                                Toast.makeText(this@ClientMainActivity, getString(R.string.perfil_actualizado), Toast.LENGTH_SHORT).show()
                                prefs.edit()
                                    .putString("userPhone", newPhone)
                                    .putString("userAddress", newAddress)
                                    .apply()
                            } else {
                                Toast.makeText(this@ClientMainActivity, getString(R.string.error_perfil, res.code()), Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(c: Call<Usuario>, t: Throwable) {
                            Toast.makeText(this@ClientMainActivity, getString(R.string.error_conexion), Toast.LENGTH_SHORT).show()
                        }
                    })
                    isProfileEditing = false
                    btnSave.text = getString(R.string.editar_datos)
                    etPhone.isEnabled = false
                    etPhone.isFocusable = false
                    etPhone.isFocusableInTouchMode = false
                    etAddress.isEnabled = false
                    etAddress.isFocusable = false
                    etAddress.isFocusableInTouchMode = false
                }
            }

            // Quick Access
            rowMisPedidos.setOnClickListener {
                startActivity(Intent(this, MisPedidosActivity::class.java))
            }
            rowFacturas.setOnClickListener {
                startActivity(Intent(this, MisPedidosActivity::class.java))
            }
            rowAyuda.setOnClickListener {
                startActivity(Intent(this, HelpActivity::class.java))
            }
            rowContacto.setOnClickListener {
                startActivity(Intent(this, ContactoActivity::class.java))
            }

            // Language
            val isSpanish = com.example.nexbitmobile.util.LanguageHelper.isSpanish(this)
            switchLang.isChecked = !isSpanish
            tvLangLabel.text = if (isSpanish) "Idioma / Language" else "Language / Idioma"
            tvLangSub.text = if (isSpanish) "Español" else "English"
            switchLang.setOnCheckedChangeListener { _, isChecked ->
                val newLocale = if (isChecked) "en" else "es"
                com.example.nexbitmobile.util.LanguageHelper.setLocale(this, newLocale)
                tvLangLabel.text = if (isChecked) "Language / Idioma" else "Idioma / Language"
                tvLangSub.text = if (isChecked) "English" else "Español"
            }

            // Logout
            btnLogout.setOnClickListener {
                prefs.edit().clear().apply()
                navStack.clear()
                toolbarSub.visibility = View.GONE
                toolbarDivider.visibility = View.GONE
                showCatalogo()
                currentNavTab = "catalogo"
                currentScreen = "catalogo"
                updateNavSelection("catalogo")
            }

        } else {
            // ── Guest / Pre-login state ──
            tvName.text = "Invitado"
            tvEmail.text = "Inicia sesión para acceder"
            tvStatusBadge.visibility = View.GONE

            etPhone.setText("---")
            etAddress.setText("---")

            btnSave.visibility = View.GONE
            btnLogout.visibility = View.GONE

            guestCta.visibility = View.VISIBLE
            btnGuestLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            tvGuestRegister.setOnClickListener {
                startActivity(Intent(this, RegistroActivity::class.java))
            }

            // Quick Access shows toast "Inicia sesión para acceder" on tap
            val loginToast = {
                Toast.makeText(this@ClientMainActivity, "Inicia sesión para acceder", Toast.LENGTH_SHORT).show()
            }
            listOf(rowMisPedidos, rowFacturas, rowAyuda, rowContacto).forEach { row ->
                row.setOnClickListener { loginToast() }
            }

            // Preferences disabled for guest
            switchLang.isEnabled = false
            switchNotif.isEnabled = false
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
