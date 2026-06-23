package com.example.nexbitmobile.ui

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.PedidoRequest
import com.example.nexbitmobile.model.Producto
import com.example.nexbitmobile.model.Usuario
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class CheckoutManualActivity : AppCompatActivity() {

    private lateinit var spinnerCliente: Spinner
    private lateinit var etDireccion: EditText
    private lateinit var etNotas: EditText
    private lateinit var spinnerProducto: Spinner
    private lateinit var btnAddProducto: View
    private lateinit var containerProductos: LinearLayout
    private lateinit var tvProductosEmpty: TextView
    private lateinit var flUploadComprobante: View
    private lateinit var ivComprobantePreview: ImageView
    private lateinit var flUploadContent: View
    private lateinit var tvSubtotal: TextView
    private lateinit var tvIva: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCancelar: TextView
    private lateinit var btnCrearPedido: TextView

    private val clientes = mutableListOf<Usuario>()
    private val productos = mutableListOf<Producto>()
    private val productosAgregados = mutableListOf<Producto>()
    private var selectedClienteId: Int? = null
    private var selectedProductoId: Int? = null
    private var comprobanteUri: Uri? = null

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            comprobanteUri = it
            ivComprobantePreview.setImageURI(it)
            ivComprobantePreview.visibility = View.VISIBLE
            flUploadContent.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_manual)

        spinnerCliente = findViewById(R.id.spinnerCliente)
        etDireccion = findViewById(R.id.etDireccion)
        etNotas = findViewById(R.id.etNotas)
        spinnerProducto = findViewById(R.id.spinnerProducto)
        btnAddProducto = findViewById(R.id.btnAddProducto)
        containerProductos = findViewById(R.id.containerProductos)
        tvProductosEmpty = findViewById(R.id.tvProductosEmpty)
        flUploadComprobante = findViewById(R.id.flUploadComprobante)
        ivComprobantePreview = findViewById(R.id.ivComprobantePreview)
        flUploadContent = findViewById(R.id.flUploadContent)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvIva = findViewById(R.id.tvIva)
        tvTotal = findViewById(R.id.tvTotal)
        btnCancelar = findViewById(R.id.btnCancelar)
        btnCrearPedido = findViewById(R.id.btnCrearPedido)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        btnCancelar.setOnClickListener { finish() }
        flUploadComprobante.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnAddProducto.setOnClickListener { agregarProducto() }
        btnCrearPedido.setOnClickListener { crearPedido() }

        cargarClientes()
        cargarProductos()
    }

    private fun cargarClientes() {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    clientes.clear()
                    clientes.addAll(response.body() ?: emptyList())
                    val names = clientes.map { "${it.nombre} (${it.email})" }
                    spinnerCliente.adapter = ArrayAdapter(
                        this@CheckoutManualActivity, android.R.layout.simple_spinner_item, names
                    ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    spinnerCliente.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                            selectedClienteId = clientes.getOrNull(pos)?.id_usuario
                        }
                        override fun onNothingSelected(p: AdapterView<*>?) {}
                    }
                    if (clientes.isNotEmpty()) selectedClienteId = clientes[0].id_usuario
                }
            }
            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(this@CheckoutManualActivity, "Error de red al cargar clientes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarProductos() {
        ApiClient.instance.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    productos.clear()
                    productos.addAll((response.body() ?: emptyList()).filter { it.stock_actual > 0 })
                    val names = productos.map { it.nombre }
                    spinnerProducto.adapter = ArrayAdapter(
                        this@CheckoutManualActivity, android.R.layout.simple_spinner_item, names
                    ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    spinnerProducto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                            selectedProductoId = productos.getOrNull(pos)?.id_producto
                        }
                        override fun onNothingSelected(p: AdapterView<*>?) {}
                    }
                    if (productos.isNotEmpty()) selectedProductoId = productos[0].id_producto
                }
            }
            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(this@CheckoutManualActivity, "Error de red al cargar productos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun agregarProducto() {
        val prod = productos.find { it.id_producto == selectedProductoId }
            ?: run { Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show(); return }
        productosAgregados.add(prod)
        renderProductos()
        recalcularTotales()
    }

    private fun removerProducto(pos: Int) {
        productosAgregados.removeAt(pos)
        renderProductos()
        recalcularTotales()
    }

    private fun renderProductos() {
        containerProductos.removeViews(1, containerProductos.childCount - 1)
        tvProductosEmpty.visibility = if (productosAgregados.isEmpty()) View.VISIBLE else View.GONE

        productosAgregados.forEachIndexed { i, prod ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_producto_agregado, containerProductos, false)
            row.findViewById<TextView>(R.id.tvProdNombre).text = prod.nombre
            row.findViewById<TextView>(R.id.tvProdPrecio).text = formatter.format(prod.precio_venta)
            row.findViewById<ImageView>(R.id.ivRemoveProducto).setOnClickListener { removerProducto(i) }
            containerProductos.addView(row)
        }
    }

    private fun recalcularTotales() {
        val subtotal = productosAgregados.sumOf { it.precio_venta }
        val iva = subtotal * 0.19
        val total = subtotal + iva

        tvSubtotal.text = formatter.format(subtotal)
        tvIva.text = formatter.format(iva)
        tvTotal.text = formatter.format(total)
    }

    private fun crearPedido() {
        val clienteId = selectedClienteId ?: run {
            Toast.makeText(this, "Selecciona un cliente", Toast.LENGTH_SHORT).show(); return
        }
        if (productosAgregados.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un producto", Toast.LENGTH_SHORT).show(); return
        }

        val subtotal = productosAgregados.sumOf { it.precio_venta }
        val total = subtotal + subtotal * 0.19

        val request = PedidoRequest(
            usuario_id = clienteId,
            total = total,
            estado = "EN_REVISION",
            direccion_entrega = etDireccion.text.toString().trim().ifEmpty { null },
            notas_entrega = etNotas.text.toString().trim().ifEmpty { null }
        )

        btnCrearPedido.isEnabled = false
        btnCrearPedido.text = "Creando..."

        ApiClient.instance.createPedido(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val pedidoId = extraerPedidoId(response)
                    if (comprobanteUri != null && pedidoId != null) {
                        subirComprobante(pedidoId)
                    } else {
                        Toast.makeText(this@CheckoutManualActivity, "Pedido creado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    btnCrearPedido.isEnabled = true
                    btnCrearPedido.text = "Crear Pedido"
                    Toast.makeText(this@CheckoutManualActivity, "Error al crear pedido", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnCrearPedido.isEnabled = true
                btnCrearPedido.text = "Crear Pedido"
                Toast.makeText(this@CheckoutManualActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun extraerPedidoId(response: Response<Void>): Int? {
        val location = response.headers().get("Location")
        return location?.split("/")?.lastOrNull()?.toIntOrNull()
    }

    private fun subirComprobante(pedidoId: Int) {
        val uri = comprobanteUri ?: return
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val fileName = getFileName(uri) ?: "comprobante.jpg"
        val mediaType = contentResolver.getType(uri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(mediaType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imagen", fileName, requestBody)

        ApiClient.instance.subirComprobante(pedidoId, imagePart).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Toast.makeText(this@CheckoutManualActivity, "Pedido creado con comprobante", Toast.LENGTH_SHORT).show()
                finish()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@CheckoutManualActivity, "Pedido creado, pero falló subir comprobante", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else null
            } else null
        }
    }
}
