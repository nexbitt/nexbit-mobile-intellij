package com.example.nexbitmobile.ui

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.ui.admin.helpers.CategoriasAdminHelper
import com.example.nexbitmobile.ui.admin.helpers.ProductosAdminHelper
import com.example.nexbitmobile.ui.admin.helpers.ProveedoresAdminHelper
import com.example.nexbitmobile.ui.admin.helpers.UsuariosAdminHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminScreens(private val activity: MainOrbixActivity) {

    val productosHelper = ProductosAdminHelper(activity)
    val categoriasHelper = CategoriasAdminHelper(activity)
    val usuariosHelper = UsuariosAdminHelper(activity)
    val proveedoresHelper = ProveedoresAdminHelper(activity)

    // ──────────── DELEGACIÓN A HELPERS ────────────

    fun showProductos(root: View) = productosHelper.showProductos(root)
    fun showCategorias(root: View) = categoriasHelper.showCategorias(root)
    fun showUsuarios(root: View) = usuariosHelper.showUsuarios(root)
    fun showProveedores(root: View) = proveedoresHelper.showProveedores(root)

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
            onEdit = { rep ->
                val intent = Intent(activity, RepartidorDetailActivity::class.java)
                intent.putExtra("repartidor_id", rep.id_usuario)
                intent.putExtra("repartidor_nombre", rep.nombre)
                intent.putExtra("repartidor_email", rep.email)
                intent.putExtra("repartidor_telefono", rep.telefono ?: "")
                intent.putExtra("repartidor_direccion", rep.direccion ?: "")
                activity.startActivity(intent)
            },
            onDelete = { rep ->
                AlertDialog.Builder(activity)
                    .setTitle("Suspender Repartidor")
                    .setMessage("¿Suspender a ${rep.nombre}?")
                    .setPositiveButton("Sí") { _, _ ->
                        ApiClient.instance.updateUsuarioByAdmin(rep.id_usuario, UsuarioUpdateRequest(activo = false))
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(c: Call<Void>, res: Response<Void>) {
                                    if (res.isSuccessful) {
                                        Toast.makeText(activity, "Repartidor suspendido", Toast.LENGTH_SHORT).show()
                                        cargarRepartidores(rv, tvEmpty, adapter, etSearch)
                                    } else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                                }
                                override fun onFailure(c: Call<Void>, t: Throwable) {
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
