package com.example.nexbitmobile.ui

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.PathInterpolator
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.api.SocketManager
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.ui.components.ComprobanteUploadHandler
import com.example.nexbitmobile.ui.components.MainCarouselComponent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class MainOrbixActivity : AppCompatActivity() {

    // ─── Navigation ───
    private lateinit var contentContainer: FrameLayout
    private lateinit var toolbarSub: View
    private lateinit var toolbarDivider: View
    private lateinit var btnToolbarBack: ImageButton
    private lateinit var tvToolbarTitle: TextView

    // Bottom nav views
    private val navItems = mutableListOf<ImageView>()
    private var currentNavTab = "home"
    private lateinit var ivNavMenu: ImageView

    // Overlay menu
    private lateinit var menuOverlay: View
    private lateinit var menuPanelContainer: View
    private lateinit var menuItemsContainer: LinearLayout
    private var isMenuOpen = false

    // Navigation stack for inline sub-screens
    private val navStack = mutableListOf<String>()
    private var currentScreen = "home"

    // ─── Admin helper ───
    private lateinit var adminScreens: AdminScreens

    // ─── Data ───
    private var topProducts: List<Producto> = emptyList()

    // ─── Carousel component ───
    private lateinit var carouselComponent: MainCarouselComponent

    // ─── Comprobante upload component ───
    private lateinit var comprobanteUpload: ComprobanteUploadHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_orbix)

        contentContainer = findViewById(R.id.contentContainer)
        toolbarSub = findViewById(R.id.toolbarSub)
        toolbarDivider = findViewById(R.id.toolbarDivider)
        btnToolbarBack = findViewById(R.id.btnToolbarBack)
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
        menuOverlay = findViewById(R.id.menuOverlay)
        menuPanelContainer = findViewById(R.id.menuPanelContainer)
        menuItemsContainer = findViewById(R.id.menuItemsContainer)

        adminScreens = AdminScreens(this)
        carouselComponent = MainCarouselComponent(this)
        comprobanteUpload = ComprobanteUploadHandler(this)

        setupNavItems()
        setupMenuPanel()
        btnToolbarBack.setOnClickListener { goBack() }
        menuOverlay.setOnClickListener { closeMenu() }

        showHome()
        updateNavSelection("home")

        SocketManager.addListener(socketListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.removeListener(socketListener)
        if (::carouselComponent.isInitialized) carouselComponent.cleanup()
    }

    private val socketListener = object : SocketManager.SocketEventListener {
        override fun onEvent(event: String, data: org.json.JSONObject) {
            runOnUiThread {
                when (event) {
                    "nuevo-pedido" -> {
                        val pedidoId = data.optInt("pedido_id", 0)
                        NotificationToastHelper.show(
                            "Nuevo Pedido",
                            "Pedido #$pedidoId registrado. Revisa la lista de pedidos.",
                            "📦"
                        )
                    }
                    "nuevo-comprobante" -> {
                        val pedidoId = data.optInt("pedido_id", 0)
                        NotificationToastHelper.show(
                            "Comprobante Recibido",
                            "Pedido #$pedidoId tiene un nuevo comprobante para revisar.",
                            "📄"
                        )
                    }
                    "pedido-estado" -> {
                        val pedidoId = data.optInt("pedido_id", 0)
                        val estado = data.optString("estado", "")
                        NotificationToastHelper.show(
                            "Estado Actualizado",
                            "Pedido #$pedidoId cambió a $estado",
                            "🔄"
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isMenuOpen) { closeMenu(); return }
        if (navStack.isNotEmpty()) { goBack(); return }
        super.onBackPressed()
    }

    // ──────────── 3-BUTTON BOTTOM NAV ────────────

    private data class NavConfig(
        val containerId: Int, val iconId: Int, val key: String
    )

    private fun setupNavItems() {
        ivNavMenu = findViewById(R.id.ivNavMenu)
        val configs = listOf(
            NavConfig(R.id.navHome, R.id.ivNavHome, "home"),
            NavConfig(R.id.navProfile, R.id.ivNavProfile, "profile"),
            NavConfig(R.id.navMenu, R.id.ivNavMenu, "menu")
        )
        for (c in configs) {
            val icon = findViewById<ImageView>(c.iconId)
            navItems.add(icon)
            findViewById<View>(c.containerId).setOnClickListener {
                if (c.key == "menu") {
                    toggleMenu()
                } else {
                    carouselComponent.stopAutoRotate()
                    carouselComponent.isExpanded = false
                    closeMenu()
                    navStack.clear()
                    toolbarSub.visibility = View.GONE
                    toolbarDivider.visibility = View.GONE
                    when (c.key) {
                        "home" -> showHome()
                        "profile" -> showProfile()
                    }
                    currentNavTab = c.key
                    currentScreen = c.key
                    updateNavSelection(c.key)
                }
            }
        }
        // Load admin avatar into profile button
        loadProfileAvatar()
    }

    private fun loadProfileAvatar() {
        val iv = findViewById<ImageView>(R.id.ivNavProfile)
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val avatarUrl = prefs.getString("userAvatar", "") ?: ""
        if (avatarUrl.isNotEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into(iv)
        } else {
            iv.setImageResource(R.drawable.ic_icon_user)
        }
    }

    private fun updateNavSelection(selected: String) {
        val keys = listOf("home", "profile", "menu")
        val activeColor = resources.getColor(R.color.nav_active, theme)
        val inactiveColor = resources.getColor(R.color.nav_inactive, theme)
        navItems.forEachIndexed { i, icon ->
            val isActive = keys[i] == selected
            icon.imageTintList = android.content.res.ColorStateList.valueOf(
                if (isActive) activeColor else inactiveColor
            )
        }
    }

    // ──────────── OVERLAY MENU PANEL ────────────

    private data class MenuItem(
        val label: String, val iconRes: Int, val screenKey: String
    )

    private val menuItems = listOf(
        MenuItem("Pedidos", R.drawable.ic_icon_orders, "pedidos_admin"),
        MenuItem("Productos", R.drawable.ic_icon_products, "productos_admin"),
        MenuItem("Categorías", R.drawable.ic_filter_orbix, "categorias_admin"),
        MenuItem("Usuarios", android.R.drawable.ic_menu_myplaces, "usuarios_admin"),
        MenuItem("Proveedores", android.R.drawable.ic_menu_send, "proveedores_admin"),
        MenuItem("Repartidores", android.R.drawable.ic_menu_directions, "repartidores_admin"),
        MenuItem("Roles", android.R.drawable.ic_menu_manage, "roles_admin"),
        MenuItem("Chat", android.R.drawable.ic_menu_share, "chat_admin"),
        MenuItem("Revisión de Pagos", android.R.drawable.ic_menu_compass, "activity:RevisionPagos"),
        MenuItem("Checkout Manual", android.R.drawable.ic_menu_compass, "activity:CheckoutManual"),
        MenuItem("Papelera", android.R.drawable.ic_menu_delete, "activity:Papelera"),
        MenuItem("Reportes", android.R.drawable.ic_menu_compass, "activity:Reportes"),
        MenuItem("Ayuda", android.R.drawable.ic_menu_compass, "activity:Help"),
        MenuItem("Contacto", android.R.drawable.ic_menu_compass, "activity:Contacto")
    )

    private fun setupMenuPanel() {
        val inflater = LayoutInflater.from(this)
        for ((i, item) in menuItems.withIndex()) {
            val row = inflater.inflate(R.layout.item_menu_row, menuItemsContainer, false)
            row.findViewById<ImageView>(R.id.menuRowIcon).setImageResource(item.iconRes)
            row.findViewById<TextView>(R.id.menuRowText).text = item.label
            row.setOnClickListener {
                closeMenu()
                if (item.screenKey.startsWith("activity:")) {
                    val activityName = item.screenKey.removePrefix("activity:")
                    val intent = when (activityName) {
                        "RevisionPagos" -> Intent(this@MainOrbixActivity, RevisionPagosActivity::class.java)
                        "CheckoutManual" -> Intent(this@MainOrbixActivity, CheckoutManualActivity::class.java)
                        "Papelera" -> Intent(this@MainOrbixActivity, TrashOrdersActivity::class.java)
                        "Reportes" -> Intent(this@MainOrbixActivity, ReportesActivity::class.java)
                        "Help" -> Intent(this@MainOrbixActivity, HelpActivity::class.java)
                        "Contacto" -> Intent(this@MainOrbixActivity, ContactoActivity::class.java)
                        else -> null
                    }
                    if (intent != null) startActivity(intent)
                } else {
                    showInlineScreen(item.screenKey)
                }
            }
            menuItemsContainer.addView(row)

            if (i < menuItems.size - 1) {
                val divider = View(this)
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                ).apply { setMargins(16, 0, 16, 0) }
                divider.setBackgroundColor(resources.getColor(R.color.menu_divider, theme))
                menuItemsContainer.addView(divider)
            }
        }
    }

    private fun toggleMenu() {
        if (isMenuOpen) closeMenu() else openMenu()
    }

    private fun openMenu() {
        isMenuOpen = true
        menuOverlay.visibility = View.VISIBLE
        menuPanelContainer.visibility = View.VISIBLE

        menuOverlay.alpha = 0f
        menuOverlay.animate()
            .alpha(1f)
            .setDuration(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Rotate menu icon 90°
        ivNavMenu.animate()
            .rotation(90f)
            .setDuration(220)
            .setInterpolator(PathInterpolator(0.16f, 1f, 0.3f, 1f))
            .start()

        // Set pivot to bottom-right corner after layout
        menuPanelContainer.post {
            menuPanelContainer.pivotX = menuPanelContainer.width.toFloat()
            menuPanelContainer.pivotY = menuPanelContainer.height.toFloat()
            menuPanelContainer.scaleX = 0.85f
            menuPanelContainer.scaleY = 0.85f
            menuPanelContainer.alpha = 0f
            menuPanelContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(220)
                .setInterpolator(PathInterpolator(0.16f, 1f, 0.3f, 1f))
                .start()
        }

        updateNavSelection("menu")
    }

    private fun closeMenu() {
        if (!isMenuOpen) return
        isMenuOpen = false

        menuOverlay.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction { menuOverlay.visibility = View.GONE }
            .start()

        // Rotate menu icon back to 0°
        ivNavMenu.animate()
            .rotation(0f)
            .setDuration(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        menuPanelContainer.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .alpha(0f)
            .setDuration(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { menuPanelContainer.visibility = View.GONE }
            .start()

        updateNavSelection(currentNavTab)
    }

    // ──────────── INLINE NAVIGATION ────────────

    private fun showInlineScreen(screenKey: String) {
        navStack.add(currentScreen)
        currentScreen = screenKey
        toolbarSub.visibility = View.VISIBLE
        toolbarDivider.visibility = View.VISIBLE
        contentContainer.removeAllViews()

        when (screenKey) {
            "pedidos_admin" -> {
                tvToolbarTitle.text = "Pedidos"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_pedidos_admin, contentContainer, false
                )
                contentContainer.addView(v); showPedidosInline(v)
            }
            "productos_admin" -> {
                tvToolbarTitle.text = "Productos"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_productos_admin, contentContainer, false
                )
                contentContainer.addView(v); adminScreens.showProductos(v)
            }
            "categorias_admin" -> {
                tvToolbarTitle.text = "Categorías"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_categorias_admin, contentContainer, false
                )
                contentContainer.addView(v); adminScreens.showCategorias(v)
            }
            "usuarios_admin" -> {
                tvToolbarTitle.text = "Usuarios"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_usuarios_admin, contentContainer, false
                )
                contentContainer.addView(v); adminScreens.showUsuarios(v)
            }
            "proveedores_admin" -> {
                tvToolbarTitle.text = "Proveedores"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_proveedores_admin, contentContainer, false
                )
                contentContainer.addView(v); adminScreens.showProveedores(v)
            }
            "repartidores_admin" -> {
                tvToolbarTitle.text = "Repartidores"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_repartidores_admin, contentContainer, false
                )
                contentContainer.addView(v); adminScreens.showRepartidores(v)
            }
            "roles_admin" -> {
                tvToolbarTitle.text = "Roles"
                val v = LayoutInflater.from(this).inflate(
                    R.layout.inline_roles_admin, contentContainer, false
                )
                contentContainer.addView(v); adminScreens.showRoles(v)
            }
            "chat_admin" -> {
                tvToolbarTitle.text = "Chat - Conversaciones"
                showChatAdmin()
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
            "home" -> { showHome(); carouselComponent.startAutoRotate() }
            "profile" -> showProfile()
            else -> showHome()
        }
    }

    // ──────────── PEDIDOS INLINE ────────────

    private var currentStatusFilter: String? = null

    private fun showPedidosInline(root: View) {
        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Pedidos"
        val rv = root.findViewById<RecyclerView>(R.id.rvPedidosInline)
        val tvEstado = root.findViewById<TextView>(R.id.tvPedidosEstado)
        val tvEmpty = root.findViewById<TextView>(R.id.tvPedidosEmpty)
        val etSearch = root.findViewById<EditText>(R.id.etSearchPedidos)

        root.findViewById<View>(R.id.btnAddHeader).setOnClickListener {
            mostrarBottomSheetCrearPedido()
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
            addDuration = 300
            removeDuration = 300
            moveDuration = 300
            changeDuration = 300
        }
        val adapter = PedidoAdminAdapter(
            pedidos = emptyList(),
            onDetalle = { pedido -> mostrarDetallePedido(pedido) },
            onDownload = { pedido -> descargarTicketPedido(pedido) },
            onEdit = { pedido -> }
        )
        rv.adapter = adapter

        val chipMap = mapOf(
            R.id.chipTodos to null,
            R.id.chipPendiente to "pend",
            R.id.chipRevision to "revision",
            R.id.chipConfirmado to "confirm",
            R.id.chipEnviado to "envia",
            R.id.chipEntregado to "entrega",
            R.id.chipCancelado to "cancel"
        )

        for ((chipId, filterValue) in chipMap) {
            root.findViewById<View>(chipId).setOnClickListener {
                currentStatusFilter = filterValue
                for ((id, _) in chipMap) {
                    val chip = root.findViewById<TextView>(id)
                    chip.setBackgroundResource(R.drawable.bg_chip)
                    chip.setTextColor(resources.getColor(R.color.chip_text, theme))
                }
                val selectedChip = root.findViewById<TextView>(chipId)
                selectedChip.setBackgroundResource(R.drawable.bg_chip_selected)
                selectedChip.setTextColor(resources.getColor(R.color.chip_selected_text, theme))
                aplicarFiltrosPedidos(adapter, etSearch)
            }
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                aplicarFiltrosPedidos(adapter, etSearch)
            }
        })

        loadPedidosInline(rv, tvEstado, tvEmpty, adapter)
    }

    private fun aplicarFiltrosPedidos(adapter: PedidoAdminAdapter, etSearch: EditText) {
        val query = etSearch.text.toString().trim().lowercase()
        val filtered = allPedidos.filter { p ->
            val matchesSearch = query.isEmpty() ||
                p.id_pedido.toString().contains(query) ||
                (p.usuario_nombre?.lowercase()?.contains(query) == true)
            val matchesStatus = currentStatusFilter == null ||
                p.estado.lowercase().contains(currentStatusFilter!!)
            matchesSearch && matchesStatus
        }
        adapter.updateData(filtered)
    }

    private var allPedidos = listOf<Pedido>()

    private fun loadPedidosInline(rv: RecyclerView, tvEstado: TextView, tvEmpty: TextView, adapter: PedidoAdminAdapter) {
        ApiClient.instance.getPedidos().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
                    allPedidos = response.body() ?: emptyList()
                    adapter.updateData(allPedidos)
                    tvEstado.visibility = View.GONE
                    if (allPedidos.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rv.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rv.visibility = View.VISIBLE
                    }
                }
            }
            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                tvEstado.text = "Error al cargar pedidos"
            }
        })
    }

    // ──────────── BOTTOM SHEET: CREAR PEDIDO ────────────

    private fun mostrarBottomSheetCrearPedido() {
        mostrarBottomSheetPedido(null)
    }

    private fun mostrarBottomSheetEditarPedido(pedido: Pedido) {
        mostrarBottomSheetPedido(pedido)
    }

    private fun mostrarBottomSheetPedido(pedidoParaEditar: Pedido?) {
        val isEdit = pedidoParaEditar != null
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_create_order, null)
        dialog.setContentView(view)

        // ── Views ──
        val etSearchCliente = view.findViewById<EditText>(R.id.etSearchCliente)
        val ivClearCliente = view.findViewById<ImageView>(R.id.ivClearCliente)
        val lvClientes = view.findViewById<ListView>(R.id.lvClientes)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)
        val etNotas = view.findViewById<EditText>(R.id.etNotas)
        val frameUpload = view.findViewById<FrameLayout>(R.id.frameUpload)
        val tvFileName = view.findViewById<TextView>(R.id.tvFileName)
        val etSearchProducto = view.findViewById<EditText>(R.id.etSearchProducto)
        val lvProductos = view.findViewById<ListView>(R.id.lvProductos)
        val etCantidad = view.findViewById<EditText>(R.id.etCantidad)
        val btnAgregar = view.findViewById<ImageButton>(R.id.btnAgregarProducto)
        val rvProductos = view.findViewById<RecyclerView>(R.id.rvProductosSheet)
        val tvEmptyProductos = view.findViewById<TextView>(R.id.tvProductosEmpty)
        val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)
        val tvIva = view.findViewById<TextView>(R.id.tvIva)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val btnCancelar = view.findViewById<TextView>(R.id.btnCancelar)
        val btnCrear = view.findViewById<TextView>(R.id.btnCrearPedido)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseSheet)

        // ── Header ──
        val tvSheetTitle = view.findViewById<TextView>(R.id.tvSheetTitle)
        val headerTitle = if (isEdit) "Editar Pedido #${pedidoParaEditar!!.id_pedido}" else "Nuevo Pedido — Administrador"
        tvSheetTitle.text = headerTitle
        btnCrear.text = if (isEdit) "Actualizar Pedido" else "Crear Pedido"

        btnClose.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }

        // ── Data ──
        var clientes = listOf<Usuario>()
        var productos = listOf<Producto>()
        var selectedCliente: Usuario? = null
        var selectedProducto: Producto? = null
        val productosAgregados = mutableListOf<ProductoEnPedido>()

        val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // ── Producto sheet adapter ──
        rvProductos.layoutManager = LinearLayoutManager(this)
        val sheetAdapter = ProductoSheetAdapter(productosAgregados) { pos ->
            productosAgregados.removeAt(pos)
            (rvProductos.adapter as? ProductoSheetAdapter)?.updateData(productosAgregados)
            actualizarVisibilidadProductos(tvEmptyProductos, rvProductos, productosAgregados)
            recalcularTotalesSheet(productosAgregados, tvSubtotal, tvIva, tvTotal, fmt)
        }
        rvProductos.adapter = sheetAdapter

        // ── Cargar clientes ──
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(c: Call<List<Usuario>>, res: Response<List<Usuario>>) {
                clientes = res.body()?.filter { it.rol_nombre != "ADMIN" } ?: emptyList()
                if (isEdit && pedidoParaEditar?.usuario_nombre != null) {
                    val match = clientes.find { it.nombre == pedidoParaEditar!!.usuario_nombre }
                    if (match != null) {
                        selectedCliente = match
                        etSearchCliente.setText("${match.nombre} (${match.email})")
                        etSearchCliente.setSelection(etSearchCliente.text.length)
                        ivClearCliente.visibility = View.VISIBLE
                    }
                }
            }
            override fun onFailure(c: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(this@MainOrbixActivity, "Error al cargar clientes", Toast.LENGTH_SHORT).show()
            }
        })

        // ── Cargar productos ──
        ApiClient.instance.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(c: Call<List<Producto>>, res: Response<List<Producto>>) {
                productos = res.body()?.filter { it.activo == 1 } ?: emptyList()
            }
            override fun onFailure(c: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(this@MainOrbixActivity, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
        })

        // ── Buscador de clientes ──
        etSearchCliente.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && clientes.isNotEmpty()) {
                mostrarListaClientes(clientes, etSearchCliente, lvClientes, ivClearCliente) { c ->
                    selectedCliente = c
                    etSearchCliente.setText("${c.nombre} (${c.email})")
                    etSearchCliente.setSelection(etSearchCliente.text.length)
                    lvClientes.visibility = View.GONE
                    ivClearCliente.visibility = View.VISIBLE
                }
            }
        }

        etSearchCliente.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                if (query.isEmpty() && selectedCliente == null) {
                    lvClientes.visibility = View.GONE
                    ivClearCliente.visibility = View.GONE
                    return
                }
                if (selectedCliente != null) {
                    selectedCliente = null
                }
                ivClearCliente.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

                val filtered = clientes.filter {
                    it.nombre.lowercase().contains(query) || it.email.lowercase().contains(query)
                }
                if (filtered.isNotEmpty()) {
                    mostrarListaClientes(filtered, etSearchCliente, lvClientes, ivClearCliente) { c ->
                        selectedCliente = c
                        etSearchCliente.setText("${c.nombre} (${c.email})")
                        etSearchCliente.setSelection(etSearchCliente.text.length)
                        lvClientes.visibility = View.GONE
                        ivClearCliente.visibility = View.VISIBLE
                    }
                } else {
                    lvClientes.visibility = View.GONE
                }
            }
        })

        ivClearCliente.setOnClickListener {
            selectedCliente = null
            etSearchCliente.setText("")
            lvClientes.visibility = View.GONE
            ivClearCliente.visibility = View.GONE
        }

        // ── Buscador de productos ──
        etSearchProducto.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && productos.isNotEmpty()) {
                    mostrarListaProductos(productos, etSearchProducto, lvProductos) { p ->
                    selectedProducto = p
                    etSearchProducto.setText("${p.nombre} — ${fmt.format(p.precio_venta)}")
                    etSearchProducto.setSelection(etSearchProducto.text.length)
                    lvProductos.visibility = View.GONE
                }
            }
        }

        etSearchProducto.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                if (query.isEmpty() && selectedProducto == null) {
                    lvProductos.visibility = View.GONE
                    return
                }
                if (selectedProducto != null) {
                    selectedProducto = null
                }
                val filtered = productos.filter {
                    it.nombre.lowercase().contains(query) || it.descripcion?.lowercase()?.contains(query) == true
                }
                if (filtered.isNotEmpty()) {
                    mostrarListaProductos(filtered, etSearchProducto, lvProductos) { p ->
                        selectedProducto = p
                        etSearchProducto.setText("${p.nombre} — ${fmt.format(p.precio_venta)}")
                        etSearchProducto.setSelection(etSearchProducto.text.length)
                        lvProductos.visibility = View.GONE
                    }
                } else {
                    lvProductos.visibility = View.GONE
                }
            }
        })

        // ── Agregar producto a la lista ──
        btnAgregar.setOnClickListener {
            val prod = selectedProducto
            if (prod == null) {
                Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cant = etCantidad.text.toString().toIntOrNull()
            if (cant == null || cant < 1) {
                Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Check if product already exists, update quantity
            val existing = productosAgregados.indexOfFirst { it.producto.id_producto == prod.id_producto }
            if (existing >= 0) {
                productosAgregados[existing] = productosAgregados[existing].copy(
                    cantidad = productosAgregados[existing].cantidad + cant
                )
            } else {
                productosAgregados.add(ProductoEnPedido(prod, cant))
            }
            sheetAdapter.updateData(productosAgregados)
            actualizarVisibilidadProductos(tvEmptyProductos, rvProductos, productosAgregados)
            recalcularTotalesSheet(productosAgregados, tvSubtotal, tvIva, tvTotal, fmt)
            etCantidad.setText("1")
            etSearchProducto.setText("")
            selectedProducto = null
        }

        // ── Subir comprobante ──
        comprobanteUpload.reset()
        comprobanteUpload.currentSheetFileNameView = tvFileName
        tvFileName.visibility = View.GONE
        frameUpload.setOnClickListener {
            comprobanteUpload.launchPicker()
        }

        // ── Pre-fill en modo edición ──
        if (isEdit) {
            val p = pedidoParaEditar!!
            etDireccion.setText(p.direccion_entrega ?: "")
            etNotas.setText(p.notas_entrega ?: "")
        }

        // ── Crear / Actualizar pedido ──
        btnCrear.setOnClickListener {
            val cliente = selectedCliente
            val direccion = etDireccion.text.toString().trim()
            val notas = etNotas.text.toString().trim()

            if (cliente == null) {
                Toast.makeText(this, "Selecciona un cliente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (productosAgregados.isEmpty()) {
                Toast.makeText(this, "Agrega al menos un producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalSinIva = productosAgregados.sumOf { it.subtotal }
            val totalConIva = totalSinIva + totalSinIva * 0.19

            val request = PedidoRequest(
                usuario_id = cliente.id_usuario,
                total = totalConIva,
                estado = pedidoParaEditar?.estado ?: "PENDIENTE_DE_PAGO",
                direccion_entrega = direccion.ifBlank { null },
                notas_entrega = notas.ifBlank { null }
            )

            btnCrear.isEnabled = false
            btnCrear.text = if (isEdit) "Actualizando..." else "Creando..."

            val call = if (isEdit) {
                ApiClient.instance.updatePedido(pedidoParaEditar!!.id_pedido, request)
            } else {
                ApiClient.instance.createPedido(request)
            }

            call.enqueue(object : Callback<Void> {
                override fun onResponse(c: Call<Void>, res: Response<Void>) {
                    btnCrear.isEnabled = true
                    btnCrear.text = if (isEdit) "Actualizar Pedido" else "Crear Pedido"
                    if (res.isSuccessful) {
                        val msg = if (isEdit) "Pedido actualizado" else "Pedido creado con éxito"
                        Toast.makeText(this@MainOrbixActivity, msg, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        allPedidos = emptyList()
                        val rv = findViewById<RecyclerView>(R.id.rvPedidosInline)
                        val tvEstado = findViewById<TextView>(R.id.tvPedidosEstado)
                        val tvEmpty = findViewById<TextView>(R.id.tvPedidosEmpty)
                        if (rv != null) {
                            val adapter = rv.adapter as? PedidoAdminAdapter
                            if (adapter != null) {
                                loadPedidosInline(rv, tvEstado, tvEmpty, adapter)
                            }
                        }
                    } else {
                        val errMsg = if (isEdit) "Error al actualizar (${res.code()})" else "Error al crear pedido (${res.code()})"
                        Toast.makeText(this@MainOrbixActivity, errMsg, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(c: Call<Void>, t: Throwable) {
                    btnCrear.isEnabled = true
                    btnCrear.text = if (isEdit) "Actualizar Pedido" else "Crear Pedido"
                    Toast.makeText(this@MainOrbixActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }

        dialog.show()
    }

    private fun mostrarListaClientes(
        lista: List<Usuario>,
        et: EditText,
        lv: ListView,
        ivClear: ImageView,
        onSeleccion: (Usuario) -> Unit
    ) {
        val nombres = lista.map { "${it.nombre} (${it.email})" }
        lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        lv.visibility = View.VISIBLE
        lv.setOnItemClickListener { _, _, pos, _ ->
            onSeleccion(lista[pos])
        }
    }

    private fun mostrarListaProductos(
        lista: List<Producto>,
        et: EditText,
        lv: ListView,
        onSeleccion: (Producto) -> Unit
    ) {
        val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val nombres = lista.map { "${it.nombre} — ${fmt.format(it.precio_venta)}" }
        lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        lv.visibility = View.VISIBLE
        lv.setOnItemClickListener { _, _, pos, _ ->
            onSeleccion(lista[pos])
        }
    }

    private fun actualizarVisibilidadProductos(
        tvEmpty: TextView,
        rv: RecyclerView,
        items: List<ProductoEnPedido>
    ) {
        if (items.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }
    }

    private fun recalcularTotalesSheet(
        items: List<ProductoEnPedido>,
        tvSubtotal: TextView,
        tvIva: TextView,
        tvTotal: TextView,
        fmt: NumberFormat
    ) {
        val subtotal = items.sumOf { it.subtotal }
        val iva = subtotal * 0.19
        tvSubtotal.text = fmt.format(subtotal)
        tvIva.text = fmt.format(iva)
        tvTotal.text = fmt.format(subtotal + iva)
    }

    // ──────────── PEDIDOS: DETALLE, TICKET, DELETE ────────────

    private fun mostrarDetallePedido(pedido: Pedido) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_detalle_pedido, null)
        dialog.setContentView(view)

        val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // ── Views ──
        val tvTitulo = view.findViewById<TextView>(R.id.tvDetalleTitulo)
        val tvCliente = view.findViewById<TextView>(R.id.tvDetalleCliente)
        val tvEstado = view.findViewById<TextView>(R.id.tvDetalleEstado)
        val tvFecha = view.findViewById<TextView>(R.id.tvDetalleFecha)
        val tvTotal = view.findViewById<TextView>(R.id.tvDetalleTotal)
        val layoutProductos = view.findViewById<LinearLayout>(R.id.layoutProductosDetalle)
        val ivComprobanteThumb = view.findViewById<ImageView>(R.id.ivComprobanteThumb)
        val tvSinComprobante = view.findViewById<TextView>(R.id.tvSinComprobante)
        val btnCancelar = view.findViewById<TextView>(R.id.btnCancelarPedido)
        val btnVerComprobante = view.findViewById<TextView>(R.id.btnVerComprobante)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDetalle)

        // ── Header ──
        tvTitulo.text = "Pedido #${String.format("%06d", pedido.id_pedido)}"

        // ── Info card ──
        tvCliente.text = pedido.usuario_nombre ?: "Sin nombre"

        val (estadoBg, estadoColor, estadoLabel) = when {
            pedido.estado.contains("ENTREG", true) -> Triple(R.drawable.bg_badge_ios_delivered, "#34C759", "ENTREGADO")
            pedido.estado.contains("PENDIENTE", true) -> Triple(R.drawable.bg_badge_ios_pending, "#FF9500", "PENDIENTE DE PAGO")
            pedido.estado.contains("CANCEL", true) -> Triple(R.drawable.bg_badge_ios_cancelled, "#FF3B30", "CANCELADO")
            pedido.estado.contains("CONFIRM", true) -> Triple(R.drawable.bg_badge_ios_delivered, "#34C759", "CONFIRMADO")
            pedido.estado.contains("RUTA", true) || pedido.estado.contains("CAMINO", true) -> Triple(R.drawable.bg_badge_ios_pending, "#FF9500", "EN CAMINO")
            else -> Triple(R.drawable.bg_badge_ios_pending, "#FF9500", pedido.estado)
        }
        tvEstado.background = ContextCompat.getDrawable(this, estadoBg)
        tvEstado.setTextColor(Color.parseColor(estadoColor))
        tvEstado.text = estadoLabel

        tvFecha.text = formatearFechaDetalle(pedido.fecha ?: pedido.fecha_pedido)
        tvTotal.text = fmt.format(pedido.total)

        // ── Comprobante thumbnail ──
        val tieneComprobante = !pedido.comprobante_pago_url.isNullOrBlank()
        if (tieneComprobante) {
            ivComprobanteThumb.visibility = View.VISIBLE
            tvSinComprobante.visibility = View.GONE
            Glide.with(this).load(pedido.comprobante_pago_url).into(ivComprobanteThumb)
        } else {
            ivComprobanteThumb.visibility = View.GONE
            tvSinComprobante.visibility = View.VISIBLE
        }

        // ── Productos ──
        layoutProductos.removeAllViews()
        val detalles = pedido.detalles
        if (!detalles.isNullOrEmpty()) {
            for (d in detalles) {
                val row = layoutInflater.inflate(R.layout.item_producto_detalle, layoutProductos, false)
                row.findViewById<TextView>(R.id.tvProdNombre).text = d.producto_nombre ?: "Producto"
                row.findViewById<TextView>(R.id.tvProdCantidad).text = "Cantidad: ${d.cantidad}"
                row.findViewById<TextView>(R.id.tvProdPrecioUnit).text = "Precio unitario: ${fmt.format(d.precio_unitario)}"
                row.findViewById<TextView>(R.id.tvProdSubtotal).text = fmt.format(d.subtotal)
                layoutProductos.addView(row)
            }
        } else {
            val emptyView = TextView(this).apply {
                text = "Sin productos detallados"
                setTextColor(ContextCompat.getColor(this@MainOrbixActivity, R.color.crud_text_secondary))
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 24, 0, 24)
            }
            layoutProductos.addView(emptyView)
        }

        // ── Botones ──
        btnClose.setOnClickListener { dialog.dismiss() }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
            confirmarCancelarPedido(pedido)
        }

        btnVerComprobante.setOnClickListener {
            mostrarRevisionPago(pedido)
        }

        dialog.show()
    }

    private fun mostrarRevisionPago(pedido: Pedido) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_revision_pago, null)
        dialog.setContentView(view)

        val etNota = view.findViewById<EditText>(R.id.etNotaRevision)
        val ivImagen = view.findViewById<ImageView>(R.id.ivComprobanteImagen)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutComprobanteEmpty)
        val btnAceptar = view.findViewById<TextView>(R.id.btnAceptarPago)
        val btnRechazar = view.findViewById<TextView>(R.id.btnRechazarPago)
        val btnChatear = view.findViewById<TextView>(R.id.btnChatearCliente)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseRevision)

        btnClose.setOnClickListener { dialog.dismiss() }

        // Comprobante image
        val tieneComprobante = !pedido.comprobante_pago_url.isNullOrBlank()
        if (tieneComprobante) {
            layoutEmpty.visibility = View.GONE
            ivImagen.visibility = View.VISIBLE
            Glide.with(this).load(pedido.comprobante_pago_url).into(ivImagen)
        } else {
            layoutEmpty.visibility = View.VISIBLE
            ivImagen.visibility = View.GONE
        }

        // Aceptar pago
        btnAceptar.setOnClickListener {
            btnAceptar.isEnabled = false
            ApiClient.instance.aprobarPago(pedido.id_pedido).enqueue(object : Callback<Void> {
                override fun onResponse(c: Call<Void>, res: Response<Void>) {
                    btnAceptar.isEnabled = true
                    if (res.isSuccessful) {
                        Toast.makeText(this@MainOrbixActivity, "Pago aprobado — Pedido listo para repartidores", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        recargarPedidosInline()
                    } else {
                        Toast.makeText(this@MainOrbixActivity, "Error al aprobar (${res.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(c: Call<Void>, t: Throwable) {
                    btnAceptar.isEnabled = true
                    Toast.makeText(this@MainOrbixActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Rechazar pago
        btnRechazar.setOnClickListener {
            val motivo = etNota.text.toString().trim()
            btnRechazar.isEnabled = false
            ApiClient.instance.rechazarPago(pedido.id_pedido, RechazarPagoRequest(motivo))
                .enqueue(object : Callback<Void> {
                    override fun onResponse(c: Call<Void>, res: Response<Void>) {
                        btnRechazar.isEnabled = true
                        if (res.isSuccessful) {
                            Toast.makeText(this@MainOrbixActivity, "Pago rechazado", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            recargarPedidosInline()
                        } else {
                            Toast.makeText(this@MainOrbixActivity, "Error al rechazar (${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(c: Call<Void>, t: Throwable) {
                        btnRechazar.isEnabled = true
                        Toast.makeText(this@MainOrbixActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Chatear
        btnChatear.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("usuario_id", pedido.usuario_id)
                putExtra("usuario_nombre", pedido.usuario_nombre ?: "Cliente")
            }
            startActivity(intent)
        }

        dialog.show()
    }

    private fun confirmarCancelarPedido(pedido: Pedido) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Cancelar Pedido #${pedido.id_pedido}")
            .setMessage("¿Estás seguro de cancelar este pedido?")
            .setPositiveButton("Cancelar Pedido") { _, _ ->
                ApiClient.instance.cancelarPedido(pedido.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(c: Call<Void>, res: Response<Void>) {
                        if (res.isSuccessful) {
                            Toast.makeText(this@MainOrbixActivity, "Pedido cancelado", Toast.LENGTH_SHORT).show()
                            recargarPedidosInline()
                        } else {
                            Toast.makeText(this@MainOrbixActivity, "Error al cancelar (${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(c: Call<Void>, t: Throwable) {
                        Toast.makeText(this@MainOrbixActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun formatearFechaDetalle(fechaRaw: String?): String {
        if (fechaRaw.isNullOrBlank()) return "Fecha no disponible"
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = isoFormat.parse(fechaRaw.take(19))
            if (date != null) {
                val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "CO"))
                outputFormat.format(date)
            } else "Fecha no disponible"
        } catch (_: Exception) {
            fechaRaw.take(16)
        }
    }

    private fun recargarPedidosInline() {
        allPedidos = emptyList()
        val rv = findViewById<RecyclerView>(R.id.rvPedidosInline)
        val tvEstado = findViewById<TextView>(R.id.tvPedidosEstado)
        val tvEmpty = findViewById<TextView>(R.id.tvPedidosEmpty)
        if (rv != null) {
            val adapter = rv.adapter as? PedidoAdminAdapter
            if (adapter != null) {
                loadPedidosInline(rv, tvEstado, tvEmpty, adapter)
            }
        }
    }

    private var ticketWebView: WebView? = null

    private fun descargarTicketPedido(pedido: Pedido) {
        ApiClient.instance.getPedidoTicket(pedido.id_pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    response.body()?.let { t -> generarHtmlYPdfPedido(t) }
                        ?: Toast.makeText(this@MainOrbixActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainOrbixActivity, "Error al obtener ticket", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Log.e("MainOrbix", "Ticket error", t)
                Toast.makeText(this@MainOrbixActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmarEliminarPedido(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Pedido #${pedido.id_pedido}")
            .setMessage("¿Estás seguro de eliminar este pedido? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                ApiClient.instance.deletePedido(pedido.id_pedido).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainOrbixActivity, "Pedido eliminado", Toast.LENGTH_SHORT).show()
                            val rv = findViewById<RecyclerView>(R.id.rvPedidosInline)
                            val tvEstado = findViewById<TextView>(R.id.tvPedidosEstado)
                            val tvEmpty = findViewById<TextView>(R.id.tvPedidosEmpty)
                            if (rv != null) {
                                val adapter = rv.adapter as? PedidoAdminAdapter
                                if (adapter != null) loadPedidosInline(rv, tvEstado, tvEmpty, adapter)
                            }
                        } else {
                            Toast.makeText(this@MainOrbixActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@MainOrbixActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun generarHtmlYPdfPedido(pedido: Pedido) {
        val detalles = pedido.detalles ?: emptyList()
        val filas = if (detalles.isNotEmpty()) {
            detalles.joinToString("") { d ->
                """<tr><td style="padding:8px;border-bottom:1px solid #e2e8f0;">${d.producto_nombre}</td><td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:center;">${d.cantidad}</td><td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.precio_unitario}</td><td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;">$${d.subtotal}</td></tr>"""
            }
        } else {
            "<tr><td colspan='4' style='padding:12px;text-align:center;color:#94a3b8;'>Sin productos detallados</td></tr>"
        }
        val html = """
            <!DOCTYPE html><html><head><meta charset="UTF-8"/><title>Ticket #${pedido.id_pedido}</title>
            <style>*{margin:0;padding:0;box-sizing:border-box}
            body{font-family:sans-serif;background:#e2e8f0;padding:40px 20px;color:#1e293b}
            .ticket{max-width:600px;margin:0 auto;background:#fff;border-radius:4px;padding:32px;border-top:6px solid #0f172a}
            .hdr{display:flex;justify-content:space-between;margin-bottom:24px}
            .brand{font-size:1.75rem;font-weight:700;color:#0f172a}
            table{width:100%;border-collapse:collapse;margin:24px 0}
            th{text-align:left;padding:12px;border-bottom:2px solid #e2e8f0;font-size:.8rem;color:#64748b;text-transform:uppercase}
            td{padding:12px;border-bottom:1px solid #f1f5f9}
            .total{text-align:right;font-size:1.5rem;font-weight:700;margin-top:16px}
            </style></head><body>
            <div class="ticket">
            <div class="hdr"><div class="brand">Nexbit</div><div class="order-id">#${String.format("%06d", pedido.id_pedido)}</div></div>
            <p><strong>Cliente:</strong> ${pedido.usuario_nombre ?: "N/A"}</p>
            <p><strong>Fecha:</strong> ${pedido.fecha ?: pedido.fecha_pedido ?: "N/A"}</p>
            <p><strong>Estado:</strong> ${pedido.estado}</p>
            <table><thead><tr><th>Producto</th><th>Cant</th><th>Precio</th><th>Subtotal</th></tr></thead><tbody>$filas</tbody></table>
            <div class="total">Total: $${pedido.total}</div>
            </div></body></html>
        """.trimIndent()
        val wv = WebView(this)
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                (getSystemService(Context.PRINT_SERVICE) as PrintManager)
                    .print("Nexbit Ticket", view.createPrintDocumentAdapter("Pedido"), PrintAttributes.Builder().build())
                ticketWebView = null
            }
        }
        wv.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
        ticketWebView = wv
    }

    // ──────────── SCREEN A: PANEL DE REPORTES ────────────

    private fun showReportsList() {
        contentContainer.removeAllViews()
        val view = LayoutInflater.from(this)
            .inflate(R.layout.panel_reportes_list, contentContainer, false)
        contentContainer.addView(view)

        // Slide-in animation
        view.translationX = resources.displayMetrics.widthPixels.toFloat()
        view.animate()
            .translationX(0f)
            .setDuration(250)
            .setInterpolator(PathInterpolator(0.2f, 0f, 0f, 1f))
            .start()

        // Back button
        view.findViewById<View>(R.id.btnReportBack).setOnClickListener { goBack() }

        // Card click listeners
        val cardIds = listOf(
            R.id.cardReport1 to "Ventas y Comprobantes",
            R.id.cardReport2 to "Inventario y Ganancias",
            R.id.cardReport3 to "Seguridad y Accesos",
            R.id.cardReport4 to "Carritos Activos",
            R.id.cardReport5 to "Repartidores y Logística"
        )
        for ((cardId, title) in cardIds) {
            view.findViewById<View>(cardId).setOnClickListener {
                navigateToReportDetail(title)
            }
        }
    }

    // ──────────── SCREEN B: DETALLE DE REPORTE ────────────

    private fun showReportDetail(title: String) {
        contentContainer.removeAllViews()

        // For now, use sales detail layout as template; in future, load by title
        val layoutRes = R.layout.report_detail_sales
        val view = LayoutInflater.from(this)
            .inflate(layoutRes, contentContainer, false)
        contentContainer.addView(view)

        // Update title
        view.findViewById<TextView>(R.id.tvDetailTitle)?.text = title

        // Back button
        view.findViewById<View>(R.id.btnDetailBack).setOnClickListener { goBack() }

        // Fade-in stagger animation
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    // ──────────── HOME ────────────

    private fun showHome() {
        carouselComponent.isExpanded = false
        contentContainer.removeAllViews()
        val view = LayoutInflater.from(this)
            .inflate(R.layout.fragment_home, contentContainer, false)
        contentContainer.addView(view)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val name = prefs.getString("userName", "Admin") ?: "Admin"
        view.findViewById<TextView>(R.id.tvHomeGreeting).text = "Good Morning!"
        view.findViewById<TextView>(R.id.tvHomeUserName).text = name

        // Load avatar
        val avatarUrl = prefs.getString("userAvatar", "") ?: ""
        val ivAvatar = view.findViewById<ImageView>(R.id.ivHomeAvatar)
        if (avatarUrl.isNotEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar)
        }

        // Carousel setup via component
        carouselComponent.setup(view)
        carouselComponent.setOnPageClick {
            carouselComponent.stopAutoRotate()
            navigateToReportList()
        }

        loadTopProducts(view)
        loadHomeStats(view)
    }

    // ──────────── TAP TO NAVIGATE TO REPORT LIST ────────────

    private fun navigateToReportList() {
        navStack.add(currentScreen)
        currentScreen = "report_list"
        toolbarSub.visibility = View.GONE
        toolbarDivider.visibility = View.GONE
        showReportsList()
    }

    private fun navigateToReportDetail(title: String) {
        navStack.add(currentScreen)
        currentScreen = "report_detail"
        toolbarSub.visibility = View.GONE
        toolbarDivider.visibility = View.GONE
        showReportDetail(title)
    }

    private fun loadHomeStats(root: View) {
        val tvStatsProducts = root.findViewById<TextView>(R.id.tvStatsProducts)
        val tvStatsOrders = root.findViewById<TextView>(R.id.tvStatsOrders)
        val tvStatsClients = root.findViewById<TextView>(R.id.tvStatsClients)
        val tvStatsCategories = root.findViewById<TextView>(R.id.tvStatsCategories)

        ApiClient.instance.getStats().enqueue(object : Callback<StatsResponse> {
            override fun onResponse(call: Call<StatsResponse>, response: Response<StatsResponse>) {
                if (response.isSuccessful) {
                    val stats = response.body() ?: return
                    tvStatsProducts?.text = stats.productos.toString()
                    tvStatsOrders?.text = stats.pedidos.toString()
                    tvStatsClients?.text = stats.clientes.toString()
                    tvStatsCategories?.text = stats.categorias.toString()
                }
            }
            override fun onFailure(call: Call<StatsResponse>, t: Throwable) {}
        })
    }

    private fun loadTopProducts(root: View) {
        val container = root.findViewById<LinearLayout>(R.id.topProductsContainer)
        ApiClient.instance.getProductosPublico()
            .enqueue(object : Callback<List<Producto>> {
                override fun onResponse(
                    c: Call<List<Producto>>, r: Response<List<Producto>>
                ) {
                    if (!r.isSuccessful) return
                    topProducts = r.body()?.take(5) ?: emptyList()
                    container.removeAllViews()
                    for ((i, p) in topProducts.withIndex()) {
                        val v = LayoutInflater.from(this@MainOrbixActivity)
                            .inflate(R.layout.item_top_product, container, false)
                        v.findViewById<TextView>(R.id.tvProductName).text = p.nombre
                        v.findViewById<TextView>(R.id.tvProductBrand).text =
                            p.categoria_nombre ?: "General"
                        v.findViewById<TextView>(R.id.tvUnitsSold).text =
                            "${p.stock_actual}"
                        v.findViewById<TextView>(R.id.tvGrowthPercent).text =
                            "+${(20 + i * 5) % 50}%"
                        com.bumptech.glide.Glide.with(this@MainOrbixActivity)
                            .load(p.imagen_url)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(v.findViewById(R.id.ivProductThumb))
                        container.addView(v)
                    }
                }
                override fun onFailure(c: Call<List<Producto>>, t: Throwable) {}
            })
    }

    // ──────────── PROFILE ────────────

    private fun showProfile() {
        contentContainer.removeAllViews()
        val view = LayoutInflater.from(this)
            .inflate(R.layout.fragment_profile, contentContainer, false)
        contentContainer.addView(view)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val name = prefs.getString("userName", "Admin") ?: "Admin"
        val email = prefs.getString("userEmail", "") ?: ""
        val phone = prefs.getString("userPhone", "") ?: ""
        val address = prefs.getString("userAddress", "") ?: ""

        // Load avatar
        val avatarUrl = prefs.getString("userAvatar", "") ?: ""
        val ivAvatar = view.findViewById<ImageView>(R.id.ivProfileAvatar)
        if (avatarUrl.isNotEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar)
        }

        view.findViewById<TextView>(R.id.tvProfileName).text = name
        val emailDisplay = email.ifEmpty { "Sin correo registrado" }
        view.findViewById<TextView>(R.id.tvProfileEmail).text = emailDisplay
        view.findViewById<TextView>(R.id.tvProfileEmailValue).text = emailDisplay
        view.findViewById<TextView>(R.id.tvProfilePhone).text =
            phone.ifEmpty { "No registrado" }
        view.findViewById<TextView>(R.id.tvProfileAddress).text =
            address.ifEmpty { "No registrada" }

        view.findViewById<View>(R.id.btnProfileLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    // ──────────── CHAT ADMIN ────────────

    private fun showChatAdmin() {
        contentContainer.removeAllViews()
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
        contentContainer.addView(scrollView)

        container.addView(TextView(this).apply {
            text = "Conversaciones"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        })

        ApiClient.instance.getConversacionesAdmin().enqueue(object : Callback<List<Conversacion>> {
            override fun onResponse(call: Call<List<Conversacion>>, response: Response<List<Conversacion>>) {
                if (response.isSuccessful) {
                    val conversaciones = response.body() ?: emptyList<Conversacion>()
                    if (conversaciones.isEmpty()) {
                        container.addView(TextView(this@MainOrbixActivity).apply {
                            text = "No hay conversaciones activas"
                            textSize = 14f
                            setPadding(0, 32, 0, 0)
                            setTextColor(resources.getColor(R.color.text_secondary, theme))
                        })
                        return
                    }
                    for (conv in conversaciones) {
                        val card = com.google.android.material.card.MaterialCardView(this@MainOrbixActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 8 }
                            radius = 12f
                            cardElevation = 2f
                            setCardBackgroundColor(resources.getColor(R.color.bg_card, theme))
                            setContentPadding(16, 16, 16, 16)
                            isClickable = true
                            isFocusable = true
                            setOnClickListener {
                                val intent = Intent(this@MainOrbixActivity, ChatActivity::class.java)
                                intent.putExtra("pedido_id", conv.pedido_id)
                                startActivity(intent)
                            }
                        }
                        val cardContent = LinearLayout(this@MainOrbixActivity).apply {
                            orientation = LinearLayout.VERTICAL
                        }
                        cardContent.addView(TextView(this@MainOrbixActivity).apply {
                            text = "Pedido #${conv.pedido_id} - ${conv.usuario?.nombre ?: "Cliente"}"
                            textSize = 14f
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            setTextColor(resources.getColor(R.color.text_main, theme))
                        })
                        cardContent.addView(TextView(this@MainOrbixActivity).apply {
                            text = conv.ultimo_mensaje ?: "Sin mensajes"
                            textSize = 12f
                            setTextColor(resources.getColor(R.color.text_secondary, theme))
                            maxLines = 1
                        })
                        if (conv.no_leidos != null && conv.no_leidos > 0) {
                            cardContent.addView(TextView(this@MainOrbixActivity).apply {
                                text = "${conv.no_leidos} mensajes no leídos"
                                textSize = 12f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(resources.getColor(R.color.error_text, theme))
                            })
                        }
                        card.addView(cardContent)
                        container.addView(card)
                    }
                }
            }
            override fun onFailure(call: Call<List<Conversacion>>, t: Throwable) {
                container.addView(TextView(this@MainOrbixActivity).apply {
                    text = "Error al cargar conversaciones"
                    setPadding(0, 32, 0, 0)
                    setTextColor(resources.getColor(R.color.error_text, theme))
                })
            }
        })
    }

    // ──────────── REPORTS (inline sub-screen) ────────────

    private fun showReportsInline() {
        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }
        scroll.addView(container)

        // Section: Sales KPIs
        container.addView(createReportSectionHeader("Ventas"))
        val ventasRow = createKpiRow()
        container.addView(ventasRow.first)
        val tvVentasIngresos = ventasRow.second[0]
        val tvVentasTickets = ventasRow.second[1]
        val tvVentasPromedio = ventasRow.second[2]

        // Section: Inventory KPIs
        container.addView(createReportSectionHeader("Inventario"))
        val invRow = createKpiRow()
        container.addView(invRow.first)
        val tvInvProductos = invRow.second[0]
        val tvInvAgotados = invRow.second[1]
        val tvInvValor = invRow.second[2]

        // Load real data
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        ApiClient.instance.getReporteVentasKpis().enqueue(object : Callback<VentaKpi> {
            override fun onResponse(call: Call<VentaKpi>, response: Response<VentaKpi>) {
                if (response.isSuccessful) {
                    val kpi = response.body() ?: return
tvVentasIngresos.text = format.format(kpi.total_ingresos)
tvVentasTickets.text = "${kpi.total_tickets}"
tvVentasPromedio.text = format.format(kpi.promedio_ticket)
                }
            }
            override fun onFailure(call: Call<VentaKpi>, t: Throwable) {}
        })

        ApiClient.instance.getReporteInventarioKpis().enqueue(object : Callback<InventarioKpi> {
            override fun onResponse(call: Call<InventarioKpi>, response: Response<InventarioKpi>) {
                if (response.isSuccessful) {
                    val kpi = response.body() ?: return
                    tvInvProductos.text = "${kpi.total_productos}"
                    tvInvAgotados.text = "${kpi.agotados}"
                    tvInvValor.text = format.format(kpi.valor_total_costo)
                }
            }
            override fun onFailure(call: Call<InventarioKpi>, t: Throwable) {}
        })

        // Navigation to detailed reports
        val reportTypes = listOf(
            "Ventas y Comprobantes" to "ventas",
            "Inventario y Ganancias" to "inventario",
            "Seguridad y Accesos" to "seguridad",
            "Carritos Activos" to "carritos",
            "Repartidores y Logística" to "repartidores"
        )

        for ((title, key) in reportTypes) {
            val card = com.google.android.material.card.MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 12 }
                radius = 12f
                cardElevation = 2f
                setCardBackgroundColor(resources.getColor(R.color.bg_card, theme))
                setContentPadding(16, 16, 16, 16)
                isClickable = true
                isFocusable = true
                setOnClickListener { navigateToReportDetail(title) }
            }
            val cardInner = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            cardInner.addView(TextView(this).apply {
                text = title
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.text_main, theme))
            })
            cardInner.addView(TextView(this).apply {
                text = "Ver detalle >"
                textSize = 12f
                setTextColor(resources.getColor(R.color.text_secondary, theme))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })
            card.addView(cardInner)
            container.addView(card)
        }

        contentContainer.addView(scroll)
    }

    private fun createReportSectionHeader(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16; bottomMargin = 8 }
        }
    }

    private fun createKpiRow(): Pair<LinearLayout, List<TextView>> {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val labels = listOf("Total", "Cantidad", "Promedio")
        val tvs = labels.map { label ->
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply { marginEnd = 8 }
                setPadding(12, 12, 12, 12)
                setBackgroundResource(R.drawable.bg_card_orbix)
                addView(TextView(this@MainOrbixActivity).apply {
                    id = android.R.id.text1
                    text = "-"; textSize = 16f
                    setTextColor(resources.getColor(R.color.text_main, theme))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })
                addView(TextView(this@MainOrbixActivity).apply {
                    text = label; textSize = 10f
                    setTextColor(resources.getColor(R.color.text_secondary, theme))
                })
            }.also { row.addView(it) }
        }.map { it.findViewById<TextView>(android.R.id.text1) }

        return Pair(row, tvs)
    }
}
