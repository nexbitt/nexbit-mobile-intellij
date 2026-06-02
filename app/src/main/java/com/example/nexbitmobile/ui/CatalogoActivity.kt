package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
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

    private var allProductos: List<Producto> = emptyList()
    private var categorias: List<Categoria> = emptyList()
    private var selectedCategoriaId: Int? = null
    private var cartItemCount = 0

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
        tvCartBadge = findViewById(R.id.tvCartBadge)
        llCategoryChips = findViewById(R.id.llCategoryChips)
        etSearch = findViewById(R.id.etSearch)

        adapter = ProductoAdapter(
            emptyList(),
            onAddToCart = { producto -> addToCart(producto) },
            onItemClick = { producto ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("id_producto", producto.id_producto)
                }
                startActivity(intent)
            }
        )
        rvProductos.layoutManager = GridLayoutManager(this, 2)
        rvProductos.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<FrameLayout>(R.id.btnCartContainer).setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterProducts()
            }
        })

        loadProducts()
        loadCategories()
        loadCartCount()
    }

    override fun onResume() {
        super.onResume()
        loadCartCount()
    }

    private fun loadProducts() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        ApiClient.instance.getProductosPublico().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    allProductos = response.body() ?: emptyList()
                    filterProducts()
                } else {
                    tvEmpty.text = "Error cargando productos (${response.code()})"
                    tvEmpty.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvEmpty.text = "Sin conexión al servidor"
                tvEmpty.visibility = View.VISIBLE
            }
        })
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
        tvEmpty.visibility = if (filtered.isEmpty() && progressBar.visibility != View.VISIBLE)
            View.VISIBLE else View.GONE
        rvProductos.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun addToCart(producto: Producto) {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)

        if (userId == 0) {
            Toast.makeText(this, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
            return
        }

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
                    Toast.makeText(this@CatalogoActivity, "${producto.nombre} agregado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CatalogoActivity, "Error al agregar (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                Toast.makeText(this@CatalogoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
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
        cartItemCount = count
        if (count > 0) {
            tvCartBadge.text = if (count > 99) "99+" else count.toString()
            tvCartBadge.visibility = View.VISIBLE
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }
}
