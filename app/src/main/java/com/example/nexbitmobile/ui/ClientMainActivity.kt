package com.example.nexbitmobile.ui

import android.content.DialogInterface
import android.content.Intent
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
import com.example.nexbitmobile.model.*
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
    }

    override fun onBackPressed() {
        if (navStack.isNotEmpty()) { goBack(); return }
        super.onBackPressed()
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
                tvToolbarTitle.text = "Mi Carrito"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_carrito, contentContainer, false
                )
                contentContainer.addView(v); showCarritoInline(v)
            }
            "mispedidos" -> {
                tvToolbarTitle.text = "Mis Pedidos"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_mispedidos, contentContainer, false
                )
                contentContainer.addView(v); showMisPedidosInline(v)
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

        // Welcome header
        container.addView(TextView(this).apply {
            text = "Hola, $name"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 20, 16, 4) }
        })
        container.addView(TextView(this).apply {
            text = "Explora nuestros productos"
            textSize = 14f
            setTextColor(0xFF9CA3AF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 0, 16, 16) }
        })

        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val userId = prefs.getInt("userId", 0)

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
                            text = "No hay productos disponibles"
                            textSize = 14f
                            setTextColor(0xFF9CA3AF.toInt())
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
                    text = "Error al cargar productos"
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
            radius = 16f
            cardElevation = 0f
            setCardBackgroundColor(0xFFFFFFFF.toInt())
            strokeWidth = 1
            strokeColor = 0xFFD1D5DB.toInt()
            setContentPadding(12, 12, 12, 12)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Product name
        root.addView(TextView(this).apply {
            text = p.nombre
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
        })

        // Horizontal content area
        val contentRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
        }

        // Image placeholder (70x70)
        val imageContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(70.dpToPx(), 70.dpToPx())
            setBackgroundColor(0xFFF3F4F6.toInt())
        }
        val ivProduct = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(70.dpToPx(), 70.dpToPx())
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        if (!p.imagen_url.isNullOrEmpty()) {
            Glide.with(this).load(p.imagen_url).placeholder(R.drawable.ic_placeholder).into(ivProduct)
        } else {
            ivProduct.setImageResource(R.drawable.ic_placeholder)
        }
        imageContainer.addView(ivProduct)
        contentRow.addView(imageContainer)

        // Center: description / "Ver ficha técnica"
        val centerCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { setMargins(12, 0, 12, 0) }
        }
        val catName = p.categoria_nombre ?: "General"
        centerCol.addView(TextView(this).apply {
            text = "Sin descripción detallada. Categoría: $catName"
            textSize = 12f
            setTextColor(0xFF9CA3AF.toInt())
        })
        centerCol.addView(TextView(this).apply {
            text = "ⓘ Ver ficha técnica"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
            setOnClickListener {
                val intent = Intent(this@ClientMainActivity, ProductDetailActivity::class.java)
                intent.putExtra("producto_id", p.id_producto)
                startActivity(intent)
            }
        })
        contentRow.addView(centerCol)

        // Right: Price + Add to cart
        val rightCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        rightCol.addView(TextView(this).apply {
            text = fmt.format(p.precio_venta)
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
        })
        // Add-to-cart button: 36dp circle, black background
        val btnAddCart = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(36.dpToPx(), 36.dpToPx())
            setImageResource(R.drawable.ic_cart)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setColorFilter(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF111827.toInt())
            setOnClickListener {
                if (userId == 0) {
                    Toast.makeText(this@ClientMainActivity, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@ClientMainActivity, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                            updateCartBadge()
                        } else {
                            Toast.makeText(this@ClientMainActivity, "Error al agregar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                        Toast.makeText(this@ClientMainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
        rightCol.addView(btnAddCart)

        contentRow.addView(rightCol)
        root.addView(contentRow)
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
        val btnCheckout = root.findViewById<TextView>(R.id.btnCheckout)

        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val token = prefs.getString("token", "") ?: ""
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
                tvItemCount.text = "${items.sumOf { it.cantidad }} artículos"
                tvTotal.text = formatter.format(items.sumOf { it.subtotal })
                cartTotal = items.sumOf { it.subtotal }
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
                            else Toast.makeText(this@ClientMainActivity, "Error actualizando cantidad", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                            Toast.makeText(this@ClientMainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    })
            },
            onRemove = { item ->
                ApiClient.instance.removeFromCarrito(item.producto_id, userId)
                    .enqueue(object : Callback<List<CarritoItem>> {
                        override fun onResponse(call: Call<List<CarritoItem>>, res: Response<List<CarritoItem>>) {
                            if (res.isSuccessful) {
                                cargarCarrito(res.body() ?: emptyList())
                                Toast.makeText(this@ClientMainActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                            } else Toast.makeText(this@ClientMainActivity, "Error eliminando item", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                            Toast.makeText(this@ClientMainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
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
                    else Toast.makeText(this@ClientMainActivity, "Error cargando carrito (${res.code()})", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ClientMainActivity, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnCheckout.setOnClickListener {
            if (cartTotal <= 0.0) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userAddress = prefs.getString("userAddress", "")
            val input = EditText(this).apply {
                hint = "Ej: Calle 45 #12-34, Bogotá"
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                maxLines = 3
                setPadding(48, 32, 48, 16)
                setText(userAddress)
            }
            AlertDialog.Builder(this)
                .setTitle("Dirección de entrega")
                .setMessage("¿Dónde entregaremos tu pedido de ${formatter.format(cartTotal)}?")
                .setView(input)
                .setPositiveButton("Confirmar Pedido") { _, _ ->
                    val direccion = input.text.toString().trim()
                    if (direccion.isEmpty()) {
                        Toast.makeText(this, "La dirección es obligatoria", Toast.LENGTH_SHORT).show()
                    } else {
                        prefs.edit().putString("userAddress", direccion).apply()
                        progressBar.visibility = View.VISIBLE
                        ApiClient.instance.checkout(CheckoutRequest(
                            usuario_id = userId,
                            direccion_entrega = direccion
                        )).enqueue(object : Callback<CheckoutResponse> {
                            override fun onResponse(call: Call<CheckoutResponse>, res: Response<CheckoutResponse>) {
                                progressBar.visibility = View.GONE
                                if (res.isSuccessful) {
                                    val pedidoId = res.body()?.id_pedido
                                    val msg = if (pedidoId != null) "Tu pedido #$pedidoId ha sido registrado con éxito.\n¡Ahora debes subir el comprobante de pago!"
                                    else "Tu pedido ha sido registrado con éxito en el sistema."
                                    AlertDialog.Builder(this@ClientMainActivity)
                                        .setTitle("Pedido Realizado!")
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("Subir Comprobante") { _, _ ->
                                            val intent = Intent(this@ClientMainActivity, ConfirmarPedidoActivity::class.java)
                                            intent.putExtra("pedido_id", pedidoId ?: 0)
                                            startActivity(intent)
                                        }
                                        .setNegativeButton("Ver Mis Pedidos") { _, _ ->
                                            showInlineScreen("mispedidos")
                                        }
                                        .show()
                                } else {
                                    val errorMsg = res.errorBody()?.string() ?: "Error desconocido"
                                    Toast.makeText(this@ClientMainActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            }
                            override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                                progressBar.visibility = View.GONE
                                Toast.makeText(this@ClientMainActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
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
                        Toast.makeText(this@ClientMainActivity, "Pedido cancelado", Toast.LENGTH_SHORT).show()
                        cargarPedidos()
                    } else {
                        val msg = res.errorBody()?.string() ?: "Error al cancelar"
                        Toast.makeText(this@ClientMainActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ClientMainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
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
                                            .setTitle("Cancelar Pedido #${pedido.id_pedido}")
                                            .setMessage("¿Estás seguro de que deseas cancelar este pedido?\nTotal: ${formatter.format(pedido.total)}")
                                            .setPositiveButton("Sí, cancelar") { _, _ -> cancelarPedido(pedido.id_pedido) }
                                            .setNegativeButton("No", null)
                                            .show()
                                    },
                                    onDetalle = { pedido ->
                                        val sb = StringBuilder()
                                        sb.append("📦 Pedido #${pedido.id_pedido}\n")
                                        sb.append("────────────────────────\n")
                                        sb.append("Estado:    ${pedido.estado}\n")
                                        sb.append("Fecha:     ${pedido.fecha.take(16).replace("T", " ")}\n")
                                        sb.append("Dirección: ${pedido.direccion_entrega ?: "No especificada"}\n")
                                        sb.append("Total:     ${formatter.format(pedido.total)}\n")
                                        val detalles = pedido.detalles
                                        if (!detalles.isNullOrEmpty()) {
                                            sb.append("\n🛍 Productos:\n")
                                            for (d in detalles) {
                                                val nombre = d.producto_nombre ?: "Producto"
                                                sb.append("  • ${d.cantidad}x $nombre → ${formatter.format(d.subtotal)}\n")
                                            }
                                        }
                                        val dialog = AlertDialog.Builder(this@ClientMainActivity)
                                            .setTitle("Detalle del Pedido")
                                            .setMessage(sb.toString())
                                            .setPositiveButton("Cerrar", null)
                                            .create()
                                        dialog.setOnShowListener {
                                            if (pedido.estado == "PENDIENTE") {
                                                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancelar Pedido") { _, _ ->
                                                    AlertDialog.Builder(this@ClientMainActivity)
                                                        .setTitle("Cancelar Pedido #${pedido.id_pedido}")
                                                        .setMessage("¿Estás seguro?")
                                                        .setPositiveButton("Sí, cancelar") { _, _ -> cancelarPedido(pedido.id_pedido) }
                                                        .setNegativeButton("No", null)
                                                        .show()
                                                }
                                            }
                                            if (pedido.estado == "PENDIENTE" || pedido.estado == "CONFIRMADO") {
                                                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Subir Comprobante") { _, _ ->
                                                    val intent = Intent(this@ClientMainActivity, ConfirmarPedidoActivity::class.java)
                                                    intent.putExtra("pedido_id", pedido.id_pedido)
                                                    startActivity(intent)
                                                }
                                            }
                                            if (pedido.estado != "CANCELADO" && pedido.estado != "ENTREGADO") {
                                                dialog.setButton(DialogInterface.BUTTON3, "Chat") { _, _ ->
                                                    val intent = Intent(this@ClientMainActivity, ChatActivity::class.java)
                                                    intent.putExtra("pedido_id", pedido.id_pedido)
                                                    startActivity(intent)
                                                }
                                            }
                                        }
                                        dialog.show()
                                    },
                                    onSubirComprobante = { pedido ->
                                        val intent = Intent(this@ClientMainActivity, ConfirmarPedidoActivity::class.java)
                                        intent.putExtra("pedido_id", pedido.id_pedido)
                                        startActivity(intent)
                                    },
                                    onReintentar = { pedido ->
                                        val intent = Intent(this@ClientMainActivity, ConfirmarPedidoActivity::class.java)
                                        intent.putExtra("pedido_id", pedido.id_pedido)
                                        startActivity(intent)
                                    },
                                    onDescargarTicket = { pedido ->
                                        Toast.makeText(this@ClientMainActivity, "Ticket: #${pedido.id_pedido}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        } else {
                            Toast.makeText(this@ClientMainActivity, "Error al cargar pedidos (${res.code()})", Toast.LENGTH_SHORT).show()
                            rvPedidos.visibility = View.GONE
                            llEmpty.visibility = View.VISIBLE
                        }
                    }
                    override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@ClientMainActivity, "Sin conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                        rvPedidos.visibility = View.GONE
                        llEmpty.visibility = View.VISIBLE
                    }
                })
            }
        }

        cargarPedidos()
    }

    // ──────────── PERFIL + CONFIGURACIÓN ────────────

    private var isProfileEditing = false

    private fun showProfile() {
        contentContainer.removeAllViews()
        val scrollView = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(container)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)

        // Avatar
        val avatarFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(64.dpToPx(), 64.dpToPx())
            setBackgroundColor(0xFFF3F4F6.toInt())
        }
        container.addView(avatarFrame.apply {
            layoutParams = (layoutParams as LinearLayout.LayoutParams).apply { gravity = android.view.Gravity.CENTER; topMargin = 24 }
        })

        val tvName = TextView(this).apply {
            id = R.id.tvProfileName
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = android.view.Gravity.CENTER; topMargin = 12 }
            text = prefs.getString("userName", "Cliente") ?: "Cliente"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
        }
        container.addView(tvName)

        val tvEmail = TextView(this).apply {
            id = R.id.tvProfileEmail
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = android.view.Gravity.CENTER; topMargin = 2 }
            text = prefs.getString("userEmail", "Sin correo") ?: "Sin correo"
            textSize = 14f
            setTextColor(0xFF9CA3AF.toInt())
        }
        container.addView(tvEmail)

        // Form fields card
        val formCard = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 24, 16, 0) }
            radius = 12f
            cardElevation = 0f
            setCardBackgroundColor(0xFFFFFFFF.toInt())
            strokeWidth = 0
            setContentPadding(4, 4, 4, 4)
        }
        val formFields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        formFields.addView(createFormField("Teléfono", R.id.etProfilePhone, "phone", false))
        formFields.addView(createFormField("Dirección", R.id.etProfileAddress, "text", false))
        formFields.addView(createFormField("Ciudad", R.id.etProfileCity, "text", false))
        formCard.addView(formFields)
        container.addView(formCard)

        // Load user data from API
        if (userId != 0) {
            ApiClient.instance.getUsuario(userId).enqueue(object : Callback<Usuario> {
                override fun onResponse(c: Call<Usuario>, res: Response<Usuario>) {
                    if (res.isSuccessful) {
                        val u = res.body()!!
                        tvName.text = u.nombre
                        tvEmail.text = u.email
                        findViewById<EditText>(R.id.etProfilePhone).setText(u.telefono ?: "No registrado")
                        findViewById<EditText>(R.id.etProfileAddress).setText(u.direccion ?: "No registrada")
                    }
                }
                override fun onFailure(c: Call<Usuario>, t: Throwable) {}
            })
        }

        // Edit / Save button
        val btnEdit = TextView(this).apply {
            id = R.id.btnProfileEdit
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                42.dpToPx()
            ).apply { setMargins(16, 20, 16, 0) }
            gravity = android.view.Gravity.CENTER
            text = "Editar Datos"
            setTextColor(0xFF111827.toInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_btn_outline)
            setOnClickListener {
                val etPhone = findViewById<EditText>(R.id.etProfilePhone)
                val etAddress = findViewById<EditText>(R.id.etProfileAddress)
                val etCity = findViewById<EditText>(R.id.etProfileCity)
                if (!isProfileEditing) {
                    isProfileEditing = true
                    text = "Guardar"
                    etPhone.isEnabled = true
                    etAddress.isEnabled = true
                    etCity.isEnabled = true
                } else {
                    val newPhone = etPhone.text.toString().trim()
                    val newAddress = etAddress.text.toString().trim()
                    if (userId != 0) {
                        ApiClient.instance.updateUsuario(userId, UsuarioUpdateRequest(
                            telefono = newPhone.ifEmpty { null },
                            direccion = newAddress.ifEmpty { null }
                        )).enqueue(object : Callback<Usuario> {
                            override fun onResponse(c: Call<Usuario>, res: Response<Usuario>) {
                                if (res.isSuccessful) {
                                    Toast.makeText(this@ClientMainActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                } else Toast.makeText(this@ClientMainActivity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                            }
                            override fun onFailure(c: Call<Usuario>, t: Throwable) {
                                Toast.makeText(this@ClientMainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    isProfileEditing = false
                    text = "Editar Datos"
                    etPhone.isEnabled = false
                    etAddress.isEnabled = false
                    etCity.isEnabled = false
                }
            }
        }
        container.addView(btnEdit)

        // Config options
        val configItems = listOf(
            "Ayuda" to { startActivity(Intent(this, HelpActivity::class.java)) },
            "Contacto" to { startActivity(Intent(this, ContactoActivity::class.java)) }
        )

        val configCard = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 12, 16, 0) }
            radius = 12f
            cardElevation = 0f
            setCardBackgroundColor(0xFFFFFFFF.toInt())
            strokeWidth = 0
            setContentPadding(0, 0, 0, 0)
        }
        val configList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        configItems.forEachIndexed { idx, item ->
            val lab = item.first
            val act = item.second
            val row = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    48.dpToPx()
                )
                gravity = android.view.Gravity.CENTER_VERTICAL
                text = "  $lab"
                textSize = 14f
                setTextColor(0xFF111827.toInt())
                setPadding(16, 0, 16, 0)
                setOnClickListener { act() }
            }
            configList.addView(row)
            if (idx < configItems.size - 1) {
                configList.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).apply { setMargins(16, 0, 16, 0) }
                    setBackgroundColor(0xFFD1D5DB.toInt())
                })
            }
        }
        configCard.addView(configList)
        container.addView(configCard)

        // Logout button
        val btnLogout = TextView(this).apply {
            id = R.id.btnProfileLogout
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                42.dpToPx()
            ).apply { setMargins(16, 20, 16, 32) }
            gravity = android.view.Gravity.CENTER
            text = "Cerrar Sesión"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(0xFFEF4444.toInt())
            setOnClickListener {
                prefs.edit().clear().apply()
                startActivity(Intent(this@ClientMainActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
        container.addView(btnLogout)

        contentContainer.addView(scrollView)
    }

    private fun createFormField(hint: String, viewId: Int, inputType: String, enabled: Boolean): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }
        container.addView(TextView(this).apply {
            text = hint
            textSize = 12f
            setTextColor(0xFF6B7280.toInt())
        })
        val et = EditText(this).apply {
            id = viewId
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                42.dpToPx()
            )
            background = resources.getDrawable(R.drawable.bg_crud_input, theme)
            setPadding(12, 0, 12, 0)
            textSize = 14f
            setTextColor(0xFF111827.toInt())
            isEnabled = enabled
        }
        container.addView(et)
        return container
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
