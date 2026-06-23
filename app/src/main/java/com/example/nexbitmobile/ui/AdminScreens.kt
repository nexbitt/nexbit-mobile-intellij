package com.example.nexbitmobile.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
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
    private var selectedCategoriaId: Int? = null
    private var currentImageUri: Uri? = null
    private var currentImageView: ImageView? = null
    private var currentFlUpload: View? = null
    private val selectImageLauncher = activity.registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            currentImageView?.let { iv ->
                iv.visibility = View.VISIBLE
                Glide.with(activity).load(it).into(iv)
            }
            currentFlUpload?.let { fl ->
                fl.foreground = null
                fl.findViewById<View>(R.id.flImageOverlay)?.visibility = View.GONE
            }
        }
    }

    // ──────────── PRODUCTOS ADMIN ────────────

    fun showProductos(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = root.findViewById<View>(R.id.btnAddHeader)
        val etSearch = root.findViewById<EditText>(R.id.etSearch)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)
        val chipLayout = root.findViewById<LinearLayout>(R.id.layoutCategoriaFilters)

        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Productos"

        selectedCategoriaId = null
        rv.layoutManager = LinearLayoutManager(activity)
        val adapter = ProductoAdminAdapter(emptyList(),
            { p -> showEditDialog(p, rv, tvEmpty) },
            { p -> deleteProducto(p, rv, tvEmpty) }
        )
        rv.adapter = adapter

        val aplicarFiltros = {
            val query = etSearch.text.toString().trim().lowercase()
            val filtered = allProductos.filter { p ->
                val matchesSearch = query.isEmpty() ||
                    p.nombre.lowercase().contains(query) ||
                    (p.categoria_nombre?.lowercase()?.contains(query) == true)
                val matchesCategoria = selectedCategoriaId == null || p.categoria_id == selectedCategoriaId
                matchesSearch && matchesCategoria
            }
            adapter.updateData(filtered)
            tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            rv.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { aplicarFiltros() }
        })

        btnAdd.setOnClickListener { showCreateDialog(rv, tvEmpty) }

        loadCategorias(chipLayout, aplicarFiltros)
        loadProveedores()
        loadProductos(rv, tvEmpty, adapter, chipLayout, aplicarFiltros)
    }

    private fun buildCategoriaChips(chipLayout: LinearLayout, aplicarFiltros: () -> Unit) {
        chipLayout.removeAllViews()

        val ctx = chipLayout.context

        val chipTodos = TextView(ctx).apply {
            text = "Todas"
            setTextColor(ContextCompat.getColor(ctx, if (selectedCategoriaId == null) R.color.chip_selected_text else R.color.chip_text))
            setBackgroundResource(if (selectedCategoriaId == null) R.drawable.bg_chip_selected else R.drawable.bg_chip)
            setPadding(28, 0, 28, 0)
            height = 32.dpToPx(ctx)
            gravity = android.view.Gravity.CENTER
            textSize = 12f
            setTextAppearance(android.R.style.TextAppearance_Small)
            setTypeface(null, android.graphics.Typeface.BOLD)
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(0, 0, 6.dpToPx(ctx), 0) }
            setOnClickListener {
                selectedCategoriaId = null; aplicarFiltros(); buildCategoriaChips(chipLayout, aplicarFiltros)
            }
        }
        chipLayout.addView(chipTodos)

        for (cat in categoriasList) {
            val isSelected = selectedCategoriaId == cat.id_categoria
            val chip = TextView(ctx).apply {
                text = cat.nombre
                setTextColor(ContextCompat.getColor(ctx, if (isSelected) R.color.chip_selected_text else R.color.chip_text))
                setBackgroundResource(if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip)
                setPadding(28, 0, 28, 0)
                height = 32.dpToPx(ctx)
                gravity = android.view.Gravity.CENTER
                textSize = 12f
                setTextAppearance(android.R.style.TextAppearance_Small)
                setTypeface(null, android.graphics.Typeface.BOLD)
                isClickable = true
                isFocusable = true
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(0, 0, 6.dpToPx(ctx), 0) }
                setOnClickListener {
                    selectedCategoriaId = cat.id_categoria; aplicarFiltros(); buildCategoriaChips(chipLayout, aplicarFiltros)
                }
            }
            chipLayout.addView(chip)
        }
    }

    private fun loadCategorias(chipLayout: LinearLayout? = null, aplicarFiltros: (() -> Unit)? = null) {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(call: Call<List<Categoria>>, response: Response<List<Categoria>>) {
                if (response.isSuccessful) {
                    categoriasList = response.body() ?: emptyList()
                    chipLayout?.let { cl -> aplicarFiltros?.let { af -> buildCategoriaChips(cl, af) } }
                }
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

    private fun loadProductos(rv: RecyclerView, tvEmpty: TextView, adapter: ProductoAdminAdapter, chipLayout: LinearLayout? = null, aplicarFiltros: (() -> Unit)? = null) {
        ApiClient.instance.getProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    allProductos = response.body() ?: emptyList()
                    aplicarFiltros?.invoke()
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
        val flImageUpload = view.findViewById<View>(R.id.flImageUpload)
        currentImageView = view.findViewById(R.id.ivPreview)
        currentFlUpload = flImageUpload

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
                currentImageView?.let { iv ->
                    iv.visibility = View.VISIBLE
                    Glide.with(activity).load(existing.imagen_url).into(iv)
                }
                currentFlUpload?.let { fl ->
                    fl.foreground = null
                    fl.findViewById<View>(R.id.flImageOverlay)?.visibility = View.GONE
                }
            }
        }

        flImageUpload.setOnClickListener { selectImageLauncher.launch("image/*") }

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
        val btnAdd = root.findViewById<View>(R.id.btnAddHeader)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Categorías"

        rv.layoutManager = LinearLayoutManager(activity)
        val adapter = CategoriaAdapter(emptyList(), { c -> editCategoria(c, rv, tvEmpty) }, { c -> deleteCategoria(c, rv, tvEmpty) })
        rv.adapter = adapter

        btnAdd.setOnClickListener { createCategoria(rv, tvEmpty, adapter) }

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
        val etSearch = root.findViewById<EditText>(R.id.etSearch)
        val btnAdd = root.findViewById<View>(R.id.btnAddHeader)
        val filterContainer = root.findViewById<LinearLayout>(R.id.filterChipsContainer)

        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Usuarios"
        rv.layoutManager = LinearLayoutManager(activity)

        var allUsers = mutableListOf<Usuario>()
        var activeFilter = ""

        val chips = listOf("Todos", "Admin", "Repartidor", "Cliente", "Activo", "Inactivo")
        for (chipText in chips) {
            val chip = TextView(activity).apply {
                text = chipText
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(16, 0, 16, 0)
                setOnClickListener {
                    activeFilter = if (chipText == "Todos") "" else chipText
                    aplicarFiltros(etSearch, activeFilter, allUsers, { filtered ->
                        val adapter = rv.adapter as? UsuarioAdapter
                        adapter?.updateData(filtered)
                        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                    })
                    // highlight active chip
                    for (i in 0 until filterContainer.childCount) {
                        val c = filterContainer.getChildAt(i) as TextView
                        if (c.text == chipText) {
                            c.setBackgroundColor(android.graphics.Color.parseColor("#111827"))
                            c.setTextColor(android.graphics.Color.parseColor("#ffffff"))
                        } else {
                            c.background = null
                            c.setTextColor(android.graphics.Color.parseColor("#374151"))
                        }
                    }
                }
            }
            filterContainer.addView(chip, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                36.dpToPx(activity)
            ).apply { marginEnd = 8 })
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                aplicarFiltros(etSearch, activeFilter, allUsers) { filtered ->
                    val adapter = rv.adapter as? UsuarioAdapter
                    adapter?.updateData(filtered)
                    tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        lateinit var adapter: UsuarioAdapter
        val deleteHandler: (Usuario) -> Unit = { usuario ->
            AlertDialog.Builder(activity)
                .setTitle("Eliminar").setMessage("¿Eliminar ${usuario.nombre}?")
                .setPositiveButton("Sí") { _, _ ->
                    ApiClient.instance.deleteUsuario(usuario.id_usuario).enqueue(object : Callback<Void> {
                        override fun onResponse(c: Call<Void>, res: Response<Void>) {
                            Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                            cargarUsuarios(rv, tvEmpty, adapter, allUsers)
                        }
                        override fun onFailure(c: Call<Void>, t: Throwable) {}
                    })
                }
                .setNegativeButton("No", null).show()
        }
        adapter = UsuarioAdapter(
            emptyList(),
            onEdit = { usuario -> mostrarDialogoEditarUsuario(usuario, rv, tvEmpty, adapter) },
            onDelete = deleteHandler
        )
        rv.adapter = adapter

        btnAdd.setOnClickListener { mostrarDialogoCrearUsuario(rv, tvEmpty, adapter) }

        cargarUsuarios(rv, tvEmpty, adapter, allUsers)
    }

    private fun aplicarFiltros(etSearch: EditText, activeFilter: String, allUsers: List<Usuario>, onResult: (List<Usuario>) -> Unit) {
        val query = etSearch.text.toString().trim().lowercase()
        var filtered = allUsers
        if (query.isNotEmpty()) {
            filtered = filtered.filter { u ->
                u.nombre.lowercase().contains(query) || u.email.lowercase().contains(query)
            }
        }
        when (activeFilter) {
            "Admin" -> filtered = filtered.filter { it.rol_id == 1 }
            "Repartidor" -> filtered = filtered.filter { it.rol_id == 4 }
            "Cliente" -> filtered = filtered.filter { it.rol_id == 2 }
            "Activo" -> filtered = filtered.filter { it.activo }
            "Inactivo" -> filtered = filtered.filter { !it.activo }
        }
        onResult(filtered)
    }

    private fun cargarUsuarios(rv: RecyclerView, tvEmpty: TextView, adapter: UsuarioAdapter, allUsers: MutableList<Usuario>) {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(c: Call<List<Usuario>>, res: Response<List<Usuario>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    allUsers.clear()
                    allUsers.addAll(list)
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

    private fun mostrarDialogoCrearUsuario(rv: RecyclerView, tvEmpty: TextView, adapter: UsuarioAdapter) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_usuario, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val spRol = view.findViewById<Spinner>(R.id.spRol)
        val etDocType = view.findViewById<EditText>(R.id.etDocType)
        val etDocNum = view.findViewById<EditText>(R.id.etDocNum)
        val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)

        spRol.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, listOf("Admin", "Repartidor", "Cliente"))

        AlertDialog.Builder(activity)
            .setTitle("Nuevo Usuario")
            .setView(view)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity, "Nombre, email y password obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val rolMap = mapOf(0 to 1, 1 to 4, 2 to 2)
                ApiClient.instance.createUsuario(UsuarioCreateRequest(
                    rol_id = rolMap[spRol.selectedItemPosition] ?: 4,
                    nombre = nombre,
                    email = email,
                    password = password,
                    tipo_documento = etDocType.text.toString().trim().ifEmpty { null },
                    numero_documento = etDocNum.text.toString().trim().ifEmpty { null },
                    telefono = etTelefono.text.toString().trim().ifEmpty { null },
                    direccion = etDireccion.text.toString().trim().ifEmpty { null }
                )).enqueue(object : Callback<UsuarioCreateResponse> {
                    override fun onResponse(c: Call<UsuarioCreateResponse>, res: Response<UsuarioCreateResponse>) {
                        if (res.isSuccessful) {
                            Toast.makeText(activity, "Usuario creado", Toast.LENGTH_SHORT).show()
                            val allUsers = mutableListOf<Usuario>()
                            cargarUsuarios(rv, tvEmpty, adapter, allUsers)
                        } else {
                            Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(c: Call<UsuarioCreateResponse>, t: Throwable) {
                        Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarUsuario(usuario: Usuario, rv: RecyclerView, tvEmpty: TextView, adapter: UsuarioAdapter) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_usuario, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val spRol = view.findViewById<Spinner>(R.id.spRol)
        val etDocType = view.findViewById<EditText>(R.id.etDocType)
        val etDocNum = view.findViewById<EditText>(R.id.etDocNum)
        val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)

        etNombre.setText(usuario.nombre)
        etEmail.setText(usuario.email)
        etPassword.hint = "Dejar vacío para no cambiar"
        etDocType.setText(usuario.tipo_documento ?: "")
        etDocNum.setText(usuario.numero_documento ?: "")
        etTelefono.setText(usuario.telefono ?: "")
        etDireccion.setText(usuario.direccion ?: "")

        spRol.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, listOf("Admin", "Repartidor", "Cliente"))
        val rolIndex = mapOf(1 to 0, 4 to 1, 2 to 2)[usuario.rol_id] ?: 2
        spRol.setSelection(rolIndex)
        etPassword.visibility = View.GONE

        AlertDialog.Builder(activity)
            .setTitle("Editar Usuario")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val email = etEmail.text.toString().trim()
                if (nombre.isEmpty() || email.isEmpty()) {
                    Toast.makeText(activity, "Nombre y email obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val rolMap = mapOf(0 to 1, 1 to 4, 2 to 2)
                ApiClient.instance.updateUsuario(usuario.id_usuario, UsuarioUpdateRequest(
                    nombre = nombre,
                    email = email,
                    tipo_documento = etDocType.text.toString().trim().ifEmpty { null },
                    numero_documento = etDocNum.text.toString().trim().ifEmpty { null },
                    telefono = etTelefono.text.toString().trim().ifEmpty { null },
                    direccion = etDireccion.text.toString().trim().ifEmpty { null }
                )).enqueue(object : Callback<Usuario> {
                    override fun onResponse(c: Call<Usuario>, res: Response<Usuario>) {
                        if (res.isSuccessful) {
                            Toast.makeText(activity, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                            val allUsers = mutableListOf<Usuario>()
                            cargarUsuarios(rv, tvEmpty, adapter, allUsers)
                        } else {
                            Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(c: Call<Usuario>, t: Throwable) {
                        Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ──────────── PROVEEDORES ADMIN ────────────

    fun showProveedores(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = root.findViewById<View>(R.id.btnAddHeader)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Proveedores"

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

        btnAdd.setOnClickListener {
            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_proveedor, null)
            val etNombre = view.findViewById<EditText>(R.id.etNombre)
            val etNit = view.findViewById<EditText>(R.id.etNit)
            val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
            val etCorreo = view.findViewById<EditText>(R.id.etCorreo)
            val etDireccion = view.findViewById<EditText>(R.id.etDireccion)

            etNit.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            etNit.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    val digits = s?.filter { it.isDigit() } ?: ""
                    if (digits != s.toString()) {
                        etNit.setText(digits)
                        etNit.setSelection(digits.length)
                    }
                }
            })

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
        val etSearch = root.findViewById<EditText>(R.id.etSearch)
        val btnAdd = root.findViewById<View>(R.id.btnAddHeader)

        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Repartidores"

        rv.layoutManager = LinearLayoutManager(activity)
        lateinit var adapter: RepartidorAdminAdapter
        adapter = RepartidorAdminAdapter(
            emptyList(),
            onViewClick = { rep ->
                val intent = Intent(activity, RepartidorDetailActivity::class.java)
                intent.putExtra("repartidor_id", rep.id_usuario)
                intent.putExtra("repartidor_nombre", rep.nombre)
                intent.putExtra("repartidor_email", rep.email)
                intent.putExtra("repartidor_telefono", rep.telefono ?: "")
                intent.putExtra("repartidor_direccion", rep.direccion ?: "")
                activity.startActivity(intent)
            },
            onDeleteClick = { rep ->
                AlertDialog.Builder(activity)
                    .setTitle("Eliminar Repartidor")
                    .setMessage("¿Desactivar a ${rep.nombre}?")
                    .setPositiveButton("Sí") { _, _ ->
                        ApiClient.instance.updateUsuario(rep.id_usuario, UsuarioUpdateRequest(activo = false))
                            .enqueue(object : Callback<Usuario> {
                                override fun onResponse(c: Call<Usuario>, res: Response<Usuario>) {
                                    if (res.isSuccessful) {
                                        Toast.makeText(activity, "Repartidor desactivado", Toast.LENGTH_SHORT).show()
                                        cargarRepartidores(rv, tvEmpty, adapter, etSearch)
                                    } else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                                }
                                override fun onFailure(c: Call<Usuario>, t: Throwable) {
                                    Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                    .setNegativeButton("No", null).show()
            }
        )
        rv.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                cargarRepartidores(rv, tvEmpty, adapter, etSearch)
            }
        })

        btnAdd.setOnClickListener {
            Toast.makeText(activity, "Usar menú Usuarios para crear repartidores", Toast.LENGTH_SHORT).show()
        }

        cargarRepartidores(rv, tvEmpty, adapter, etSearch)
    }

    private fun cargarRepartidores(rv: RecyclerView, tvEmpty: TextView, adapter: RepartidorAdminAdapter, etSearch: EditText) {
        val query = etSearch.text.toString().trim().lowercase()
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(c: Call<List<Usuario>>, res: Response<List<Usuario>>) {
                if (res.isSuccessful) {
                    val todos = res.body() ?: emptyList()
                    var repartidores = todos.filter { it.rol_id == 4 }
                    if (query.isNotEmpty()) {
                        repartidores = repartidores.filter {
                            it.nombre.lowercase().contains(query) ||
                                it.email.lowercase().contains(query)
                        }
                    }
                    adapter.updateData(repartidores)
                    rv.visibility = if (repartidores.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (repartidores.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ──────────── ROLES ────────────

    fun showRoles(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)
        val btnNuevo = root.findViewById<TextView>(R.id.btnNuevoRol)

        rv.layoutManager = LinearLayoutManager(activity)
        val adapter = RolAdapter(emptyList()) { rol -> editRol(rol, rv, tvEmpty) }
        rv.adapter = adapter

        btnNuevo.setOnClickListener { mostrarDialogoNuevoRol(rv, tvEmpty) }

        reloadRoles(rv, tvEmpty, adapter)
    }

    private fun mostrarDialogoNuevoRol(rv: RecyclerView, tvEmpty: TextView) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_rol, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDesc = view.findViewById<EditText>(R.id.etDescripcion)

        AlertDialog.Builder(activity)
            .setTitle("Nuevo Rol")
            .setView(view)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(activity, "Nombre obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                ApiClient.instance.createRol(RolUpdateRequest(nombre, etDesc.text.toString().trim().ifEmpty { null }))
                    .enqueue(object : Callback<JsonResponse> {
                        override fun onResponse(c: Call<JsonResponse>, res: Response<JsonResponse>) {
                            if (res.isSuccessful) {
                                Toast.makeText(activity, "Rol creado", Toast.LENGTH_SHORT).show()
                                val adapter = rv.adapter as? RolAdapter ?: return
                                reloadRoles(rv, tvEmpty, adapter)
                            } else {
                                Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(c: Call<JsonResponse>, t: Throwable) {
                            Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editRol(rol: Rol, rv: RecyclerView, tvEmpty: TextView) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_rol, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDesc = view.findViewById<EditText>(R.id.etDescripcion)

        etNombre.setText(rol.nombre)
        etDesc.setText(rol.descripcion ?: "")

        AlertDialog.Builder(activity)
            .setTitle("Editar Rol")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(activity, "Nombre obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                ApiClient.instance.updateRol(rol.id_rol, RolUpdateRequest(nombre, etDesc.text.toString().trim().ifEmpty { null }))
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(c: Call<Void>, res: Response<Void>) {
                            if (res.isSuccessful) {
                                Toast.makeText(activity, "Rol actualizado", Toast.LENGTH_SHORT).show()
                                reloadRoles(rv, tvEmpty, rv.adapter as RolAdapter)
                            } else {
                                Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(c: Call<Void>, t: Throwable) {
                            Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun reloadRoles(rv: RecyclerView, tvEmpty: TextView, adapter: RolAdapter) {
        ApiClient.instance.getRoles().enqueue(object : Callback<List<Rol>> {
            override fun onResponse(c: Call<List<Rol>>, res: Response<List<Rol>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Rol>>, t: Throwable) {}
        })
    }
}

private fun Int.dpToPx(ctx: android.content.Context): Int {
    return (this * ctx.resources.displayMetrics.density).toInt()
}
