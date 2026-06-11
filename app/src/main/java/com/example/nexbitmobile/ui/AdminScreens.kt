package com.example.nexbitmobile.ui

import android.app.AlertDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
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

class AdminScreens(private val activity: MainOrbixActivity) {

    private var categoriasList = listOf<Categoria>()
    private var proveedoresList = listOf<Proveedor>()
    private var allProductos = listOf<Producto>()
    private var currentImageUri: Uri? = null
    private var currentImageView: ImageView? = null
    private val selectImageLauncher = activity.registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            currentImageView?.let { iv -> Glide.with(activity).load(it).into(iv) }
        }
    }

    // ──────────── PRODUCTOS ADMIN ────────────

    fun showProductos(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val fab = root.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)
        val etSearch = root.findViewById<EditText>(R.id.etSearch)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        rv.layoutManager = LinearLayoutManager(activity)
        val adapter = ProductoAdminAdapter(emptyList(),
            { p -> showEditDialog(p, rv, tvEmpty) },
            { p -> deleteProducto(p, rv, tvEmpty) }
        )
        rv.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = etSearch.text.toString().trim().lowercase()
                val filtered = if (query.isEmpty()) allProductos
                else allProductos.filter { p ->
                    p.nombre.lowercase().contains(query) ||
                        (p.categoria_nombre?.lowercase()?.contains(query) == true)
                }
                (rv.adapter as ProductoAdminAdapter).updateData(filtered)
                tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                rv.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        fab.setOnClickListener { showCreateDialog(rv, tvEmpty) }

        loadCategorias()
        loadProveedores()
        loadProductos(rv, tvEmpty, adapter)
    }

    private fun loadCategorias() {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(call: Call<List<Categoria>>, response: Response<List<Categoria>>) {
                if (response.isSuccessful) categoriasList = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Categoria>>, t: Throwable) {}
        })
    }

    private fun loadProveedores() {
        ApiClient.instance.getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(call: Call<List<Proveedor>>, response: Response<List<Proveedor>>) {
                if (response.isSuccessful) proveedoresList = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Proveedor>>, t: Throwable) {}
        })
    }

    private fun loadProductos(rv: RecyclerView, tvEmpty: TextView, adapter: ProductoAdminAdapter) {
        ApiClient.instance.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    allProductos = response.body() ?: emptyList()
                    adapter.updateData(allProductos)
                    rv.visibility = if (allProductos.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (allProductos.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateDialog(rv: RecyclerView, tvEmpty: TextView) {
        currentImageUri = null
        buildProductDialog(null, "Nuevo Producto", "Guardar") { p -> createProducto(p, rv, tvEmpty) }
    }

    private fun showEditDialog(producto: Producto, rv: RecyclerView, tvEmpty: TextView) {
        currentImageUri = null
        buildProductDialog(producto, "Editar Producto", "Actualizar") { p -> updateProducto(p, rv, tvEmpty, producto) }
    }

    private data class ProductFormData(
        val nombre: String, val descripcion: String,
        val precioCompra: String, val precioVenta: String,
        val stock: String, val stockMinimo: String,
        val categoriaId: Int, val proveedorId: String,
        val activo: String, val imageUri: Uri?
    )

    private fun buildProductDialog(existing: Producto?, title: String, btnText: String, onSave: (ProductFormData) -> Unit) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_producto, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val spCategoria = view.findViewById<Spinner>(R.id.spCategoria)
        val spProveedor = view.findViewById<Spinner>(R.id.spProveedor)
        val etPrecioCompra = view.findViewById<EditText>(R.id.etPrecioCompra)
        val etPrecioVenta = view.findViewById<EditText>(R.id.etPrecioVenta)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val etStockMinimo = view.findViewById<EditText>(R.id.etStockMinimo)
        val spEstado = view.findViewById<Spinner>(R.id.spEstado)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnSeleccionarImagen = view.findViewById<Button>(R.id.btnSeleccionarImagen)
        currentImageView = view.findViewById(R.id.ivPreview)

        spCategoria.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, categoriasList.map { it.nombre })
        spProveedor.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, listOf("Sin proveedor") + proveedoresList.map { it.nombre })
        spEstado.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Activo", "Inactivo"))

        if (existing != null) {
            val ci = categoriasList.indexOfFirst { it.id_categoria == existing.categoria_id }
            if (ci >= 0) spCategoria.setSelection(ci)
            if (existing.proveedor_id != null) {
                val pi = proveedoresList.indexOfFirst { it.id_proveedor == existing.proveedor_id }
                if (pi >= 0) spProveedor.setSelection(pi + 1)
            }
            spEstado.setSelection(if (existing.activo == 1) 0 else 1)
            etNombre.setText(existing.nombre)
            etPrecioCompra.setText(existing.precio_compra.toString())
            etPrecioVenta.setText(existing.precio_venta.toString())
            etStock.setText(existing.stock_actual.toString())
            etStockMinimo.setText(existing.stock_minimo.toString())
            etDescripcion.setText(existing.descripcion ?: "")
            if (!existing.imagen_url.isNullOrEmpty()) {
                currentImageView?.let { Glide.with(activity).load(existing.imagen_url).into(it) }
            }
        }

        btnSeleccionarImagen.setOnClickListener { selectImageLauncher.launch("image/*") }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(title).setView(view)
            .setPositiveButton(btnText, null)
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) { etNombre.error = "Nombre obligatorio"; return@setOnClickListener }
            if (categoriasList.isEmpty()) { Toast.makeText(activity, "Sin categorías", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            onSave(ProductFormData(
                nombre, etDescripcion.text.toString().trim(),
                if (etPrecioCompra.text.isEmpty()) "0" else etPrecioCompra.text.toString(),
                if (etPrecioVenta.text.isEmpty()) "0" else etPrecioVenta.text.toString(),
                if (etStock.text.isEmpty()) "0" else etStock.text.toString(),
                if (etStockMinimo.text.isEmpty()) "0" else etStockMinimo.text.toString(),
                categoriasList[spCategoria.selectedItemPosition].id_categoria,
                if (spProveedor.selectedItemPosition > 0) proveedoresList[spProveedor.selectedItemPosition - 1].id_proveedor.toString() else "",
                if (spEstado.selectedItemPosition == 0) "1" else "0",
                currentImageUri
            ))
            dialog.dismiss()
        }
    }

    private fun createProducto(data: ProductFormData, rv: RecyclerView, tvEmpty: TextView) {
        sendProductMultipart(null, data, rv, tvEmpty, isCreate = true)
    }

    private fun updateProducto(data: ProductFormData, rv: RecyclerView, tvEmpty: TextView, existing: Producto) {
        sendProductMultipart(existing.id_producto, data, rv, tvEmpty, isCreate = false, existing.imagen_url)
    }

    private fun sendProductMultipart(productId: Int?, data: ProductFormData, rv: RecyclerView, tvEmpty: TextView, isCreate: Boolean, existingImageUrl: String? = null) {
        val toReq: (String) -> RequestBody = { it.toRequestBody("text/plain".toMediaTypeOrNull()) }
        var imagePart: MultipartBody.Part? = null
        data.imageUri?.let { uri ->
            try {
                val inputStream = activity.contentResolver.openInputStream(uri)
                val file = File.createTempFile("upload_", ".jpg", activity.cacheDir)
                inputStream?.use { `is` -> `is`.copyTo(FileOutputStream(file)) }
                val mimeType = activity.contentResolver.getType(uri) ?: "image/jpeg"
                imagePart = MultipartBody.Part.createFormData("imagen", file.name, file.asRequestBody(mimeType.toMediaTypeOrNull()))
            } catch (_: Exception) {}
        }
        val imgUrlPart = if (!isCreate && imagePart == null && !existingImageUrl.isNullOrEmpty()) toReq(existingImageUrl) else null

        val call = if (isCreate) {
            ApiClient.instance.createProducto(
                toReq(data.categoriaId.toString()), toReq(data.proveedorId),
                toReq(data.nombre), toReq(data.descripcion),
                toReq(data.precioCompra), toReq(data.precioVenta),
                toReq(data.stock), toReq(data.stockMinimo),
                toReq(data.activo), imagePart
            )
        } else {
            ApiClient.instance.updateProducto(
                productId!!,
                toReq(data.categoriaId.toString()), toReq(data.proveedorId),
                toReq(data.nombre), toReq(data.descripcion),
                toReq(data.precioCompra), toReq(data.precioVenta),
                toReq(data.stock), toReq(data.stockMinimo),
                toReq(data.activo), imagePart, imgUrlPart
            )
        }

        call.enqueue(object : Callback<Void> {
            override fun onResponse(c: Call<Void>, res: Response<Void>) {
                if (res.isSuccessful) {
                    Toast.makeText(activity, if (isCreate) "Producto creado" else "Producto actualizado", Toast.LENGTH_SHORT).show()
                    loadProductos(rv, tvEmpty, rv.adapter as ProductoAdminAdapter)
                } else {
                    Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(c: Call<Void>, t: Throwable) {
                Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteProducto(producto: Producto, rv: RecyclerView, tvEmpty: TextView) {
        AlertDialog.Builder(activity)
            .setTitle("Eliminar").setMessage("¿Eliminar ${producto.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                ApiClient.instance.deleteProducto(producto.id_producto).enqueue(object : Callback<Void> {
                    override fun onResponse(c: Call<Void>, res: Response<Void>) {
                        Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                        loadProductos(rv, tvEmpty, rv.adapter as ProductoAdminAdapter)
                    }
                    override fun onFailure(c: Call<Void>, t: Throwable) {
                        Toast.makeText(activity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("No", null).show()
    }

    // ──────────── CATEGORIAS ADMIN ────────────

    fun showCategorias(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val fab = root.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        rv.layoutManager = LinearLayoutManager(activity)
        val adapter = CategoriaAdapter(emptyList(), { c -> editCategoria(c, rv, tvEmpty) }, { c -> deleteCategoria(c, rv, tvEmpty) })
        rv.adapter = adapter

        fab.setOnClickListener { createCategoria(rv, tvEmpty, adapter) }

        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(c: Call<List<Categoria>>, res: Response<List<Categoria>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Categoria>>, t: Throwable) {}
        })
    }

    private fun createCategoria(rv: RecyclerView, tvEmpty: TextView, adapter: CategoriaAdapter) {
        showCategoriaDialog(null, "Nueva Categoría", "Guardar") { nombre, desc ->
            ApiClient.instance.createCategoria(CategoriaRequest(nombre, desc)).enqueue(object : Callback<CategoriaCreateResponse> {
                override fun onResponse(c: Call<CategoriaCreateResponse>, res: Response<CategoriaCreateResponse>) {
                    if (res.isSuccessful) {
                        Toast.makeText(activity, "Categoría creada", Toast.LENGTH_SHORT).show()
                        reloadCategorias(rv, tvEmpty, adapter)
                    } else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(c: Call<CategoriaCreateResponse>, t: Throwable) {
                    Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun editCategoria(cat: Categoria, rv: RecyclerView, tvEmpty: TextView) {
        showCategoriaDialog(cat, "Editar Categoría", "Actualizar") { nombre, desc ->
            ApiClient.instance.updateCategoria(cat.id_categoria, CategoriaRequest(nombre, desc)).enqueue(object : Callback<Void> {
                override fun onResponse(c: Call<Void>, res: Response<Void>) {
                    if (res.isSuccessful) {
                        Toast.makeText(activity, "Categoría actualizada", Toast.LENGTH_SHORT).show()
                        val adapter = rv.adapter as CategoriaAdapter
                        reloadCategorias(rv, tvEmpty, adapter)
                    } else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(c: Call<Void>, t: Throwable) {
                    Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showCategoriaDialog(existing: Categoria?, title: String, btnText: String, onSave: (String, String?) -> Unit) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_categoria, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDesc = view.findViewById<EditText>(R.id.etDescripcion)

        if (existing != null) {
            etNombre.setText(existing.nombre)
            etDesc.setText(existing.descripcion ?: "")
        }

        AlertDialog.Builder(activity)
            .setTitle(title).setView(view)
            .setPositiveButton(btnText) { _, _ ->
                val nombre = etNombre.text.toString().trim()
                if (nombre.isEmpty()) { Toast.makeText(activity, "Nombre obligatorio", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                onSave(nombre, etDesc.text.toString().trim().ifEmpty { null })
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun deleteCategoria(cat: Categoria, rv: RecyclerView, tvEmpty: TextView) {
        AlertDialog.Builder(activity)
            .setTitle("Eliminar").setMessage("¿Eliminar ${cat.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                ApiClient.instance.deleteCategoria(cat.id_categoria).enqueue(object : Callback<Void> {
                    override fun onResponse(c: Call<Void>, res: Response<Void>) {
                        Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                        val adapter = rv.adapter as CategoriaAdapter
                        reloadCategorias(rv, tvEmpty, adapter)
                    }
                    override fun onFailure(c: Call<Void>, t: Throwable) {
                        Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("No", null).show()
    }

    private fun reloadCategorias(rv: RecyclerView, tvEmpty: TextView, adapter: CategoriaAdapter) {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(c: Call<List<Categoria>>, res: Response<List<Categoria>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Categoria>>, t: Throwable) {}
        })
    }

    // ──────────── USUARIOS ADMIN ────────────

    fun showUsuarios(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        rv.layoutManager = LinearLayoutManager(activity)
        lateinit var adapter: UsuarioAdapter
        val deleteHandler: (Usuario) -> Unit = { usuario ->
            AlertDialog.Builder(activity)
                .setTitle("Eliminar").setMessage("¿Eliminar ${usuario.nombre}?")
                .setPositiveButton("Sí") { _, _ ->
                    ApiClient.instance.deleteUsuario(usuario.id_usuario).enqueue(object : Callback<Void> {
                        override fun onResponse(c: Call<Void>, res: Response<Void>) {
                            Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                            reloadUsuarios(rv, tvEmpty, adapter)
                        }
                        override fun onFailure(c: Call<Void>, t: Throwable) {}
                    })
                }
                .setNegativeButton("No", null).show()
        }
        adapter = UsuarioAdapter(
            emptyList(),
            onEdit = { usuario ->
                AlertDialog.Builder(activity)
                    .setTitle(usuario.nombre)
                    .setMessage("Email: ${usuario.email}\nTel: ${usuario.telefono ?: "N/A"}\nRol: ${usuario.rol_id}")
                    .setPositiveButton("OK", null).show()
            },
            onDelete = deleteHandler
        )
        rv.adapter = adapter

        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(c: Call<List<Usuario>>, res: Response<List<Usuario>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun reloadUsuarios(rv: RecyclerView, tvEmpty: TextView, adapter: UsuarioAdapter) {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(c: Call<List<Usuario>>, res: Response<List<Usuario>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Usuario>>, t: Throwable) {}
        })
    }

    // ──────────── PROVEEDORES ADMIN ────────────

    fun showProveedores(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val fab = root.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        rv.layoutManager = LinearLayoutManager(activity)
        lateinit var adapter: ProveedorAdapter
        val deleteHandler: (Proveedor) -> Unit = { p ->
            AlertDialog.Builder(activity).setTitle("Eliminar")
                .setMessage("¿Eliminar ${p.nombre}?").setPositiveButton("Sí") { _, _ ->
                    ApiClient.instance.deleteProveedor(p.id_proveedor!!).enqueue(object : Callback<Void> {
                        override fun onResponse(c: Call<Void>, res: Response<Void>) {
                            Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                            reloadProveedores(rv, tvEmpty, adapter)
                        }
                        override fun onFailure(c: Call<Void>, t: Throwable) {}
                    })
                }.setNegativeButton("No", null).show()
        }
        adapter = ProveedorAdapter(emptyList(),
            { p ->
                AlertDialog.Builder(activity).setTitle(p.nombre)
                    .setMessage("NIT: ${p.nit}\nTel: ${p.telefono}\nCorreo: ${p.correo}")
                    .setPositiveButton("OK", null).show()
            },
            deleteHandler)
        rv.adapter = adapter

        fab.setOnClickListener {
            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_proveedor, null)
            val etNombre = view.findViewById<EditText>(R.id.etNombre)
            val etNit = view.findViewById<EditText>(R.id.etNit)
            val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
            val etCorreo = view.findViewById<EditText>(R.id.etCorreo)
            val etDireccion = view.findViewById<EditText>(R.id.etDireccion)
            AlertDialog.Builder(activity).setTitle("Nuevo Proveedor").setView(view)
                .setPositiveButton("Guardar") { _, _ ->
                    val n = etNombre.text.toString().trim()
                    if (n.isEmpty()) { Toast.makeText(activity, "Nombre obligatorio", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                    ApiClient.instance.createProveedor(Proveedor(
                        nit = etNit.text.toString().trim().ifEmpty { "N/A" },
                        nombre = n,
                        telefono = etTelefono.text.toString().trim().ifEmpty { "N/A" },
                        correo = etCorreo.text.toString().trim().ifEmpty { "N/A" },
                        direccion = etDireccion.text.toString().trim().ifEmpty { "N/A" }
                    )).enqueue(object : Callback<ProveedorResponse> {
                        override fun onResponse(c: Call<ProveedorResponse>, res: Response<ProveedorResponse>) {
                            if (res.isSuccessful) { Toast.makeText(activity, "Creado", Toast.LENGTH_SHORT).show(); reloadProveedores(rv, tvEmpty, adapter) }
                            else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(c: Call<ProveedorResponse>, t: Throwable) { Toast.makeText(activity, "Error conexión", Toast.LENGTH_SHORT).show() }
                    })
                }.setNegativeButton("Cancelar", null).show()
        }

        reloadProveedores(rv, tvEmpty, adapter)
    }

    private fun reloadProveedores(rv: RecyclerView, tvEmpty: TextView, adapter: ProveedorAdapter) {
        ApiClient.instance.getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(c: Call<List<Proveedor>>, res: Response<List<Proveedor>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Proveedor>>, t: Throwable) {}
        })
    }

    // ──────────── REPARTIDORES (Entregas) ────────────

    fun showRepartidores(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        rv.layoutManager = LinearLayoutManager(activity)
        lateinit var adapter: EntregaAdapter
        adapter = EntregaAdapter(
            emptyList(),
            onVerMapaClick = { pedido ->
                Toast.makeText(activity, "Mapa: Pedido #${pedido.id_pedido}", Toast.LENGTH_SHORT).show()
            },
            onConfirmarClick = { pedido ->
                ApiClient.instance.cambiarEstadoPedido(pedido.id_pedido, EstadoPedidoRequest("ENTREGADO", null))
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(c: Call<Void>, res: Response<Void>) {
                            if (res.isSuccessful) {
                                Toast.makeText(activity, "Pedido #${pedido.id_pedido} entregado", Toast.LENGTH_SHORT).show()
                                adapter.updateData(emptyList())
                            }
                        }
                        override fun onFailure(c: Call<Void>, t: Throwable) {}
                    })
            },
            onItemClick = { pedido ->
                AlertDialog.Builder(activity).setTitle("Pedido #${pedido.id_pedido}")
                    .setMessage("Estado: ${pedido.estado}\nCliente: ${pedido.cliente?.nombre ?: "N/A"}")
                    .setPositiveButton("OK", null).show()
            }
        )
        rv.adapter = adapter

        ApiClient.instance.getPedidosSinAsignar().enqueue(object : Callback<List<PedidoRepartidor>> {
            override fun onResponse(c: Call<List<PedidoRepartidor>>, res: Response<List<PedidoRepartidor>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<PedidoRepartidor>>, t: Throwable) {
                Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ──────────── ROLES ────────────

    fun showRoles(root: View) {
        root.findViewById<TextView>(R.id.tvEmpty).apply {
            text = "Gestión de Roles\n(Próximamente)"
            visibility = View.VISIBLE
        }
    }
}
