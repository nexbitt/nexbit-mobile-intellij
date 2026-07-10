package com.example.nexbitmobile.ui.admin.helpers

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.ui.MainOrbixActivity
import com.example.nexbitmobile.ui.UsuarioAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsuariosAdminHelper(private val activity: MainOrbixActivity) {

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
                    aplicarFiltros(etSearch, activeFilter, allUsers) { filtered ->
                        val adapter = rv.adapter as? UsuarioAdapter
                        adapter?.updateData(filtered)
                        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                    }
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
}

private fun Int.dpToPx(ctx: android.content.Context): Int {
    return (this * ctx.resources.displayMetrics.density).toInt()
}
