package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.app.AlertDialog
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.api.SocketManager
import com.example.nexbitmobile.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogoActivity : AppCompatActivity() {

    private lateinit var rvProductos: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvCartBadge: TextView
    private lateinit var llCategoryChips: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var btnRetry: Button
    private lateinit var btnEntrar: Button
    private lateinit var ivProfileAvatar: ImageView

    private var allProductos: List<Producto> = emptyList()
    private var categorias: List<Categoria> = emptyList()
    private var selectedCategoriaId: Int? = null
    private var cartItemCount = 0

    private var isLoggedIn = false
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalogo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvProductos = findViewById(R.id.rvProductos)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnRetry = findViewById(R.id.btnRetry)
        tvCartBadge = findViewById(R.id.tvCartBadge)
        llCategoryChips = findViewById(R.id.llCategoryChips)
        etSearch = findViewById(R.id.etSearch)
        btnEntrar = findViewById(R.id.btnEntrar)
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar)

        checkAuthState()

        adapter = ProductoAdapter(
            emptyList(),
            onAddToCart = { producto -> handleAddToCart(producto) },
            onItemClick = { producto ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("id_producto", producto.id_producto)
                }
                startActivity(intent)
            },
            onVerFichaClick = { producto -> mostrarFichaTecnica(producto) }
        )
        rvProductos.layoutManager = GridLayoutManager(this, 2)
        rvProductos.adapter = adapter

        btnEntrar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        ivProfileAvatar.setOnClickListener {
            if (isAdmin) {
                startActivity(Intent(this, MainOrbixActivity::class.java))
            } else {
                startActivity(Intent(this, ClientProfileActivity::class.java))
            }
        }

        findViewById<FrameLayout>(R.id.btnCartContainer).setOnClickListener {
            if (isLoggedIn) {
                startActivity(Intent(this, CarritoActivity::class.java))
            } else {
                showLoginBottomSheet()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterProducts()
            }
        })

        btnRetry.setOnClickListener { loadProducts() }

        loadProducts()
        loadCategories()
        if (isLoggedIn) loadCartCount()

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
                    "pedido-estado" -> {
                        val pedidoId = data.optInt("pedido_id", 0)
                        val estado = data.optString("estado", "")
                        NotificationToastHelper.show(
                            "Pedido Actualizado",
                            "Tu pedido #$pedidoId ahora está: $estado",
                            "🔄"
                        )
                    }
                    "pago-aprobado" -> {
                        val pedidoId = data.optInt("pedido_id", 0)
                        NotificationToastHelper.show(
                            "Pago Aprobado",
                            "El pago del pedido #$pedidoId fue aprobado. ¡Gracias!",
                            "✅"
                        )
                    }
                    "pago-rechazado" -> {
                        val pedidoId = data.optInt("pedido_id", 0)
                        NotificationToastHelper.show(
                            "Pago Rechazado",
                            "El pago del pedido #$pedidoId fue rechazado. Revisa el motivo.",
                            "❌"
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAuthState()
        if (isLoggedIn) loadCartCount()
    }

    private fun checkAuthState() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val rolId = prefs.getInt("rolId", 0)
        val avatarUrl = prefs.getString("userAvatar", "") ?: ""

        isLoggedIn = !token.isNullOrEmpty()
        isAdmin = rolId == 1

        if (isLoggedIn) {
            btnEntrar.visibility = View.GONE
            ivProfileAvatar.visibility = View.VISIBLE
            if (avatarUrl.isNotEmpty()) {
                Glide.with(this).load(avatarUrl).circleCrop().into(ivProfileAvatar)
            }
        } else {
            btnEntrar.visibility = View.VISIBLE
            ivProfileAvatar.visibility = View.GONE
        }
    }

    private fun handleAddToCart(producto: Producto) {
        if (!isLoggedIn) {
            showLoginBottomSheet()
            return
        }
        addToCart(producto)
    }

    private fun mostrarFichaTecnica(producto: Producto) {
        val specs = StringBuilder()
        specs.append("📋 Ficha Técnica\n")
        specs.append("─────────────────\n")
        specs.append("Nombre:     ${producto.nombre}\n")
        specs.append("Categoría:  ${producto.categoria_nombre ?: "General"}\n")
        specs.append("Proveedor:  ${producto.proveedor_nombre ?: "N/A"}\n")
        specs.append("Precio:     $${String.format("%,.2f", producto.precio_venta)}\n")
        specs.append("Stock:      ${producto.stock_actual} / ${producto.stock_minimo} (mín.)\n")
        specs.append("Estado:     ${if (producto.activo == 1) "Activo" else "Inactivo"}\n")
        if (!producto.descripcion.isNullOrBlank()) {
            specs.append("\n📝 Descripción:\n${producto.descripcion}\n")
        }
        specs.append("\nID Producto:  ${producto.id_producto}")
        specs.append("\nID Categoría: ${producto.categoria_id ?: "N/A"}")

        AlertDialog.Builder(this)
            .setTitle("Ficha Técnica")
            .setMessage(specs.toString())
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Agregar al carrito") { _, _ ->
                handleAddToCart(producto)
            }
            .show()
    }

    private fun showLoginBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_login_prompt, null)
        view.findViewById<Button>(R.id.btnLoginPrompt).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        view.findViewById<Button>(R.id.btnRegisterPrompt).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, RegistroActivity::class.java))
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun loadProducts() {
        progressBar.visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.llEmptyState).visibility = View.GONE
        rvProductos.visibility = View.GONE

        ApiClient.instance.getProductosPublico().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    allProductos = response.body() ?: emptyList()
                    filterProducts()
                } else {
                    showError("Error al cargar productos (${response.code()})")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                progressBar.visibility = View.GONE
                showError("Sin conexión al servidor")
            }
        })
    }

    private fun showError(message: String) {
        tvEmpty.text = message
        btnRetry.visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.llEmptyState).visibility = View.VISIBLE
        rvProductos.visibility = View.GONE
    }

    private fun loadCategories() {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(call: Call<List<Categoria>>, response: Response<List<Categoria>>) {
                if (response.isSuccessful) {
                    categorias = response.body() ?: emptyList()
                    buildCategoryChips()
                }
            }

            override fun onFailure(call: Call<List<Categoria>>, t: Throwable) {
            }
        })
    }

    private fun buildCategoryChips() {
        llCategoryChips.removeAllViews()
        addChip("Todos", null, selectedCategoriaId == null)
        for (cat in categorias) {
            addChip(cat.nombre, cat.id_categoria, selectedCategoriaId == cat.id_categoria)
        }
    }

    private fun addChip(label: String, catId: Int?, isSelected: Boolean) {
        val chip = TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(
                if (isSelected) resources.getColor(R.color.chip_selected_text, theme)
                else resources.getColor(R.color.chip_text, theme)
            )
            setBackgroundResource(
                if (isSelected) R.drawable.bg_chip_selected
                else R.drawable.bg_chip
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = 8
            layoutParams = params
            setOnClickListener {
                selectedCategoriaId = catId
                buildCategoryChips()
                filterProducts()
            }
        }
        llCategoryChips.addView(chip)
    }

    private fun filterProducts() {
        val query = etSearch.text.toString().trim().lowercase()
        var filtered = allProductos
        if (selectedCategoriaId != null) {
            filtered = filtered.filter { it.categoria_id == selectedCategoriaId }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.nombre.lowercase().contains(query) ||
                        (it.descripcion?.lowercase()?.contains(query) == true)
            }
        }
        adapter.updateList(filtered)
        val isEmpty = filtered.isEmpty() && progressBar.visibility != View.VISIBLE
        findViewById<LinearLayout>(R.id.llEmptyState).visibility = if (isEmpty) View.VISIBLE else View.GONE
        btnRetry.visibility = View.GONE
        tvEmpty.text = "No hay productos disponibles"
        rvProductos.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun addToCart(producto: Producto) {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        if (userId == 0) return

        val request = CarritoAddRequest(
            usuario_id = userId,
            session_id = null,
            producto_id = producto.id_producto,
            cantidad = 1
        )

        ApiClient.instance.addToCarrito(request).enqueue(object : Callback<List<CarritoItem>> {
            override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                if (response.isSuccessful) {
                    val cart = response.body() ?: emptyList()
                    updateCartBadge(cart.size)
                    showCartAddedSnackbar(producto)
                } else {
                    showCartErrorSnackbar("Error al agregar (${response.code()})")
                }
            }

            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                showCartErrorSnackbar("Error de conexión")
            }
        })
    }

    private fun showCartAddedSnackbar(producto: Producto) {
        val snackbar = Snackbar.make(findViewById(R.id.main), "${producto.nombre} agregado", Snackbar.LENGTH_SHORT)
        snackbar.setAction("Ver Carrito") {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
        snackbar.setActionTextColor(resources.getColor(R.color.secondary, theme))
        snackbar.show()
    }

    private fun showCartErrorSnackbar(msg: String) {
        val snackbar = Snackbar.make(findViewById(R.id.main), msg, Snackbar.LENGTH_SHORT)
        snackbar.setTextColor(resources.getColor(R.color.error_text, theme))
        snackbar.show()
    }

    private fun loadCartCount() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        if (userId == 0) return

        ApiClient.instance.getCarrito(userId).enqueue(object : Callback<List<CarritoItem>> {
            override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                if (response.isSuccessful) {
                    updateCartBadge(response.body()?.size ?: 0)
                }
            }

            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {}
        })
    }

    private fun updateCartBadge(count: Int) {
        val wasEmpty = cartItemCount == 0
        cartItemCount = count
        if (count > 0) {
            tvCartBadge.text = if (count > 99) "99+" else count.toString()
            tvCartBadge.visibility = View.VISIBLE
            if (!wasEmpty) {
                val bump = AnimationUtils.loadAnimation(this, R.anim.badge_bump)
                tvCartBadge.startAnimation(bump)
            }
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }
}
