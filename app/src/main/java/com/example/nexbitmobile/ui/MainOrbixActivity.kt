package com.example.nexbitmobile.ui

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.PathInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.api.SocketManager
import com.example.nexbitmobile.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
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

    // ─── Carousel ───
    private lateinit var carouselPager: ViewPager2
    private lateinit var pageIndicator: LinearLayout
    private lateinit var expandedContainer: FrameLayout
    private lateinit var carouselCard: MaterialCardView
    private var carouselHandler = Handler(Looper.getMainLooper())
    private var isExpanded = false
    private var pageIndicatorDots = mutableListOf<View>()
    private val carouselLayouts = listOf(
        R.layout.expanded_sales,
        R.layout.expanded_inventory,
        R.layout.expanded_security,
        R.layout.expanded_carts,
        R.layout.expanded_logistics
    )

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
                    stopAutoRotate()
                    isExpanded = false
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
                closeMenu()
                val intent = Intent(this, PedidosAdminActivity::class.java)
                intent.putExtra("isClienteView", false)
                startActivity(intent)
                toolbarSub.visibility = View.GONE
                navStack.removeLastOrNull()
                currentScreen = navStack.lastOrNull() ?: "home"
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
            "home" -> { showHome(); startAutoRotate() }
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
            startActivity(Intent(this, CheckoutManualActivity::class.java))
        }

        rv.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter(emptyList()) { pedido ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("pedido_id", pedido.id_pedido)
            startActivity(intent)
        }
        rv.adapter = adapter

        // Status filter chips
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
                // Reset all chips to unselected
                for ((id, _) in chipMap) {
                    val chip = root.findViewById<TextView>(id)
                    chip.setBackgroundResource(R.drawable.bg_chip)
                    chip.setTextColor(resources.getColor(R.color.chip_text, theme))
                }
                // Highlight selected chip
                val selectedChip = root.findViewById<TextView>(chipId)
                selectedChip.setBackgroundResource(R.drawable.bg_chip_selected)
                selectedChip.setTextColor(resources.getColor(R.color.chip_selected_text, theme))
                // Apply filters
                aplicarFiltros(adapter, etSearch)
            }
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                aplicarFiltros(adapter, etSearch)
            }
        })

        loadPedidos(rv, tvEstado, tvEmpty, adapter)
    }

    private fun aplicarFiltros(adapter: OrderAdapter, etSearch: EditText) {
        val query = etSearch.text.toString().trim().lowercase()
        val filtered = allPedidos.filter { p ->
            val matchesSearch = query.isEmpty() ||
                p.id_pedido.toString().contains(query) ||
                (p.usuario_nombre?.lowercase()?.contains(query) == true)
            val matchesStatus = currentStatusFilter == null ||
                p.estado.lowercase().contains(currentStatusFilter!!)
            matchesSearch && matchesStatus
        }
        adapter.updateList(filtered)
    }

    private var allPedidos = listOf<Pedido>()

    private fun loadPedidos(rv: RecyclerView, tvEstado: TextView, tvEmpty: TextView, adapter: OrderAdapter) {
        ApiClient.instance.getPedidos().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
                    allPedidos = response.body() ?: emptyList()
                    adapter.updateList(allPedidos)
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
        isExpanded = false
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

        // Carousel setup
        carouselPager = view.findViewById(R.id.carouselPager)
        pageIndicator = view.findViewById(R.id.pageIndicator)
        expandedContainer = view.findViewById(R.id.expandedContainer)
        carouselCard = view.findViewById(R.id.carouselCard)

        setupCarousel()
        setupPageIndicator()

        loadTopProducts(view)
        loadHomeStats(view)
    }

    // ──────────── CAROUSEL ────────────

    private fun setupCarousel() {
        carouselPager.adapter = CarouselAdapter { position -> onCarouselPageClick(position) }
        carouselPager.offscreenPageLimit = 5
        carouselPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
                stopAutoRotate()
                startAutoRotate()
            }
        })

        // Pause auto-rotation on touch
        carouselPager.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) stopAutoRotate()
            if (event.action == MotionEvent.ACTION_UP) startAutoRotate()
            false
        }

        startAutoRotate()
    }

    private fun setupPageIndicator() {
        pageIndicator.removeAllViews()
        pageIndicatorDots.clear()
        for (i in 0 until 5) {
            val dot = View(this)
            val size = if (i == 0) 12 else 6
            val lp = LinearLayout.LayoutParams(dp(size), 6)
            lp.marginEnd = 4
            dot.layoutParams = lp
            dot.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 3f
                setColor(
                    if (i == 0) resources.getColor(R.color.nav_active, theme)
                    else resources.getColor(R.color.tab_inactive, theme)
                )
            }
            pageIndicator.addView(dot)
            pageIndicatorDots.add(dot)
        }
    }

    private fun updatePageIndicator(active: Int) {
        for ((i, dot) in pageIndicatorDots.withIndex()) {
            val isActive = i == active
            val size = if (isActive) 12 else 6
            val lp = dot.layoutParams
            lp.width = dp(size)
            lp.height = 6
            dot.layoutParams = lp
            val bg = dot.background as android.graphics.drawable.GradientDrawable
            bg.setColor(
                if (isActive) resources.getColor(R.color.nav_active, theme)
                else resources.getColor(R.color.tab_inactive, theme)
            )
        }
    }

    private var autoRotateRunnable: Runnable? = null

    private fun startAutoRotate() {
        stopAutoRotate()
        if (isExpanded) return
        autoRotateRunnable = Runnable {
            val next = (carouselPager.currentItem + 1) % 5
            carouselPager.setCurrentItem(next, true)
        }
        carouselHandler.postDelayed(autoRotateRunnable!!, 5000)
    }

    private fun stopAutoRotate() {
        autoRotateRunnable?.let { carouselHandler.removeCallbacks(it) }
        autoRotateRunnable = null
    }

    // ──────────── TAP TO NAVIGATE TO REPORT LIST ────────────

    private fun onCarouselPageClick(position: Int) {
        stopAutoRotate()
        navigateToReportList()
    }

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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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
