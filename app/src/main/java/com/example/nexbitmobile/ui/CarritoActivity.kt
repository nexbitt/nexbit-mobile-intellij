package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class CarritoActivity : AppCompatActivity() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var adapter: CarritoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmpty: LinearLayout
    private lateinit var llSummary: LinearLayout
    private lateinit var tvItemCount: TextView
    private lateinit var tvTotal: TextView

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private var userId = 0
    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)
        token = prefs.getString("token", "") ?: ""

        // Bind views
        rvCarrito = findViewById(R.id.rvCarrito)
        progressBar = findViewById(R.id.progressBar)
        llEmpty = findViewById(R.id.llEmpty)
        llSummary = findViewById(R.id.llSummary)
        tvItemCount = findViewById(R.id.tvItemCount)
        tvTotal = findViewById(R.id.tvTotal)

        // Setup RecyclerView
        adapter = CarritoAdapter(
            items = emptyList(),
            onQuantityChange = { item, newQty -> updateQuantity(item, newQty) },
            onRemove = { item -> removeItem(item) }
        )
        rvCarrito.layoutManager = LinearLayoutManager(this)
        rvCarrito.adapter = adapter

        // Back
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Clear cart
        findViewById<ImageButton>(R.id.btnClearCart).setOnClickListener { confirmClearCart() }

        // Go to catalog (from empty state)
        findViewById<Button>(R.id.btnGoToCatalog).setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
            finish()
        }

        // Checkout placeholder
        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            Toast.makeText(this, "Funcionalidad próximamente", Toast.LENGTH_SHORT).show()
        }

        loadCart()
    }

    private fun loadCart() {
        if (userId == 0 || token.isEmpty()) {
            showEmpty()
            return
        }

        progressBar.visibility = View.VISIBLE

        ApiClient.instance.getCarrito("Bearer $token", userId).enqueue(object : Callback<List<CarritoItem>> {
            override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    updateUI(items)
                } else {
                    Toast.makeText(this@CarritoActivity, "Error cargando carrito (${response.code()})", Toast.LENGTH_SHORT).show()
                    showEmpty()
                }
            }

            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@CarritoActivity, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
                showEmpty()
            }
        })
    }

    private fun updateUI(items: List<CarritoItem>) {
        if (items.isEmpty()) {
            showEmpty()
            return
        }

        llEmpty.visibility = View.GONE
        rvCarrito.visibility = View.VISIBLE
        llSummary.visibility = View.VISIBLE

        adapter.updateList(items)

        val totalItems = items.sumOf { it.cantidad }
        val totalPrice = items.sumOf { it.subtotal }

        tvItemCount.text = "$totalItems items"
        tvTotal.text = formatter.format(totalPrice)
    }

    private fun showEmpty() {
        llEmpty.visibility = View.VISIBLE
        rvCarrito.visibility = View.GONE
        llSummary.visibility = View.GONE
    }

    private fun updateQuantity(item: CarritoItem, newQty: Int) {
        val request = CarritoUpdateRequest(
            cantidad = newQty,
            usuario_id = userId,
            session_id = null
        )

        ApiClient.instance.updateCarritoItem("Bearer $token", item.id_carrito, request)
            .enqueue(object : Callback<List<CarritoItem>> {
                override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                    if (response.isSuccessful) {
                        updateUI(response.body() ?: emptyList())
                    } else {
                        Toast.makeText(this@CarritoActivity, "Error actualizando cantidad", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                    Toast.makeText(this@CarritoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun removeItem(item: CarritoItem) {
        ApiClient.instance.removeFromCarrito("Bearer $token", item.producto_id, userId)
            .enqueue(object : Callback<List<CarritoItem>> {
                override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                    if (response.isSuccessful) {
                        updateUI(response.body() ?: emptyList())
                        Toast.makeText(this@CarritoActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CarritoActivity, "Error eliminando item", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                    Toast.makeText(this@CarritoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmClearCart() {
        AlertDialog.Builder(this)
            .setTitle("Vaciar carrito")
            .setMessage("¿Estás seguro de que deseas vaciar todo el carrito?")
            .setPositiveButton("Vaciar") { _, _ -> clearCart() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun clearCart() {
        val request = CarritoClearRequest(usuario_id = userId, session_id = null)

        ApiClient.instance.clearCarrito("Bearer $token", request).enqueue(object : Callback<List<CarritoItem>> {
            override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                if (response.isSuccessful) {
                    updateUI(emptyList())
                    Toast.makeText(this@CarritoActivity, "Carrito vaciado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CarritoActivity, "Error vaciando carrito", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                Toast.makeText(this@CarritoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
