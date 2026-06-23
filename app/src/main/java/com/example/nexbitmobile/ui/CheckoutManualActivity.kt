package com.example.nexbitmobile.ui

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var actvCliente: AutoCompleteTextView
    private lateinit var etDireccion: EditText
    private lateinit var etNotas: EditText
    private lateinit var actvProducto: AutoCompleteTextView
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
    private val productosAgregados = mutableListOf<ProductoConCantidad>()
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

    private data class ProductoConCantidad(
        val producto: Producto,
        var cantidad: Int
    )

    private data class ClienteItem(val label: String, val id: Int) {
        override fun toString(): String = label
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_manual)

        actvCliente = findViewById(R.id.actvCliente)
        etDireccion = findViewById(R.id.etDireccion)
        etNotas = findViewById(R.id.etNotas)
        actvProducto = findViewById(R.id.actvProducto)
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

        actvCliente.threshold = 1
        actvProducto.threshold = 1

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        btnCancelar.setOnClickListener { finish() }
        btnAddProducto.setOnClickListener { agregarProducto() }
        flUploadComprobante.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnCrearPedido.setOnClickListener { crearPedido() }

        cargarClientes()
        cargarProductos()
    }

    private fun cargarClientes() {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    clientes.clear()
                    val todos = response.body() ?: emptyList()
                    clientes.addAll(todos.filter { it.rol_id == 2 })
                    val items = clientes.map { ClienteItem("${it.nombre} (${it.email})", it.id_usuario) }

                    val adapter = object : ArrayAdapter<ClienteItem>(
                        this@CheckoutManualActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        items
                    ) {
                        override fun getFilter(): Filter {
                            return object : Filter() {
                                override fun performFiltering(constraint: CharSequence?): FilterResults {
                                    val q = constraint?.toString()?.lowercase() ?: ""
                                    val results = FilterResults()
                                    results.values = if (q.isEmpty()) items
                                    else items.filter { it.label.lowercase().contains(q) }
                                    results.count = (results.values as List<*>).size
                                    return results
                                }

                                @Suppress("UNCHECKED_CAST")
                                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                                    clear()
                                    if (results != null && results.count > 0) {
                                        addAll(results.values as List<ClienteItem>)
                                    }
                                    notifyDataSetChanged()
                                }
                            }
                        }
                    }
                    actvCliente.setAdapter(adapter)

                    actvCliente.setOnItemClickListener { _, _, position, _ ->
                        val item = adapter.getItem(position)
                        selectedClienteId = item?.id
                    }
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

                    val adapter = object : ArrayAdapter<String>(
                        this@CheckoutManualActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        names
                    ) {
                        override fun getFilter(): Filter {
                            return object : Filter() {
                                override fun performFiltering(constraint: CharSequence?): FilterResults {
                                    val q = constraint?.toString()?.lowercase() ?: ""
                                    val results = FilterResults()
                                    results.values = if (q.isEmpty()) names
                                    else names.filter { it.lowercase().contains(q) }
                                    results.count = (results.values as List<*>).size
                                    return results
                                }

                                @Suppress("UNCHECKED_CAST")
                                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                                    clear()
                                    if (results != null && results.count > 0) {
                                        addAll(results.values as List<String>)
                                    }
                                    notifyDataSetChanged()
                                }
                            }
                        }
                    }
                    actvProducto.setAdapter(adapter)

                    actvProducto.setOnItemClickListener { _, _, position, _ ->
                        val selectedName = adapter.getItem(position)
                        val prod = productos.find { it.nombre == selectedName }
                        selectedProductoId = prod?.id_producto
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

        val existing = productosAgregados.indexOfFirst { it.producto.id_producto == prod.id_producto }
        if (existing >= 0) {
            productosAgregados[existing].cantidad++
        } else {
            productosAgregados.add(ProductoConCantidad(prod, 1))
        }
        actvProducto.setText("")
        actvProducto.clearFocus()
        renderProductos()
        recalcularTotales()
    }

    private fun renderProductos() {
        containerProductos.removeViews(1, containerProductos.childCount - 1)
        tvProductosEmpty.visibility = if (productosAgregados.isEmpty()) View.VISIBLE else View.GONE

        productosAgregados.forEachIndexed { i, item ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_producto_qty, containerProductos, false)
            row.findViewById<TextView>(R.id.tvProdNombre).text = item.producto.nombre
            row.findViewById<TextView>(R.id.tvProdSubtotal).text = formatter.format(item.producto.precio_venta * item.cantidad)
            row.findViewById<TextView>(R.id.tvQtyValue).text = item.cantidad.toString()

            row.findViewById<ImageView>(R.id.ivQtyMinus).setOnClickListener { actualizarCantidad(i, -1) }
            row.findViewById<ImageView>(R.id.ivQtyPlus).setOnClickListener { actualizarCantidad(i, 1) }
            row.findViewById<View>(R.id.btnRemoveProducto).setOnClickListener { removerProducto(i) }

            containerProductos.addView(row)
        }
    }

    private fun actualizarCantidad(index: Int, delta: Int) {
        if (index !in productosAgregados.indices) return
        val item = productosAgregados[index]
        val nueva = item.cantidad + delta
        if (nueva < 1) {
            removerProducto(index)
            return
        }
        item.cantidad = nueva
        renderProductos()
        recalcularTotales()
    }

    private fun removerProducto(index: Int) {
        if (index !in productosAgregados.indices) return
        productosAgregados.removeAt(index)
        renderProductos()
        recalcularTotales()
    }

    private fun recalcularTotales() {
        val subtotal = productosAgregados.sumOf { it.producto.precio_venta * it.cantidad }
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

        val subtotal = productosAgregados.sumOf { it.producto.precio_venta * it.cantidad }
        val total = subtotal + subtotal * 0.19
        val estado = if (comprobanteUri != null) "EN_REVISION" else "PENDIENTE_DE_PAGO"

        val request = PedidoRequest(
            usuario_id = clienteId,
            total = total,
            estado = estado,
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
                        Toast.makeText(this@CheckoutManualActivity, "Pedido creado exitosamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    btnCrearPedido.isEnabled = true
                    btnCrearPedido.text = "Crear Pedido"
                    Toast.makeText(this@CheckoutManualActivity, "Error al crear pedido (${response.code()})", Toast.LENGTH_SHORT).show()
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
