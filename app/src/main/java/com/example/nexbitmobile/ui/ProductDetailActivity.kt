package com.example.nexbitmobile.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.CarritoAddRequest
import com.example.nexbitmobile.model.CarritoItem
import com.example.nexbitmobile.model.Producto
import com.example.nexbitmobile.util.SecurePrefs
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private var productId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        productId = intent.getIntExtra("id_producto", -1)
        if (productId == -1) {
            Toast.makeText(this, "ID de producto inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup Toolbar back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            // Simple back navigation, preserves CatalogoActivity state naturally
            finish()
        }

        loadProductDetail()
    }

    private fun loadProductDetail() {
        findViewById<FrameLayout>(R.id.loadingOverlay).visibility = View.VISIBLE

        ApiClient.instance.getProducto(productId).enqueue(object : Callback<Producto> {
            override fun onResponse(call: Call<Producto>, response: Response<Producto>) {
                findViewById<FrameLayout>(R.id.loadingOverlay).visibility = View.GONE
                if (response.isSuccessful) {
                    val producto = response.body()
                    if (producto != null) {
                        populateView(producto)
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Error al cargar detalle (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Producto>, t: Throwable) {
                findViewById<FrameLayout>(R.id.loadingOverlay).visibility = View.GONE
                Toast.makeText(this@ProductDetailActivity, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun populateView(producto: Producto) {
        findViewById<TextView>(R.id.tvToolbarTitle).text = producto.nombre
        findViewById<TextView>(R.id.tvProductName).text = producto.nombre
        findViewById<TextView>(R.id.tvProductCategory).text = producto.categoria_nombre ?: "General"
        findViewById<TextView>(R.id.tvProductDescription).text =
            producto.descripcion ?: "No hay descripción detallada para este producto."

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        findViewById<TextView>(R.id.tvProductPrice).text = format.format(producto.precio_venta)

        val tvStock = findViewById<TextView>(R.id.tvProductStock)
        val btnAddToCart = findViewById<Button>(R.id.btnAddToCart)

        if (producto.stock_actual <= 0) {
            tvStock.text = "Agotado"
            tvStock.setTextColor(resources.getColor(R.color.error_text, theme))
            btnAddToCart.isEnabled = false
            btnAddToCart.text = "Sin stock"
            btnAddToCart.alpha = 0.5f
        } else {
            tvStock.text = "Stock disponible: ${producto.stock_actual}"
            tvStock.setTextColor(resources.getColor(R.color.text_secondary, theme))
            btnAddToCart.isEnabled = true
            btnAddToCart.text = "Agregar al carrito"
            btnAddToCart.alpha = 1.0f
        }

        // Technical info section
        findViewById<TextView>(R.id.tvTechId).text = "#${producto.id_producto}"
        findViewById<TextView>(R.id.tvTechSupplier).text = producto.proveedor_nombre ?: "No especificado"
        findViewById<TextView>(R.id.tvTechStock).text = "${producto.stock_actual} unidades"

        Glide.with(this)
            .load(producto.imagen_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(findViewById<ImageView>(R.id.ivProductImage))

        btnAddToCart.setOnClickListener {
            addToCart(producto)
        }
    }

    private fun addToCart(producto: Producto) {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val token = SecurePrefs.getToken(this) ?: ""

        if (userId == 0 || token.isEmpty()) {
            Toast.makeText(this, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CarritoAddRequest(
            usuario_id = userId,
            session_id = null,
            producto_id = producto.id_producto,
            cantidad = 1
        )

        findViewById<FrameLayout>(R.id.loadingOverlay).visibility = View.VISIBLE

        ApiClient.instance.addToCarrito(request).enqueue(object : Callback<List<CarritoItem>> {
            override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                findViewById<FrameLayout>(R.id.loadingOverlay).visibility = View.GONE
                if (response.isSuccessful) {
                    val snackbar = Snackbar.make(findViewById(R.id.main), "✓ ${producto.nombre} agregado al carrito", Snackbar.LENGTH_SHORT)
                    snackbar.setAction("Ver Carrito") {
                        startActivity(android.content.Intent(this@ProductDetailActivity, CarritoActivity::class.java))
                    }
                    snackbar.setActionTextColor(resources.getColor(R.color.secondary, theme))
                    snackbar.show()
                } else {
                    Snackbar.make(findViewById(R.id.main), "Error al agregar (${response.code()})", Snackbar.LENGTH_SHORT)
                        .setTextColor(resources.getColor(R.color.error_text, theme))
                        .show()
                }
            }

            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                findViewById<FrameLayout>(R.id.loadingOverlay).visibility = View.GONE
                Snackbar.make(findViewById(R.id.main), "Error de conexión", Snackbar.LENGTH_SHORT)
                    .setTextColor(resources.getColor(R.color.error_text, theme))
                    .show()
            }
        })
    }
}
