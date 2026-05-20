package com.example.nexbitmobile.ui

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Categoria
import com.example.nexbitmobile.model.Producto
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

    private var categoriasList = listOf<Categoria>()
    
    // Para manejo de imagen en el Dialog
    private var currentImageUri: Uri? = null
    private var currentImageView: ImageView? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            currentImageView?.let { iv ->
                Glide.with(this).load(it).into(iv)
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

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdminAdapter(emptyList(), this::showEditDialog, this::deleteProducto)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showCreateDialog() }

        loadCategorias()
        loadProductos()
    }

    private fun loadCategorias() {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(call: Call<List<Categoria>>, response: Response<List<Categoria>>) {
                if (response.isSuccessful) {
                    categoriasList = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<Categoria>>, t: Throwable) {}
        })
    }

    private fun loadProductos() {
        ApiClient.instance.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    adapter.updateData(response.body() ?: emptyList())
                } else {
                    Toast.makeText(this@ProductosAdminActivity, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(this@ProductosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFileFromUri(uri: Uri): File? {
        try {
            val contentResolver = applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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
        val etPrecioCompra = view.findViewById<EditText>(R.id.etPrecioCompra)
        val etPrecioVenta = view.findViewById<EditText>(R.id.etPrecioVenta)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnSeleccionarImagen = view.findViewById<Button>(R.id.btnSeleccionarImagen)
        currentImageView = view.findViewById(R.id.ivPreview)

        val catNames = categoriasList.map { it.nombre }
        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)

        btnSeleccionarImagen.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle("Nuevo Producto")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                if (categoriasList.isEmpty() || etNombre.text.isEmpty()) {
                    Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val catId = categoriasList[spCategoria.selectedItemPosition].id_categoria
                
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
                    createPartFromString(""), // proveedor
                    createPartFromString(etNombre.text.toString()),
                    createPartFromString(etDescripcion.text.toString()),
                    createPartFromString(if(etPrecioCompra.text.isEmpty()) "0" else etPrecioCompra.text.toString()),
                    createPartFromString(if(etPrecioVenta.text.isEmpty()) "0" else etPrecioVenta.text.toString()),
                    createPartFromString(if(etStock.text.isEmpty()) "0" else etStock.text.toString()),
                    createPartFromString("5"), // stock minimo default
                    createPartFromString("1"), // activo
                    imagePart
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@ProductosAdminActivity, "Producto creado", Toast.LENGTH_SHORT).show()
                        loadProductos()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(producto: Producto) {
        currentImageUri = null
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val spCategoria = view.findViewById<Spinner>(R.id.spCategoria)
        val etPrecioCompra = view.findViewById<EditText>(R.id.etPrecioCompra)
        val etPrecioVenta = view.findViewById<EditText>(R.id.etPrecioVenta)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnSeleccionarImagen = view.findViewById<Button>(R.id.btnSeleccionarImagen)
        currentImageView = view.findViewById(R.id.ivPreview)

        val catNames = categoriasList.map { it.nombre }
        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)

        val catIndex = categoriasList.indexOfFirst { it.id_categoria == producto.categoria_id }
        if (catIndex >= 0) spCategoria.setSelection(catIndex)

        etNombre.setText(producto.nombre)
        etPrecioCompra.setText(producto.precio_compra.toString())
        etPrecioVenta.setText(producto.precio_venta.toString())
        etStock.setText(producto.stock_actual.toString())
        etDescripcion.setText(producto.descripcion ?: "")

        if (!producto.imagen_url.isNullOrEmpty()) {
            Glide.with(this).load(producto.imagen_url).into(currentImageView!!)
        }

        btnSeleccionarImagen.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle("Editar Producto")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val catId = categoriasList[spCategoria.selectedItemPosition].id_categoria
                
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
                    createPartFromString(producto.proveedor_id?.toString() ?: ""),
                    createPartFromString(etNombre.text.toString()),
                    createPartFromString(etDescripcion.text.toString()),
                    createPartFromString(etPrecioCompra.text.toString()),
                    createPartFromString(etPrecioVenta.text.toString()),
                    createPartFromString(etStock.text.toString()),
                    createPartFromString(producto.stock_minimo.toString()),
                    createPartFromString(producto.activo.toString()),
                    imagePart,
                    imageUrlPart
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@ProductosAdminActivity, "Producto actualizado", Toast.LENGTH_SHORT).show()
                        loadProductos()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProducto(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Deseas eliminar el producto ${producto.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                ApiClient.instance.deleteProducto(producto.id_producto).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@ProductosAdminActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        loadProductos()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
            .setNegativeButton("No", null)
            .show()
    }
}
