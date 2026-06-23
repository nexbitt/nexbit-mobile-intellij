package com.example.nexbitmobile.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.nexbitmobile.model.Rol
import com.example.nexbitmobile.model.RolUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RolesAdminActivity : AppCompatActivity() {

    private lateinit var rvRoles: RecyclerView
    private lateinit var adapter: RolAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: EditText
    private var allRoles: List<Rol> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_roles_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvRoles = findViewById(R.id.rvRoles)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        etSearch = findViewById(R.id.etSearch)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnAddRol).setOnClickListener { showRolDialog(null) }

        adapter = RolAdapter(emptyList()) { rol -> showRolDialog(rol) }
        rvRoles.layoutManager = LinearLayoutManager(this)
        rvRoles.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { filterRoles() }
        })

        loadRoles()
    }

    private fun loadRoles() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        ApiClient.instance.getRoles().enqueue(object : Callback<List<Rol>> {
            override fun onResponse(call: Call<List<Rol>>, response: Response<List<Rol>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    allRoles = response.body() ?: emptyList()
                    filterRoles()
                } else {
                    Toast.makeText(this@RolesAdminActivity, "Error al cargar roles (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Rol>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@RolesAdminActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterRoles() {
        val query = etSearch.text.toString().trim().lowercase()
        val filtered = if (query.isEmpty()) allRoles
        else allRoles.filter { it.nombre.lowercase().contains(query) || (it.descripcion?.lowercase()?.contains(query) == true) }

        adapter.updateData(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        rvRoles.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showRolDialog(rol: Rol?) {
        val isEditing = rol != null
        val view = layoutInflater.inflate(R.layout.dialog_rol, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)

        if (isEditing) {
            etNombre.setText(rol!!.nombre)
            etDescripcion.setText(rol.descripcion ?: "")
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEditing) "Editar Rol" else "Nuevo Rol")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val descripcion = etDescripcion.text.toString().trim().ifEmpty { null }
                val request = RolUpdateRequest(nombre = nombre.uppercase(), descripcion = descripcion)

                if (isEditing) {
                    ApiClient.instance.updateRol(rol!!.id_rol, request).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@RolesAdminActivity, "Rol actualizado", Toast.LENGTH_SHORT).show()
                                loadRoles()
                            } else {
                                Toast.makeText(this@RolesAdminActivity, "Error al actualizar (${response.code()})", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@RolesAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    ApiClient.instance.updateRol(0, request).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Toast.makeText(this@RolesAdminActivity, "Rol creado", Toast.LENGTH_SHORT).show()
                            loadRoles()
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@RolesAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
