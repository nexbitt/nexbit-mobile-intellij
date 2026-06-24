package com.example.nexbitmobile.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Categoria
import com.example.nexbitmobile.model.Producto
import com.example.nexbitmobile.model.Proveedor
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ProductosAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdminAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView
    private lateinit var llSkeleton: View

    private var allProductos = listOf<Producto>()
    private var categoriasList = listOf<Categoria>()
    private var proveedoresList = listOf<Proveedor>()
    
    private var currentImageUri: Uri? = null
    private var currentImageView: ImageView? = null
    private var currentFlUpload: View? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            currentImageView?.let { iv ->
                Glide.with(this).load(it).into(iv)
                iv.visibility = View.VISIBLE
            }
            currentFlUpload?.let { fl ->
                fl.foreground = null
                fl.findViewById<View>(R.id.flImageOverlay)?.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_admin)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        etSearch = findViewById(R.id.etSearch)
        tvEmpty = findViewById(R.id.tvEmpty)
        llSkeleton = findViewById(R.id.llSkeleton)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = ProductoAdminAdapter(
            emptyList(),
            onEdit = this::showEditDialog,
            onStock = { p -> Toast.makeText(this, "Stock: ${p.stock_actual} uds.", Toast.LENGTH_SHORT).show() }
        )
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterProducts()
            }
        })

        fabAdd.setOnClickListener { showCreateDialog() }

        loadCategorias()
        loadProveedores()
        loadProductos()
    }

    private fun filterProducts() {
        val query = etSearch.text.toString().trim().lowercase()
        val filtered = if (query.isEmpty()) {
            allProductos
        } else {
            allProductos.filter { p ->
                (p.nombre.lowercase().contains(query)) ||
                (p.categoria_nombre?.lowercase()?.contains(query) == true) ||
                (p.proveedor_nombre?.lowercase()?.contains(query) == true)
            }
        }
        adapter.updateData(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadCategorias() {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(call: Call<List<Categoria>>, response: Response<List<Categoria>>) {
                if (response.isSuccessful) {
                    categoriasList = response.body() ?: emptyList()
                } else {
                    Log.e("ProductosAdmin", "Error loading categories: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Categoria>>, t: Throwable) {
                Log.e("ProductosAdmin", "Category load failed", t)
            }
        })
    }

    private fun loadProductos() {
        llSkeleton.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        ApiClient.instance.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                llSkeleton.visibility = View.GONE
                if (response.isSuccessful) {
                    allProductos = response.body() ?: emptyList()
                    filterProducts()
                } else {
                    Toast.makeText(this@ProductosAdminActivity, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                llSkeleton.visibility = View.GONE
                Toast.makeText(this@ProductosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadProveedores() {
        ApiClient.instance.getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(call: Call<List<Proveedor>>, response: Response<List<Proveedor>>) {
                if (response.isSuccessful) {
                    proveedoresList = response.body() ?: emptyList()
                } else {
                    Log.e("ProductosAdmin", "Error loading proveedores: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Proveedor>>, t: Throwable) {
                Log.e("ProductosAdmin", "Proveedor load failed", t)
            }
        })
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val contentResolver = applicationContext.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                tempFile
            }
        } catch (e: Exception) {
            Log.e("ProductosAdmin", "Error converting URI to file", e)
            null
        }
    }

    private fun createPartFromString(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun showCreateDialog() {
        currentImageUri = null
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val spCategoria = view.findViewById<Spinner>(R.id.spCategoria)
        val spProveedor = view.findViewById<Spinner>(R.id.spProveedor)
        val etPrecioCompra = view.findViewById<EditText>(R.id.etPrecioCompra)
        val etPrecioVenta = view.findViewById<EditText>(R.id.etPrecioVenta)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val etStockMinimo = view.findViewById<EditText>(R.id.etStockMinimo)
        val spEstado = view.findViewById<Spinner>(R.id.spEstado)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val flImageUpload = view.findViewById<View>(R.id.flImageUpload)
        currentImageView = view.findViewById(R.id.ivPreview)
        currentFlUpload = flImageUpload

        val catNames = categoriasList.map { it.nombre }
        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)

        val provNames = listOf("Sin proveedor") + proveedoresList.map { it.nombre }
        spProveedor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provNames)

        val estados = arrayOf("Activo", "Inactivo")
        spEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        flImageUpload.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nuevo Producto")
            .setView(view)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            if (categoriasList.isEmpty()) {
                Toast.makeText(this, "No hay categorías disponibles. Crea una primero.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catId = categoriasList[spCategoria.selectedItemPosition].id_categoria
            val provId = if (spProveedor.selectedItemPosition > 0)
                proveedoresList[spProveedor.selectedItemPosition - 1].id_proveedor.toString() else ""
            val activo = if (spEstado.selectedItemPosition == 0) "1" else "0"

            var imagePart: MultipartBody.Part? = null
            currentImageUri?.let { uri ->
                getFileFromUri(uri)?.let { file ->
                    val mimeType = applicationContext.contentResolver.getType(uri) ?: "image/jpeg"
                    val reqFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("imagen", file.name, reqFile)
                }
            }

            ApiClient.instance.createProducto(
                createPartFromString(catId.toString()),
                createPartFromString(provId),
                createPartFromString(nombre),
                createPartFromString(etDescripcion.text.toString().trim()),
                createPartFromString(if(etPrecioCompra.text.isEmpty()) "0" else etPrecioCompra.text.toString()),
                createPartFromString(if(etPrecioVenta.text.isEmpty()) "0" else etPrecioVenta.text.toString()),
                createPartFromString(if(etStock.text.isEmpty()) "0" else etStock.text.toString()),
                createPartFromString(if(etStockMinimo.text.isEmpty()) "0" else etStockMinimo.text.toString()),
                createPartFromString(activo),
                imagePart
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductosAdminActivity, "Producto creado", Toast.LENGTH_SHORT).show()
                        loadProductos()
                        dialog.dismiss()
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Datos inválidos. Verifica los campos obligatorios."
                            401 -> "No autorizado"
                            else -> "Error al crear producto (${response.code()})"
                        }
                        Toast.makeText(this@ProductosAdminActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ProductosAdminActivity, "Error de conexión al crear producto", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showEditDialog(producto: Producto) {
        currentImageUri = null
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val spCategoria = view.findViewById<Spinner>(R.id.spCategoria)
        val spProveedor = view.findViewById<Spinner>(R.id.spProveedor)
        val etPrecioCompra = view.findViewById<EditText>(R.id.etPrecioCompra)
        val etPrecioVenta = view.findViewById<EditText>(R.id.etPrecioVenta)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val etStockMinimo = view.findViewById<EditText>(R.id.etStockMinimo)
        val spEstado = view.findViewById<Spinner>(R.id.spEstado)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val flImageUpload = view.findViewById<View>(R.id.flImageUpload)
        currentImageView = view.findViewById(R.id.ivPreview)
        currentFlUpload = flImageUpload

        val catNames = categoriasList.map { it.nombre }
        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)

        val provNames = listOf("Sin proveedor") + proveedoresList.map { it.nombre }
        spProveedor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provNames)

        val estados = arrayOf("Activo", "Inactivo")
        spEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)

        val catIndex = categoriasList.indexOfFirst { it.id_categoria == producto.categoria_id }
        if (catIndex >= 0) spCategoria.setSelection(catIndex)

        if (producto.proveedor_id != null) {
            val provIndex = proveedoresList.indexOfFirst { it.id_proveedor == producto.proveedor_id }
            if (provIndex >= 0) spProveedor.setSelection(provIndex + 1)
        }

        spEstado.setSelection(if (producto.activo == 1) 0 else 1)

        etNombre.setText(producto.nombre)
        etPrecioCompra.setText(producto.precio_compra.toString())
        etPrecioVenta.setText(producto.precio_venta.toString())
        etStock.setText(producto.stock_actual.toString())
        etStockMinimo.setText(producto.stock_minimo.toString())
        etDescripcion.setText(producto.descripcion ?: "")

        if (!producto.imagen_url.isNullOrEmpty()) {
            currentImageView?.let { iv ->
                iv.visibility = View.VISIBLE
                Glide.with(this).load(producto.imagen_url).into(iv)
            }
            currentFlUpload?.let { fl ->
                fl.foreground = null
                fl.findViewById<View>(R.id.flImageOverlay)?.visibility = View.GONE
            }
        }

        flImageUpload.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Producto")
            .setView(view)
            .setPositiveButton("Actualizar", null)
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            val catId = categoriasList[spCategoria.selectedItemPosition].id_categoria
            val provId = if (spProveedor.selectedItemPosition > 0)
                proveedoresList[spProveedor.selectedItemPosition - 1].id_proveedor.toString() else ""
            val activo = if (spEstado.selectedItemPosition == 0) "1" else "0"

            var imagePart: MultipartBody.Part? = null
            currentImageUri?.let { uri ->
                getFileFromUri(uri)?.let { file ->
                    val mimeType = applicationContext.contentResolver.getType(uri) ?: "image/jpeg"
                    val reqFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("imagen", file.name, reqFile)
                }
            }

            val imageUrlPart = if (imagePart == null && !producto.imagen_url.isNullOrEmpty()) {
                createPartFromString(producto.imagen_url)
            } else null

            ApiClient.instance.updateProducto(
                producto.id_producto,
                createPartFromString(catId.toString()),
                createPartFromString(provId),
                createPartFromString(nombre),
                createPartFromString(etDescripcion.text.toString().trim()),
                createPartFromString(etPrecioCompra.text.toString()),
                createPartFromString(etPrecioVenta.text.toString()),
                createPartFromString(etStock.text.toString()),
                createPartFromString(if(etStockMinimo.text.isEmpty()) "0" else etStockMinimo.text.toString()),
                createPartFromString(activo),
                imagePart,
                imageUrlPart
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductosAdminActivity, "Producto actualizado", Toast.LENGTH_SHORT).show()
                        loadProductos()
                        dialog.dismiss()
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Datos inválidos. Verifica los campos."
                            404 -> "Producto no encontrado"
                            else -> "Error al actualizar (${response.code()})"
                        }
                        Toast.makeText(this@ProductosAdminActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ProductosAdminActivity, "Error de conexión al actualizar", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun deleteProducto(producto: Producto) {
        val view = layoutInflater.inflate(R.layout.dialog_delete_confirm, null)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = "¿Deseas eliminar el producto ${producto.nombre}?"

        AlertDialog.Builder(this)
            .setView(view)
            .show()
            .apply {
                view.findViewById<View>(R.id.btnCancel).setOnClickListener { dismiss() }
                view.findViewById<View>(R.id.btnConfirm).setOnClickListener {
                    ApiClient.instance.deleteProducto(producto.id_producto).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Toast.makeText(this@ProductosAdminActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                            loadProductos()
                            dismiss()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("ProductosAdmin", "Delete product failed", t)
                            Toast.makeText(this@ProductosAdminActivity, "Error al eliminar producto", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
    }
}
