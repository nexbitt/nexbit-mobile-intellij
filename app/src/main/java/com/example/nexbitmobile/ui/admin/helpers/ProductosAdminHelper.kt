package com.example.nexbitmobile.ui.admin.helpers

import android.app.AlertDialog
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
import com.example.nexbitmobile.ui.MainOrbixActivity
import com.example.nexbitmobile.ui.ProductoAdminAdapter
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

class ProductosAdminHelper(private val activity: MainOrbixActivity) {

    var categoriasList = listOf<Categoria>()
    var proveedoresList = listOf<Proveedor>()
    var allProductos = listOf<Producto>()
    var selectedCategoriaId: Int? = null
    var currentImageUri: Uri? = null
    var currentImageView: ImageView? = null
    var currentFlUpload: View? = null

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
            { p -> Toast.makeText(activity, "Stock: ${p.stock_actual}", Toast.LENGTH_SHORT).show() }
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

    fun buildCategoriaChips(chipLayout: LinearLayout, aplicarFiltros: () -> Unit) {
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

    fun loadCategorias(chipLayout: LinearLayout? = null, aplicarFiltros: (() -> Unit)? = null) {
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

    fun loadProveedores() {
        ApiClient.instance.getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(call: Call<List<Proveedor>>, response: Response<List<Proveedor>>) {
                if (response.isSuccessful) proveedoresList = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Proveedor>>, t: Throwable) {}
        })
    }

    fun loadProductos(rv: RecyclerView, tvEmpty: TextView, adapter: ProductoAdminAdapter, chipLayout: LinearLayout? = null, aplicarFiltros: (() -> Unit)? = null) {
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

    fun showCreateDialog(rv: RecyclerView, tvEmpty: TextView) {
        currentImageUri = null
        buildProductDialog(null, "Nuevo Producto", "Guardar") { p -> createProducto(p, rv, tvEmpty) }
    }

    fun showEditDialog(producto: Producto, rv: RecyclerView, tvEmpty: TextView) {
        currentImageUri = null
        buildProductDialog(producto, "Editar Producto", "Actualizar") { p -> updateProducto(p, rv, tvEmpty, producto) }
    }

    data class ProductFormData(
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

    fun createProducto(data: ProductFormData, rv: RecyclerView, tvEmpty: TextView) {
        sendProductMultipart(null, data, rv, tvEmpty, isCreate = true)
    }

    fun updateProducto(data: ProductFormData, rv: RecyclerView, tvEmpty: TextView, existing: Producto) {
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

    fun deleteProducto(producto: Producto, rv: RecyclerView, tvEmpty: TextView) {
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
}

private fun Int.dpToPx(ctx: android.content.Context): Int {
    return (this * ctx.resources.displayMetrics.density).toInt()
}
