package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.nexbitmobile.util.SecurePrefs
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
    private var cartTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carrito)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)
        token = SecurePrefs.getToken(this) ?: ""

        rvCarrito = findViewById(R.id.rvCarrito)
        progressBar = findViewById(R.id.progressBar)
        llEmpty = findViewById(R.id.llEmpty)
        llSummary = findViewById(R.id.llSummary)
        tvItemCount = findViewById(R.id.tvItemCount)
        tvTotal = findViewById(R.id.tvTotal)

        adapter = CarritoAdapter(
            items = emptyList(),
            onQuantityChange = { item, newQty -> updateQuantity(item, newQty) },
            onRemove = { item -> removeItem(item) }
        )
        rvCarrito.layoutManager = LinearLayoutManager(this)
        rvCarrito.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnClearCart).setOnClickListener { confirmClearCart() }
        findViewById<Button>(R.id.btnGoToCatalog).setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
            finish()
        }
        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            if (cartTotal <= 0.0) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            confirmCheckout()
        }

        loadCart()
    }

    private fun loadCart() {
        if (userId == 0 || token.isEmpty()) {
            showEmpty()
            return
        }

        progressBar.visibility = View.VISIBLE

        ApiClient.instance.getCarrito(userId).enqueue(object : Callback<List<CarritoItem>> {
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
            cartTotal = 0.0
            showEmpty()
            return
        }

        llEmpty.visibility = View.GONE
        rvCarrito.visibility = View.VISIBLE
        llSummary.visibility = View.VISIBLE

        adapter.updateList(items)

        val totalItems = items.sumOf { it.cantidad }
        val totalPrice = items.sumOf { it.subtotal }
        cartTotal = totalPrice

        tvItemCount.text = "$totalItems items"
        tvTotal.text = formatter.format(totalPrice)
    }

    private fun showEmpty() {
        cartTotal = 0.0
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

        ApiClient.instance.updateCarritoItem(item.id_carrito, request)
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
        ApiClient.instance.removeFromCarrito(item.producto_id, userId)
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

        ApiClient.instance.clearCarrito(request).enqueue(object : Callback<List<CarritoItem>> {
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

    private fun confirmCheckout() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
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
                    realizarPedido(direccion)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun realizarPedido(direccionEntrega: String) {
        if (userId == 0 || token.isEmpty()) {
            Toast.makeText(this, "Inicia sesión para continuar", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        val request = CheckoutRequest(
            usuario_id = userId,
            direccion_entrega = direccionEntrega
        )

        ApiClient.instance.checkout(request).enqueue(object : Callback<CheckoutResponse> {
            override fun onResponse(call: Call<CheckoutResponse>, response: Response<CheckoutResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    showSuccessDialog(response.body()?.id_pedido)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(this@CarritoActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@CarritoActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSuccessDialog(pedidoId: Int? = null) {
        val msg = if (pedidoId != null) "Tu pedido #$pedidoId ha sido registrado con éxito.\n¡Ahora debes subir el comprobante de pago!"
                  else "Tu pedido ha sido registrado con éxito en el sistema."
        AlertDialog.Builder(this)
            .setTitle("Pedido Realizado!")
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("Subir Comprobante") { _, _ ->
                val intent = Intent(this, ConfirmarPedidoActivity::class.java)
                intent.putExtra("pedido_id", pedidoId ?: 0)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Ver Mis Pedidos") { _, _ ->
                startActivity(Intent(this, MisPedidosActivity::class.java))
                finish()
            }
            .show()
    }
}
